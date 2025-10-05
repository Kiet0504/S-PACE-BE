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
                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://localhost:5173",
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:3001",
                        "http://127.0.0.1:5173",
                        "https://s-pace.com.vn",
                        "https://www.s-pace.com.vn",
                        "http://s-pace.com.vn",
                        "http://www.s-pace.com.vn"
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

        // Serve default avatars from classpath
        registry.addResourceHandler("/images/avatars/**")
                .addResourceLocations("classpath:/static/images/avatars/")
                .setCachePeriod(86400); // Cache for 24 hours

        // Serve all uploaded files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }
}