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
    @Operation(summary = "Handle Google OAuth callback", description = "Handles the OAuth2 callback from Google")
    public void googleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletResponse response) throws IOException {

        try {
            // Xử lý lỗi từ Google
            if (error != null) {
                logger.error("Google OAuth error received: {}", error);
                String redirectUrl = "http://localhost:5173/auth/callback?error=" +
                        URLEncoder.encode(error, StandardCharsets.UTF_8.toString());
                response.sendRedirect(redirectUrl);
                return;
            }

            // Xử lý authorization code
            if (code != null) {
                logger.info("Google OAuth callback received with code");

                // 1-4. Exchange code, get user info, create/update user, generate JWT
                LoginResponse loginResponse = userService.processGoogleOAuthCallback(code, state);

                logger.info("Login response created successfully");
                logger.info("Token generated for user: {}", loginResponse.getUser().getEmail());

                // 5. Redirect về frontend với token
                String redirectUrl = "http://localhost:5173/auth/callback?token=" +
                        URLEncoder.encode(loginResponse.getToken(), StandardCharsets.UTF_8.toString());
                
                logger.info("Redirecting to frontend: {}", redirectUrl);
                response.sendRedirect(redirectUrl);
                return;
            }

            // Nếu không có code hoặc error
            logger.warn("Google OAuth callback received without code or error");
            String redirectUrl = "http://localhost:5173/auth/callback?error=" +
                    URLEncoder.encode("No authorization code received", StandardCharsets.UTF_8.toString());
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            logger.error("Google OAuth callback error", e);
            String redirectUrl = "http://localhost:5173/auth/callback?error=" +
                    URLEncoder.encode("Authentication failed: " + e.getMessage(), StandardCharsets.UTF_8.toString());
            response.sendRedirect(redirectUrl);
        }
    }
}