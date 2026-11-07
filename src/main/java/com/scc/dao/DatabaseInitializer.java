package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Database connection verified successfully.");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
}