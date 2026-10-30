package com.dinedemand.db;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for sales analytics, KPI reporting, and transaction history.
 */
public class ReportDAO {

    public static class SalesMetric {
        private final String period;
        private final double totalRevenue;
        private final int totalOrders;
        private final double averageOrderValue;

        public SalesMetric(String period, double totalRevenue, int totalOrders, double averageOrderValue) {
            this.period = period;
            this.totalRevenue = BigDecimal.valueOf(totalRevenue).setScale(2, RoundingMode.HALF_UP).doubleValue();
            this.totalOrders = totalOrders;
            this.averageOrderValue = BigDecimal.valueOf(averageOrderValue).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }

        public String getPeriod() { return period; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getTotalOrders() { return totalOrders; }
        public double getAverageOrderValue() { return averageOrderValue; }

        public String getFormattedRevenue() { return String.format("₱%,.2f", totalRevenue); }
        public String getFormattedAverage() { return String.format("₱%,.2f", averageOrderValue); }
    }

    public static class TopItem {
        private final String itemName;
        private final int unitsSold;
        private final double revenue;

        public TopItem(String itemName, int unitsSold, double revenue) {
            this.itemName = itemName;
            this.unitsSold = unitsSold;
            this.revenue = BigDecimal.valueOf(revenue).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }

        public String getItemName() { return itemName; }
        public int getUnitsSold() { return unitsSold; }
        public double getRevenue() { return revenue; }
        public String getFormattedRevenue() { return String.format("₱%,.2f", revenue); }
    }

    public static class TransactionRecord {
        private final int id;
        private final String orderNumber;
        private final String serviceMode;
        private final int itemCount;
        private final double totalAmount;
        private final String discountType;
        private final String paymentMethod;
        private final String paymentReference;
        private final String createdAt;

        public TransactionRecord(int id, String orderNumber, String serviceMode, int itemCount,
                                 double totalAmount, String discountType, String paymentMethod,
                                 String paymentReference, String createdAt) {
            this.id = id;
            this.orderNumber = orderNumber;
            this.serviceMode = serviceMode;
            this.itemCount = itemCount;
            this.totalAmount = BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
            this.discountType = discountType;
            this.paymentMethod = paymentMethod;
            this.paymentReference = paymentReference != null ? paymentReference : "-";
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public String getOrderNumber() { return orderNumber; }
        public String getServiceMode() { return serviceMode; }
        public int getItemCount() { return itemCount; }
        public double getTotalAmount() { return totalAmount; }
        public String getFormattedTotal() { return String.format("₱%,.2f", totalAmount); }
        public String getDiscountType() { return discountType; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getPaymentReference() { return paymentReference; }
        public String getCreatedAt() { return createdAt; }
    }

    public SalesMetric getDailySales() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sql = """
            SELECT COUNT(*), COALESCE(SUM(total_amount), 0.0), COALESCE(AVG(total_amount), 0.0) 
            FROM orders 
            WHERE DATE(created_at) = DATE(?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new SalesMetric("Today (" + today + ")", rs.getDouble(2), rs.getInt(1), rs.getDouble(3));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating daily sales: " + e.getMessage());
        }
        return new SalesMetric("Today", 0.0, 0, 0.0);
    }

    public SalesMetric getWeeklySales() {
        String sql = """
            SELECT COUNT(*), COALESCE(SUM(total_amount), 0.0), COALESCE(AVG(total_amount), 0.0) 
            FROM orders 
            WHERE DATE(created_at) >= DATE('now', '-7 days')
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new SalesMetric("Last 7 Days", rs.getDouble(2), rs.getInt(1), rs.getDouble(3));
            }
        } catch (SQLException e) {
            System.err.println("Error calculating weekly sales: " + e.getMessage());
        }
        return new SalesMetric("Last 7 Days", 0.0, 0, 0.0);
    }

    public SalesMetric getMonthlySales() {
        String sql = """
            SELECT COUNT(*), COALESCE(SUM(total_amount), 0.0), COALESCE(AVG(total_amount), 0.0) 
            FROM orders 
            WHERE strftime('%Y-%m', created_at) = strftime('%Y-%m', 'now')
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new SalesMetric("This Month", rs.getDouble(2), rs.getInt(1), rs.getDouble(3));
            }
        } catch (SQLException e) {
            System.err.println("Error calculating monthly sales: " + e.getMessage());
        }
        return new SalesMetric("This Month", 0.0, 0, 0.0);
    }

    public List<TopItem> getTopSellingItems(int limit) {
        List<TopItem> list = new ArrayList<>();
        String sql = """
            SELECT item_name, SUM(quantity) as total_qty, SUM(subtotal) as total_rev 
            FROM order_items 
            GROUP BY item_name 
            ORDER BY total_qty DESC 
            LIMIT ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopItem(
                            rs.getString("item_name"),
                            rs.getInt("total_qty"),
                            rs.getDouble("total_rev")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching top items: " + e.getMessage());
        }
        return list;
    }

    public List<TransactionRecord> getTransactionHistory() {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = """
            SELECT o.id, o.order_number, o.service_mode, 
                   COALESCE((SELECT SUM(quantity) FROM order_items WHERE order_id = o.id), 0) as item_count,
                   o.total_amount, o.discount_type, o.payment_method, o.payment_reference, o.created_at 
            FROM orders o 
            ORDER BY o.id DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TransactionRecord(
                        rs.getInt("id"),
                        rs.getString("order_number"),
                        rs.getString("service_mode"),
                        rs.getInt("item_count"),
                        rs.getDouble("total_amount"),
                        rs.getString("discount_type"),
                        rs.getString("payment_method"),
                        rs.getString("payment_reference"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transaction history: " + e.getMessage());
        }
        return list;
    }
}
