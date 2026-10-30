package com.dinedemand.db;

import com.dinedemand.model.MenuItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for catalog products and stock inventory.
 */
public class MenuDAO {

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, category, price, image_path, stock_quantity FROM menu_items ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("image_path"),
                        rs.getInt("stock_quantity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching menu items: " + e.getMessage());
        }
        return items;
    }

    public List<MenuItem> getMenuItemsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("All") || category.equalsIgnoreCase("All Items")) {
            return getAllMenuItems();
        }
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT id, name, category, price, image_path, stock_quantity FROM menu_items WHERE category = ? ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new MenuItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getString("image_path"),
                            rs.getInt("stock_quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching items by category: " + e.getMessage());
        }
        return items;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
        }
        return categories;
    }

    public boolean updateStock(int itemId, int newQuantity) {
        String sql = "UPDATE menu_items SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Math.max(0, newQuantity));
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    public boolean restockItem(int itemId, int addedQuantity) {
        String sql = "UPDATE menu_items SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Math.max(0, addedQuantity));
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error restocking item: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePrice(int itemId, double newPrice) {
        String sql = "UPDATE menu_items SET price = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, Math.max(0.0, newPrice));
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating price: " + e.getMessage());
            return false;
        }
    }

    public boolean addMenuItem(MenuItem item) {
        String sqlCat = "INSERT OR IGNORE INTO categories (name) VALUES (?)";
        String sqlItem = "INSERT INTO menu_items (name, category, price, image_path, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pCat = conn.prepareStatement(sqlCat);
                 PreparedStatement pItem = conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {

                pCat.setString(1, item.getCategory());
                pCat.executeUpdate();

                pItem.setString(1, item.getName());
                pItem.setString(2, item.getCategory());
                pItem.setDouble(3, item.getPrice());
                pItem.setString(4, item.getImagePath());
                pItem.setInt(5, item.getStockQuantity());
                int affected = pItem.executeUpdate();

                if (affected > 0) {
                    try (ResultSet gk = pItem.getGeneratedKeys()) {
                        if (gk.next()) {
                            item.setId(gk.getInt(1));
                        }
                    }
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error adding menu item: " + e.getMessage());
            return false;
        }
    }

    public void seedInitialDataIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM menu_items";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding initial DineOn-Demand menu items and categories...");

                // Initial Categories
                String[] categories = {"Mains", "Sides", "Drinks", "Desserts"};
                try (PreparedStatement pCat = conn.prepareStatement("INSERT OR IGNORE INTO categories (name) VALUES (?)")) {
                    for (String cat : categories) {
                        pCat.setString(1, cat);
                        pCat.executeUpdate();
                    }
                }

                // Initial 11 Products with Default Stock = 25
                Object[][] initialItems = {
                        {"Classic Burger", "Mains", 150.00, "/images/classic-burger.png", 25},
                        {"Cheeseburger", "Mains", 180.00, "/images/cheese-burger.png", 25},
                        {"Chicken Burger", "Mains", 170.00, "/images/chicken-burger.png", 25},
                        {"French Fries", "Sides", 60.00, "/images/fries.png", 25},
                        {"Onion Rings", "Sides", 75.00, "/images/onion-rings.png", 25},
                        {"Coleslaw", "Sides", 45.00, "/images/coleslaw.png", 25},
                        {"Coke 500ml", "Drinks", 40.00, "/images/coke.png", 25},
                        {"Iced Tea", "Drinks", 35.00, "/images/iced-tea.png", 25},
                        {"Mineral Water", "Drinks", 20.00, "/images/water.png", 25},
                        {"Choco Sundae", "Desserts", 55.00, "/images/choco-sundae.png", 25},
                        {"Apple Pie", "Desserts", 65.00, "/images/apple-pie.png", 25}
                };

                String insertSql = "INSERT INTO menu_items (name, category, price, image_path, stock_quantity) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pItem = conn.prepareStatement(insertSql)) {
                    for (Object[] row : initialItems) {
                        pItem.setString(1, (String) row[0]);
                        pItem.setString(2, (String) row[1]);
                        pItem.setDouble(3, (Double) row[2]);
                        pItem.setString(4, (String) row[3]);
                        pItem.setInt(5, (Integer) row[4]);
                        pItem.executeUpdate();
                    }
                }
                System.out.println("Initial menu seeding complete.");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding menu data: " + e.getMessage());
        }
    }
}
