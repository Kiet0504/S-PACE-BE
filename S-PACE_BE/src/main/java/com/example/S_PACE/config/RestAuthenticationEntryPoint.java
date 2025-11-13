package com.example.S_PACE.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();
        
        // Check if this is an API request (JSON, XML, or multipart/form-data for file uploads)
        boolean isApiRequest = (acceptHeader != null && (acceptHeader.contains("application/json") || acceptHeader.contains("application/xml")))
                || (contentType != null && (contentType.contains("application/json") || contentType.contains("multipart/form-data")))
                || requestURI.startsWith("/api/");
        
        if (isApiRequest) {
            logger.warn("Unauthorized API request to: {} - returning 401", requestURI);
            
            // Add CORS headers to allow frontend to read the error
            String origin = request.getHeader("Origin");
            if (origin != null) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                response.setHeader("Access-Control-Allow-Headers", "*");
            }
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized - Valid JWT token required\",\"data\":null}");
            response.getWriter().flush();
        } else {
            // For non-API requests, let Spring Security handle the redirect
            logger.warn("Unauthorized web request to: {} - redirecting to login", requestURI);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}

