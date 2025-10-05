package com.example.S_PACE.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentConfig implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> dotenvProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> dotenvProperties.put(entry.getKey(), entry.getValue()));

            if (!dotenvProperties.isEmpty()) {
                environment.getPropertySources().addLast(new MapPropertySource("dotenvProperties", dotenvProperties));
                logger.info("Loaded properties from .env file.");
            } else {
                logger.warn("No .env file found or no properties loaded from .env.");
            }
        } catch (Exception e) {
            logger.warn("Could not load .env file: {}", e.getMessage());
        }
    }
}



