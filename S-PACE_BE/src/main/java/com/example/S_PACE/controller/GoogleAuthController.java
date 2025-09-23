package com.example.S_PACE.controller;

import com.example.S_PACE.dto.response.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Google Authentication", description = "Google OAuth authentication APIs")
public class GoogleAuthController {

    @GetMapping("/google")
    @Operation(summary = "Initiate Google OAuth login", description = "Redirect to Google OAuth login page. Note: This endpoint requires browser interaction and cannot be tested directly in Swagger UI due to CORS restrictions. Use a browser to test the OAuth flow.")
    public void googleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
}
