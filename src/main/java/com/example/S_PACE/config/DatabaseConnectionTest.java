package com.example.S_PACE.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Component
public class DatabaseConnectionTest {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            logger.info("Connected to database: {} {}",
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion());
            logger.info("Database URL: {}", connection.getMetaData().getURL());
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
        }
    }
}
