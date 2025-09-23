package com.example.S_PACE.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j


@Configuration
public class DatabaseConfig {



    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;


    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                log.info("Starting database migration process...");

                ensureDatabaseExists();

                var info = flyway.info();
                var current = info.current();

                if (current == null) {
                    log.info("Fresh database detected. Running baseline...");
                    flyway.baseline();
                } else {
                    log.info("Found existing schema version: {}", current.getVersion());
                }

                MigrateResult result = flyway.migrate();

                if (result.migrationsExecuted > 0) {
                    log.info("Successfully executed {} migration(s)", result.migrationsExecuted);
                    if (result.migrations != null && !result.migrations.isEmpty()) {
                        result.migrations.forEach(m ->
                                log.info("  - [{}] {}", m.version, m.description)
                        );
                    }
                    log.info("Database updated to version: {}", result.targetSchemaVersion);
                } else {
                    log.info("Database is already up to date");
                }

                logMigrationInfo(flyway);

            } catch (Exception e) {
                log.error("Migration failed: {}", e.getMessage(), e);
                throw new RuntimeException("Database migration failed", e);
            }
        };
    }

    private void ensureDatabaseExists() {
        String dbName = extractDatabaseName(databaseUrl);
        if (dbName == null) {
            log.error("Could not extract database name from URL: {}", databaseUrl);
            throw new RuntimeException("Could not extract database name from URL");
        }

        String serverUrl = databaseUrl.substring(0, databaseUrl.lastIndexOf("/")) + "/postgres";

        try {
            log.info("Checking if database '{}' exists...", dbName);

            Flyway tempFlyway = Flyway.configure()
                    .dataSource(serverUrl, username, password)
                    .load();

            try (Connection conn = tempFlyway.getConfiguration().getDataSource().getConnection()) {
                boolean dbExists = false;
                try (var ps = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                    ps.setString(1, dbName);
                    var rs = ps.executeQuery();
                    if (rs.next()) {
                        dbExists = true;
                    }
                }

                if (!dbExists) {
                    log.info("Database '{}' not found. Creating...", dbName);
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE " + dbName);
                        log.info("Database '{}' created successfully", dbName);
                    }
                } else {
                    log.info("Database '{}' already exists", dbName);
                }
            }

        } catch (SQLException e) {
            log.warn("Could not auto-create database: {}", e.getMessage());
            log.info("Please ensure database '{}' exists manually", dbName);
        }
    }

    private String extractDatabaseName(String url) {
        try {
            String dbPart = url.substring(url.lastIndexOf("/") + 1);
            int queryParamIndex = dbPart.indexOf("?");
            if (queryParamIndex != -1) {
                return dbPart.substring(0, queryParamIndex);
            }
            return dbPart;
        } catch (Exception e) {
            log.error("Could not extract database name from url: {}", url, e);
            return null;
        }
    }

    private void logMigrationInfo(Flyway flyway) {
        try {
            var info = flyway.info();
            log.info("Migration Summary:");
            log.info("Total migrations: {}", info.all().length);

            var pending = info.pending();
            if (pending.length > 0) {
                log.info("  Pending migrations: {}", pending.length);
            }

            var current = info.current();
            if (current != null) {
                log.info("Current version: {}", current.getVersion());
                log.info("Current description: {}", current.getDescription());
            }

        } catch (Exception e) {
            log.warn("Could not retrieve migration info: {}", e.getMessage());
        }
    }
}
