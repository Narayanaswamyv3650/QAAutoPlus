package com.qaautoplus.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database configuration using HikariCP connection pool for Neon PostgreSQL.
 * Connection is optional — the app runs without a DB (API returns empty results).
 */
public class DatabaseConfig {

    private static HikariDataSource dataSource;
    private static boolean available = false;

    public static void initialize() {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            System.out.println("  DATABASE_URL not set — running without database.");
            return;
        }

        String dbUser = System.getenv("DATABASE_USER");
        if (dbUser == null || dbUser.isBlank()) dbUser = "postgres";

        String dbPassword = System.getenv("DATABASE_PASSWORD");
        if (dbPassword == null || dbPassword.isBlank()) dbPassword = "";

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(300000);
            config.addDataSourceProperty("sslmode", "require");

            dataSource = new HikariDataSource(config);
            createTables();
            available = true;
            System.out.println("  Database connected: " + dbUrl);
        } catch (Exception e) {
            System.err.println("  WARNING: Could not connect to database: " + e.getMessage());
            System.err.println("  App will run without database (articles stored in memory).");
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("Database not configured");
        return dataSource.getConnection();
    }

    private static void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id SERIAL PRIMARY KEY,
                    slug VARCHAR(50) UNIQUE NOT NULL,
                    label VARCHAR(100) NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS articles (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(500) NOT NULL,
                    excerpt VARCHAR(1000) NOT NULL,
                    content TEXT NOT NULL,
                    author VARCHAR(200) NOT NULL,
                    category_slug VARCHAR(50) NOT NULL REFERENCES categories(slug),
                    featured BOOLEAN DEFAULT FALSE,
                    read_time VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                INSERT INTO categories (slug, label) VALUES
                    ('ai', 'AI / ML'),
                    ('rpa', 'RPA'),
                    ('llm', 'LLMs'),
                    ('devops', 'MLOps'),
                    ('ml', 'Computer Vision')
                ON CONFLICT (slug) DO NOTHING
            """);

            System.out.println("  Database tables ready.");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public static void shutdown() {
        if (dataSource != null) dataSource.close();
    }
}
