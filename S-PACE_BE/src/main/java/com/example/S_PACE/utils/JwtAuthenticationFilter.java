package com.example.S_PACE.utils;

import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/register",
            "/api/health",
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/v3/api-docs",
            "/v3/api-docs.yaml",
            "/webjars",
            "/static",
            "/public",
            "/error",
            "/favicon.ico",
            "/h2-console",

            "/api/auth/google",
            "/login/oauth2",
            "/oauth2"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processing request: {} {}", method, requestURI);

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                logger.debug("JWT token found, validating...");

                if (tokenProvider.validateToken(jwt)) {
                    String userEmail = tokenProvider.getUserEmailFromJWT(jwt);
                    logger.debug("JWT valid, user email: {}", userEmail);

                    if (userEmail != null) {
                        Optional<User> userOpt = userRepository.findByEmail(userEmail);

                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            logger.debug("User found: {}", user.getEmail());

                            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";
                            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                                    user.getEmail(),
                                    user.getPasswordHash(),
                                    true, true, true, true,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()))
                            );

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.debug("User authenticated successfully: {}", userEmail);
                        } else {
                            logger.warn("User not found in database: {}", userEmail);
                        }
                    }
                } else {
                    logger.warn("JWT token validation failed");
                }
            } else {
                logger.debug("No JWT token found in request");
            }
        } catch (Exception ex) {
            logger.error("Authentication error: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equals(method)) {
            logger.debug("Allowing OPTIONS request: {}", path);
            return true;
        }

        boolean shouldExclude = EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
        if (shouldExclude) {
            logger.debug("Path excluded from JWT authentication: {}", path);
        } else {
            logger.debug("Path requires JWT authentication: {}", path);
        }
        return shouldExclude;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
