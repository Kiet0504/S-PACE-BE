package com.example.S_PACE.controller;

import com.example.S_PACE.dto.response.GoogleLoginUrlResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Google Authentication", description = "Google OAuth authentication APIs")
public class GoogleAuthController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @GetMapping("/google")
    @Operation(summary = "Get Google OAuth login URL", description = "Returns the Google OAuth login URL that frontend can use to initiate login")
    public ResponseEntity<ResponseDTO<GoogleLoginUrlResponse>> getGoogleLoginUrl(HttpServletRequest request) {
        try {
            // Get the base URL
            String baseUrl = getBaseUrl(request);
            
            // Build the redirect URI - using the callback endpoint
            String redirectUri = baseUrl + "/api/auth/google/callback";
            
            // Build the Google OAuth authorization URL
            String googleAuthUrl = buildGoogleAuthUrl(redirectUri);
            
            // Create response object
            GoogleLoginUrlResponse responseData = GoogleLoginUrlResponse.builder()
                .loginUrl(googleAuthUrl)
                .redirectUri(redirectUri)
                .message("Use this URL to redirect user to Google OAuth login")
                .clientId(clientId)
                .build();
            
            logger.info("Generated Google OAuth URL for frontend: {}", googleAuthUrl);
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Google login URL generated successfully", responseData));
            
        } catch (Exception e) {
            logger.error("Failed to generate Google login URL: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ResponseDTO<>(false, "Failed to generate Google login URL: " + e.getMessage(), null));
        }
    }

    @GetMapping("/google/redirect")
    @Operation(summary = "Direct redirect to Google OAuth (legacy)", description = "Directly redirects to Google OAuth login page. Use /google endpoint instead for better frontend control.")
    public void googleLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/google/info")
    @Operation(summary = "Get Google OAuth2 user info", description = "Returns the current Google OAuth2 user information")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getGoogleUserInfo(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Google user info retrieved", attributes));
        }
        
        return ResponseEntity.ok(new ResponseDTO<>(false, "No Google user authenticated", null));
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        String port = request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort();
        String context = contextPath.isEmpty() ? "" : contextPath;
        
        return scheme + "://" + serverName + port + context;
    }

    private String buildGoogleAuthUrl(String redirectUri) {
        String baseUrl = "https://accounts.google.com/o/oauth2/auth";
        String scope = "openid profile email";
        
        try {
            return baseUrl + "?" +
                "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent" +
                "&state=random_state_string";
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Google auth URL", e);
        }
    }
}
