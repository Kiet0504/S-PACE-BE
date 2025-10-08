package com.example.S_PACE.utils;

import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // 1. Lấy token từ header
            String jwt = getJwtFromRequest(request);
            
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // 2. Parse token để lấy userId
                String userId = jwtTokenProvider.getUserIdFromJWT(jwt);
                
                if (userId != null) {
                    // 3. Load user details từ database
                    Optional<User> userOptional = userRepository.findById(java.util.UUID.fromString(userId));
                    
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        
                        // 4. Tạo authorities từ role
                        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);
                        
                        // 5. Tạo authentication object
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                user, 
                                null, 
                                Collections.singletonList(authority)
                            );
                        
                        // 6. Set vào SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        logger.debug("JWT authentication successful for user: {}", user.getEmail());
                    } else {
                        logger.warn("User not found for JWT token with userId: {}", userId);
                    }
                } else {
                    logger.warn("Could not extract userId from JWT token");
                }
            } else if (jwt != null) {
                logger.warn("Invalid JWT token provided");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    // Lấy JWT token từ Authorization header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bỏ "Bearer " prefix
        }
        
        return null;
    }
} 
