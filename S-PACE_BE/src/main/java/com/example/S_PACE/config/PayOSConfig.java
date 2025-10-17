package com.example.S_PACE.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

/**
 * PayOS configuration that wires credentials from application.yml (or environment variables)
 * instead of hardcoding them in source code.
 *
 * Properties expected:
 *  - payos.client-id
 *  - payos.api-key
 *  - payos.checksum-key
 */
@Configuration
public class PayOSConfig {

    private static final Logger logger = LoggerFactory.getLogger(PayOSConfig.class);

    @Value("${payos.client-id:}")
    private String clientId;

    @Value("${payos.api-key:}")
    private String apiKey;

    @Value("${payos.checksum-key:}")
    private String checksumKey;

    @Bean
    public PayOS payOS() {
        logger.info("Initializing PayOS with clientId: {}", clientId);
        logger.info("API Key length: {}", apiKey != null ? apiKey.length() : 0);
        logger.info("Checksum Key length: {}", checksumKey != null ? checksumKey.length() : 0);

        // It's better to fail-fast if any of the required values are missing
        if (isBlank(clientId) || isBlank(apiKey) || isBlank(checksumKey)) {
            throw new IllegalStateException("PayOS credentials are not configured. Please set PAYOS_CLIENT_ID, PAYOS_API_KEY, and PAYOS_CHECKSUM_KEY environment variables or define them in application.yml");
        }
        return new PayOS(clientId, apiKey, checksumKey);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

