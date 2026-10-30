package com.dinedemand.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database lifecycle, connection retrieval, and schema initialization.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:dine_on_demand.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static synchronized void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Categories Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL
                );
            """);

            // 2. Menu Items Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS menu_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    price REAL NOT NULL,
                    image_path TEXT NOT NULL,
                    stock_quantity INTEGER NOT NULL DEFAULT 20
                );
            """);

            // 3. Orders Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_number TEXT NOT NULL UNIQUE,
                    service_mode TEXT NOT NULL,
                    subtotal REAL NOT NULL,
                    discount_type TEXT NOT NULL,
                    discount_rate REAL NOT NULL,
                    discount_amount REAL NOT NULL,
                    total_amount REAL NOT NULL,
                    payment_method TEXT NOT NULL,
                    payment_reference TEXT,
                    amount_tendered REAL,
                    change_amount REAL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 4. Order Items Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    menu_item_id INTEGER NOT NULL,
                    item_name TEXT NOT NULL,
                    unit_price REAL NOT NULL,
                    quantity INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    FOREIGN KEY(order_id) REFERENCES orders(id)
                );
            """);

            // Seed initial menu data if empty
            MenuDAO menuDAO = new MenuDAO();
            menuDAO.seedInitialDataIfEmpty();

        } catch (SQLException e) {
            System.err.println("Failed to initialize SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
