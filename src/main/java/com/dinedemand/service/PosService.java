package com.dinedemand.service;

import com.dinedemand.db.MenuDAO;
import com.dinedemand.db.OrderDAO;
import com.dinedemand.db.ReportDAO;
import com.dinedemand.model.MenuItem;
import com.dinedemand.model.Order;
import com.dinedemand.model.OrderLine;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core business service orchestrating inventory, order transactions, and analytics.
 */
public class PosService {
    private final MenuDAO menuDAO;
    private final OrderDAO orderDAO;
    private final ReportDAO reportDAO;

    public PosService() {
        this.menuDAO = new MenuDAO();
        this.orderDAO = new OrderDAO();
        this.reportDAO = new ReportDAO();
    }

    public List<MenuItem> getCatalog(String category, String searchQuery) {
        List<MenuItem> items = menuDAO.getMenuItemsByCategory(category);
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String query = searchQuery.trim().toLowerCase();
            return items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(query) ||
                                 i.getCategory().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }
        return items;
    }

    public List<String> getCategories() {
        return menuDAO.getAllCategories();
    }

    public boolean updateStock(int itemId, int newQuantity) {
        return menuDAO.updateStock(itemId, newQuantity);
    }

    public boolean restockItem(int itemId, int addedQuantity) {
        return menuDAO.restockItem(itemId, addedQuantity);
    }

    public boolean updatePrice(int itemId, double newPrice) {
        return menuDAO.updatePrice(itemId, newPrice);
    }

    public boolean addMenuItem(MenuItem item) {
        return menuDAO.addMenuItem(item);
    }

    public String getNextOrderNumber() {
        return orderDAO.getNextOrderNumber();
    }

    public boolean completeOrder(Order order, List<OrderLine> lines) throws SQLException {
        return orderDAO.saveOrder(order, lines);
    }

    public ReportDAO.SalesMetric getDailySales() {
        return reportDAO.getDailySales();
    }

    public ReportDAO.SalesMetric getWeeklySales() {
        return reportDAO.getWeeklySales();
    }

    public ReportDAO.SalesMetric getMonthlySales() {
        return reportDAO.getMonthlySales();
    }

    public List<ReportDAO.TopItem> getTopSellingItems(int limit) {
        return reportDAO.getTopSellingItems(limit);
    }

    public List<ReportDAO.TransactionRecord> getTransactionHistory() {
        return reportDAO.getTransactionHistory();
    }
}
