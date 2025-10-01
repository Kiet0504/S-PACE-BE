package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.LoginRequest;
import com.example.S_PACE.dto.request.SignUpRequest;
import com.example.S_PACE.dto.response.LoginResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.dto.response.UserResponse;
import com.example.S_PACE.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Registration failed"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ResponseDTO<UserResponse>> register(@RequestBody SignUpRequest signUpRequest) {
        try {
            logger.info("Registration request received for email: {}", signUpRequest.getEmail());
            UserResponse userResponse = userService.register(signUpRequest);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User registered successfully", userResponse));
        } catch (IllegalArgumentException ex) {
            // Handle validation errors and business logic errors
            String errorMessage = ex.getMessage();
            logger.warn("Registration validation error: {}", errorMessage);

            if (errorMessage.contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ResponseDTO<>(false, errorMessage, null));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(false, errorMessage, null));
            }
        } catch (Exception ex) {
            logger.error("Registration failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Registration failed: " + ex.getMessage(), null));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ResponseDTO<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login request received for email: {}", loginRequest.getEmail());
            LoginResponse loginResponse = userService.login(loginRequest);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Login successful", loginResponse));
        } catch (IllegalArgumentException ex) {
            logger.warn("Login validation error: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Login failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO<>(false, "Login failed: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/google/callback")
    public void googleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {

        try {
            logger.info("Google OAuth callback received with code: {}", code);

            // Use the service method instead of local method
            LoginResponse loginResponse = userService.processGoogleOAuthCallback(code, state);

            logger.info("Login response created successfully");
            logger.info("Token: {}", loginResponse.getToken());
            logger.info("User: {}", loginResponse.getUser().getEmail());

            // Redirect về frontend với token - THÊM &success=true
            String frontendUrl = "http://localhost:5173/auth/callback?token=" +
                    URLEncoder.encode(loginResponse.getToken(), StandardCharsets.UTF_8.toString()) +
                    "&user=" + URLEncoder.encode(objectMapper.writeValueAsString(loginResponse.getUser()), StandardCharsets.UTF_8.toString()) +
                    "&success=true";  // THÊM DÒNG NÀY

            logger.info("Redirecting to frontend: {}", frontendUrl);
            response.sendRedirect(frontendUrl);

        } catch (Exception e) {
            logger.error("Google OAuth callback failed: {}", e.getMessage(), e);

            // Redirect về frontend với error
            String errorUrl = "http://localhost:5173/auth/callback?error=" +
                    URLEncoder.encode("Authentication failed: " + e.getMessage(), StandardCharsets.UTF_8.toString()) +
                    "&success=false";  // THÊM DÒNG NÀY
            response.sendRedirect(errorUrl);
        }
    }
}