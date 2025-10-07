package com.example.S_PACE.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "https://s-pace.com.vn",
                        "https://www.s-pace.com.vn",
                        "https://api.s-pace.com.vn",
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // Additional CORS mapping for all endpoints
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://s-pace.com.vn",
                        "https://www.s-pace.com.vn",
                        "https://api.s-pace.com.vn",
                        "http://api.s-pace.com.vn",
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // CORS for static files
        registry.addMapping("/uploads/**")
                .allowedOrigins("*") // Allow all origins for static files
                .allowedMethods("GET", "OPTIONS")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded avatars
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:uploads/avatars/")
                .setCachePeriod(3600); // Cache for 1 hour

        // Serve uploaded CVs
        registry.addResourceHandler("/uploads/cvs/**")
                .addResourceLocations("file:uploads/cvs/")
                .setCachePeriod(3600);

        // Serve uploaded certificates
        registry.addResourceHandler("/uploads/certificates/**")
                .addResourceLocations("file:uploads/certificates/")
                .setCachePeriod(3600);

        // Default avatars are now handled by frontend - no backend serving needed

        // Serve all uploaded files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }
}