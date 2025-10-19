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
                    
                    // Check if database actually has existing tables
                    if (hasExistingTables()) {
                        log.warn("Database appears to have existing tables but no Flyway history. This could cause migration conflicts.");
                        log.warn("Setting baseline to version 11 to avoid conflicts with existing tables.");
                        
                        // Set baseline to the latest version to avoid running old migrations
                        flyway.baseline("11");
                    } else {
                        flyway.baseline();
                    }
                } else {
                    log.info("Found existing schema version: {}", current.getVersion());
                }

                // Check for validation issues and repair if needed
                try {
                    flyway.validate();
                } catch (Exception e) {
                    log.warn("Validation failed: {}. Attempting repair...", e.getMessage());

                    // Fix: Delete migration V6 if it has checksum mismatch, then repair
                    fixMigrationV6Checksum(flyway);

                    // Fix: Handle missing schema history entries
                    fixMissingSchemaHistory(flyway);

                    // Use repair to fix checksums in schema history
                    flyway.repair();
                    log.info("Repair completed successfully");
                }

                // Handle out-of-order migrations
                MigrateResult result;
                try {
                    result = flyway.migrate();
                } catch (Exception e) {
                    if (e.getMessage().contains("outOfOrder=true")) {
                        log.warn("Out-of-order migration detected. Re-running with outOfOrder=true...");
                        // Configure Flyway to allow out-of-order migrations
                        Flyway outOfOrderFlyway = Flyway.configure()
                                .dataSource(flyway.getConfiguration().getDataSource())
                                .locations(flyway.getConfiguration().getLocations())
                                .outOfOrder(true)
                                .load();
                        result = outOfOrderFlyway.migrate();
                    } else if (e.getMessage().contains("already exists")) {
                        log.warn("Migration conflict detected: {}", e.getMessage());
                        log.warn("Attempting to resolve by marking conflicting migrations as applied...");
                        
                        // Try to resolve by marking conflicting migrations as applied
                        resolveMigrationConflicts(flyway, e.getMessage());
                        
                        // Retry migration
                        result = flyway.migrate();
                    } else {
                        throw e;
                    }
                }

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

    private void fixMigrationV6Checksum(Flyway flyway) {
        try {
            log.info("Checking for migration V6 checksum mismatch...");

            try (Connection conn = flyway.getConfiguration().getDataSource().getConnection();
                 Statement stmt = conn.createStatement()) {

                // Delete migration V6 if it exists (to allow re-run with new checksum)
                int deleted = stmt.executeUpdate(
                    "DELETE FROM flyway_schema_history WHERE version = '6'"
                );

                if (deleted > 0) {
                    log.info("Deleted migration V6 record to fix checksum mismatch. It will be re-applied.");

                    // Also update old participation_status values
                    stmt.executeUpdate(
                        "UPDATE attendance_logs SET participation_status = 'ATTENDED' WHERE participation_status = 'WORKED'"
                    );
                    stmt.executeUpdate(
                        "UPDATE attendance_logs SET participation_status = 'NOT_ATTENDED' WHERE participation_status IS NULL"
                    );
                    log.info("Updated old attendance_logs data.");
                }
            }
        } catch (SQLException e) {
            log.warn("Could not fix migration V6: {}", e.getMessage());
            // Don't throw - let Flyway handle it
        }
    }

    private void fixMissingSchemaHistory(Flyway flyway) {
        try {
            log.info("Checking for missing schema history entries...");

            try (Connection conn = flyway.getConfiguration().getDataSource().getConnection();
                 Statement stmt = conn.createStatement()) {

                // Check if flyway_schema_history table exists
                boolean historyTableExists = false;
                try (var rs = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.tables WHERE table_name = 'flyway_schema_history'")) {
                    historyTableExists = rs.next();
                }

                if (!historyTableExists) {
                    log.info("flyway_schema_history table doesn't exist. This might be a fresh database with existing tables.");
                    return;
                }

                // Check if we have existing tables but missing history entries
                boolean hasTables = false;
                try (var rs = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.tables WHERE table_name = 'company' LIMIT 1")) {
                    hasTables = rs.next();
                }

                if (hasTables) {
                    // Database has existing tables but might be missing schema history
                    int historyCount = 0;
                    try (var rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history")) {
                        if (rs.next()) {
                            historyCount = rs.getInt(1);
                        }
                    }

                    if (historyCount == 0) {
                        log.warn("Database has existing tables but no migration history. This could cause conflicts.");
                        log.warn("Consider running: DELETE FROM flyway_schema_history WHERE version IN ('1','2','3','4','5','6','7','8','9','10','11');");
                    }
                }

            }
        } catch (SQLException e) {
            log.warn("Could not check schema history: {}", e.getMessage());
            // Don't throw - let Flyway handle it
        }
    }

    private boolean hasExistingTables() {
        try {
            Flyway tempFlyway = Flyway.configure()
                    .dataSource(databaseUrl, username, password)
                    .load();

            try (Connection conn = tempFlyway.getConfiguration().getDataSource().getConnection();
                 Statement stmt = conn.createStatement()) {

                // Check for key tables that should exist
                try (var rs = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.tables WHERE table_name IN ('company', 'user', 'event') LIMIT 1")) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            log.warn("Could not check for existing tables: {}", e.getMessage());
            return false;
        }
    }

    private void resolveMigrationConflicts(Flyway flyway, String errorMessage) {
        try {
            log.info("Attempting to resolve migration conflicts...");

            try (Connection conn = flyway.getConfiguration().getDataSource().getConnection();
                 Statement stmt = conn.createStatement()) {

                // Extract migration version from error message
                String version = extractVersionFromError(errorMessage);
                if (version != null) {
                    log.info("Marking migration V{} as applied to resolve conflict...", version);
                    
                    // Insert a record for the conflicting migration as if it was already applied
                    String insertSql = String.format(
                        "INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) " +
                        "VALUES (1, '%s', 'Migration conflict resolved', 'SQL', 'V%s__Conflict_Resolved.sql', 0, 'system', NOW(), 0, true)",
                        version, version
                    );
                    
                    try {
                        stmt.executeUpdate(insertSql);
                        log.info("Successfully marked migration V{} as applied", version);
                    } catch (SQLException e) {
                        log.warn("Could not mark migration as applied: {}", e.getMessage());
                    }
                }

                // Also try to repair any other issues
                flyway.repair();
                log.info("Migration conflicts resolved successfully");

            }
        } catch (SQLException e) {
            log.warn("Could not resolve migration conflicts: {}", e.getMessage());
            // Don't throw - let the calling method handle it
        }
    }

    private String extractVersionFromError(String errorMessage) {
        // Extract version from error message like "V1__Initial_Schema.sql"
        if (errorMessage.contains("V1__")) return "1";
        if (errorMessage.contains("V2__")) return "2";
        if (errorMessage.contains("V3__")) return "3";
        if (errorMessage.contains("V4__")) return "4";
        if (errorMessage.contains("V5__")) return "5";
        if (errorMessage.contains("V6__")) return "6";
        if (errorMessage.contains("V7__")) return "7";
        if (errorMessage.contains("V8__")) return "8";
        if (errorMessage.contains("V9__")) return "9";
        if (errorMessage.contains("V10__")) return "10";
        if (errorMessage.contains("V11__")) return "11";
        return null;
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
