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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        logger.info("OAuth2 login successful for user: {}", email);
        logger.info("Google OAuth2 attributes: {}", attributes);

        try {
            // Find or create user
            User user = findOrCreateUser(email, name, picture);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user);

            // Create response
            Map<String, Object> responseData = Map.of(
                "success", true,
                "message", "Google login successful",
                "token", token,
                "tokenType", "Bearer",
                "user", Map.of(
                    "userId", user.getUserId(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "avatar", user.getAvatar(),
                    "status", user.getStatus(),
                    "role", Map.of(
                        "roleId", user.getRole().getRoleId(),
                        "roleName", user.getRole().getRoleName(),
                        "description", user.getRole().getDescription()
                    )
                )
            );

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(responseData));

        } catch (Exception e) {
            logger.error("Error processing OAuth2 login: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Login failed\"}");
        }
    }

    private User findOrCreateUser(String email, String name, String picture) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info if needed
            if (user.getAvatar() == null && picture != null) {
                user.setAvatar(picture);
                userRepository.save(user);
            }
            return user;
        }

        // Create new user
        Role collaboratorRole = roleRepository.findByRoleName("COLLABORATOR")
                .orElseThrow(() -> new RuntimeException("COLLABORATOR role not found"));

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name);
        newUser.setAvatar(picture);
        newUser.setRole(collaboratorRole);
        newUser.setStatus(com.example.S_PACE.enums.UserStatus.ACTIVE);
        // Set a random password for OAuth users (they won't use it)
        newUser.setPasswordHash("OAUTH_USER");

        return userRepository.save(newUser);
    }
}
