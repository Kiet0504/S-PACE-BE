package com.example.S_PACE.config.oauth2;

import com.example.S_PACE.pojo.Role;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.RoleRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.utils.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public OAuth2SuccessHandler(UserRepository userRepository,
                               RoleRepository roleRepository,
                               JwtTokenProvider jwtTokenProvider,
                               ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Extract Google user information
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");

        logger.info("OAuth2 login successful for user: {}", email);
        logger.info("Google OAuth2 attributes: {}", attributes);

        try {
            // Find or create user with Google data
            User user = findOrCreateUser(email, name, picture, givenName, familyName);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user);

            // Build frontend redirect URL with token and user data
            String frontendUrl = buildFrontendRedirectUrl(token, user);
            
            logger.info("Redirecting to frontend: {}", frontendUrl);
            response.sendRedirect(frontendUrl);

        } catch (Exception e) {
            logger.error("Error processing OAuth2 login: {}", e.getMessage(), e);
            
            // Redirect to frontend with error
            String errorUrl = buildErrorRedirectUrl(e.getMessage());
            response.sendRedirect(errorUrl);
        }
    }

    private User findOrCreateUser(String email, String name, String picture, String givenName, String familyName) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info with latest Google data
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            if (picture != null && !picture.equals(user.getAvatar())) {
                user.setAvatar(picture);
            }
            userRepository.save(user);
            return user;
        }

        // Create new user with Google data
        Role collaboratorRole = roleRepository.findByRoleName("COLLABORATOR")
                .orElseThrow(() -> new RuntimeException("COLLABORATOR role not found"));

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name != null ? name : (givenName + " " + familyName).trim());
        newUser.setAvatar(picture);
        newUser.setRole(collaboratorRole);
        newUser.setStatus(com.example.S_PACE.enums.UserStatus.ACTIVE);
        // Set a random password for OAuth users (they won't use it)
        newUser.setPasswordHash("OAUTH_USER");

        return userRepository.save(newUser);
    }

    /**
     * Build frontend redirect URL with token and user data
     */
    private String buildFrontendRedirectUrl(String token, User user) throws IOException {
        String baseUrl = "http://localhost:5173/auth/callback";
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8.toString());
        String userJson = URLEncoder.encode(objectMapper.writeValueAsString(user), StandardCharsets.UTF_8.toString());
        
        return String.format("%s?token=%s&user=%s&success=true", baseUrl, encodedToken, userJson);
    }

    /**
     * Build error redirect URL
     */
    private String buildErrorRedirectUrl(String errorMessage) {
        String baseUrl = "http://localhost:5173/auth/callback";
        String error = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        return String.format("%s?error=%s&success=false", baseUrl, error);
    }
}
