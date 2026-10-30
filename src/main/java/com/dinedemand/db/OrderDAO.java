package com.dinedemand.db;

import com.dinedemand.model.Order;
import com.dinedemand.model.OrderLine;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Data Access Object for persisting completed transactions and deducting inventory atomically.
 */
public class OrderDAO {

    public synchronized String getNextOrderNumber() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = rs.next() ? rs.getInt(1) : 0;
            return String.format("ORD-%04d", 1001 + count);
        } catch (SQLException e) {
            return "ORD-" + System.currentTimeMillis() % 10000;
        }
    }

    /**
     * Atomically saves an order, persists line items, and deducts stock from inventory.
     * Enforces database transaction rollback if any step fails.
     */
    public boolean saveOrder(Order order, List<OrderLine> lines) throws SQLException {
        String orderSql = """
            INSERT INTO orders (
                order_number, service_mode, subtotal, discount_type, discount_rate,
                discount_amount, total_amount, payment_method, payment_reference,
                amount_tendered, change_amount, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String itemSql = """
            INSERT INTO order_items (
                order_id, menu_item_id, item_name, unit_price, quantity, subtotal
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        String stockSql = """
            UPDATE menu_items 
            SET stock_quantity = stock_quantity - ? 
            WHERE id = ? AND stock_quantity >= ?
        """;

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Begin Atomic Transaction

            int orderId;
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            order.setCreatedAt(now);

            // 1. Insert Order Header
            try (PreparedStatement pstmtOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtOrder.setString(1, order.getOrderNumber());
                pstmtOrder.setString(2, order.getServiceMode());
                pstmtOrder.setDouble(3, order.getSubtotal());
                pstmtOrder.setString(4, order.getDiscountType());
                pstmtOrder.setDouble(5, order.getDiscountRate());
                pstmtOrder.setDouble(6, order.getDiscountAmount());
                pstmtOrder.setDouble(7, order.getTotalAmount());
                pstmtOrder.setString(8, order.getPaymentMethod());
                pstmtOrder.setString(9, order.getPaymentReference());
                if (order.getAmountTendered() != null) {
                    pstmtOrder.setDouble(10, order.getAmountTendered());
                } else {
                    pstmtOrder.setNull(10, Types.REAL);
                }
                if (order.getChangeAmount() != null) {
                    pstmtOrder.setDouble(11, order.getChangeAmount());
                } else {
                    pstmtOrder.setNull(11, Types.REAL);
                }
                pstmtOrder.setString(12, now);

                pstmtOrder.executeUpdate();
                try (ResultSet gk = pstmtOrder.getGeneratedKeys()) {
                    if (gk.next()) {
                        orderId = gk.getInt(1);
                        order.setId(orderId);
                    } else {
                        conn.rollback();
                        throw new SQLException("Failed to retrieve generated order ID.");
                    }
                }
            }

            // 2. Insert Order Items & Deduct Stock
            try (PreparedStatement pstmtItem = conn.prepareStatement(itemSql);
                 PreparedStatement pstmtStock = conn.prepareStatement(stockSql)) {

                for (OrderLine line : lines) {
                    int menuItemId = line.getItem().getId();
                    int qty = line.getQuantity();

                    // Insert line item
                    pstmtItem.setInt(1, orderId);
                    pstmtItem.setInt(2, menuItemId);
                    pstmtItem.setString(3, line.getItem().getName());
                    pstmtItem.setDouble(4, line.getItem().getPrice());
                    pstmtItem.setInt(5, qty);
                    pstmtItem.setDouble(6, line.getSubtotal());
                    pstmtItem.executeUpdate();

                    // Deduct stock safely with row count check
                    pstmtStock.setInt(1, qty);
                    pstmtStock.setInt(2, menuItemId);
                    pstmtStock.setInt(3, qty);
                    int updated = pstmtStock.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        throw new SQLException("Insufficient stock available for item: " + line.getItem().getName());
                    }
                }
            }

            conn.commit(); // Commit Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    System.err.println("Rollback failed: " + rbEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
        }
    }
}
