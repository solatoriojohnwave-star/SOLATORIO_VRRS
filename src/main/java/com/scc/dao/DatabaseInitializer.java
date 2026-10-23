package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String[] tables = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles    TEXT NOT NULL,
                    status   TEXT NOT NULL DEFAULT 'PENDING'
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS room_types (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    type_name      TEXT NOT NULL UNIQUE,
                    rate_per_hour  REAL NOT NULL,
                    capacity       INTEGER NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS rooms (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_name     TEXT NOT NULL UNIQUE,
                    room_type_id  INTEGER NOT NULL,
                    status        TEXT NOT NULL DEFAULT 'AVAILABLE',
                    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS customers (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id    INTEGER,
                    full_name  TEXT NOT NULL,
                    contact_no TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS reservations (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id  INTEGER NOT NULL,
                    room_id      INTEGER NOT NULL,
                    created_by   INTEGER NOT NULL,
                    start_time   TEXT NOT NULL,
                    end_time     TEXT NOT NULL,
                    total_amount REAL NOT NULL DEFAULT 0,
                    status       TEXT NOT NULL DEFAULT 'RESERVED',
                    FOREIGN KEY (customer_id) REFERENCES customers(id),
                    FOREIGN KEY (room_id)     REFERENCES rooms(id),
                    FOREIGN KEY (created_by)  REFERENCES users(id)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS logs (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id    INTEGER NOT NULL,
                    action     TEXT NOT NULL,
                    log_time   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
            seedDefaultAdmin(conn);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
        }
    }

    // Creates the default admin once. INSERT OR IGNORE skips it if 'admin' already
    // exists (username is UNIQUE), so it is safe to run on every startup.
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO users (username, password, roles, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, "admin123");
            ps.setString(3, "ADMIN");
            ps.setString(4, "APPROVED");
            ps.executeUpdate();
        }
    }
}