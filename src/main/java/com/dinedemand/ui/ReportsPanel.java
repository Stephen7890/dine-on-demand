package com.dinedemand.ui;

import com.dinedemand.db.ReportDAO;
import com.dinedemand.service.PosService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrative reporting panel displaying Daily, Weekly, and Monthly sales metrics,
 * top-performing menu products, and full itemized transaction history.
 */
public class ReportsPanel extends JPanel {
    private final PosService posService;

    // Metric KPI Labels
    private JLabel revenueMetricLabel;
    private JLabel ordersMetricLabel;
    private JLabel avgMetricLabel;
    private JLabel periodIndicatorLabel;

    // Active period selection ("DAILY", "WEEKLY", "MONTHLY")
    private String selectedPeriod = "DAILY";

    // Period buttons
    private JButton dailyBtn;
    private JButton weeklyBtn;
    private JButton monthlyBtn;

    // Tables
    private DefaultTableModel transactionsTableModel;
    private DefaultTableModel topItemsTableModel;

    public ReportsPanel(PosService posService) {
        this.posService = posService;

        setLayout(new BorderLayout(0, 18));
        setBackground(UIUtils.PRIMARY_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        buildUI();
        refreshReportData();
    }

    private void buildUI() {
        // 1. Header & Period Selector
        JPanel topPanel = new JPanel(new BorderLayout(16, 0));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Sales Analytics & Business Reports");
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Audit transactional revenue, customer average tickets, and top menu demand.");
        subtitleLabel.setFont(UIUtils.FONT_SMALL);
        subtitleLabel.setForeground(UIUtils.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Period switcher buttons
        JPanel periodSwitchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        periodSwitchPanel.setOpaque(false);

        dailyBtn = createPeriodButton("Daily (Today)", true);
        weeklyBtn = createPeriodButton("Weekly (7 Days)", false);
        monthlyBtn = createPeriodButton("Monthly (Month)", false);

        dailyBtn.addActionListener(e -> {
            selectedPeriod = "DAILY";
            setPeriodActive(dailyBtn, weeklyBtn, monthlyBtn);
            refreshReportData();
        });

        weeklyBtn.addActionListener(e -> {
            selectedPeriod = "WEEKLY";
            setPeriodActive(weeklyBtn, dailyBtn, monthlyBtn);
            refreshReportData();
        });

        monthlyBtn.addActionListener(e -> {
            selectedPeriod = "MONTHLY";
            setPeriodActive(monthlyBtn, dailyBtn, weeklyBtn);
            refreshReportData();
        });

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshReportData());

        periodSwitchPanel.add(dailyBtn);
        periodSwitchPanel.add(weeklyBtn);
        periodSwitchPanel.add(monthlyBtn);
        periodSwitchPanel.add(refreshBtn);
        topPanel.add(periodSwitchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 2. Center Panel: KPI Cards + Tables
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // KPI Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(0, 100));

        kpiRow.add(createMetricCard("GROSS REVENUE", "₱0.00", UIUtils.ACCENT_PRIMARY, revenueMetricLabel = new JLabel("₱0.00")));
        kpiRow.add(createMetricCard("TOTAL ORDERS COMPLETED", "0", UIUtils.TEXT_PRIMARY, ordersMetricLabel = new JLabel("0")));
        kpiRow.add(createMetricCard("AVERAGE ORDER VALUE", "₱0.00", UIUtils.SUCCESS_GREEN, avgMetricLabel = new JLabel("₱0.00")));

        centerPanel.add(kpiRow, BorderLayout.NORTH);

        // Tables Section: Split into Transactions (Left/Main) and Top Items (Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Transactions Table
        JPanel transPanel = new JPanel(new BorderLayout(0, 8));
        transPanel.setOpaque(false);
        JLabel transTitle = new JLabel("Transaction History Log");
        transTitle.setFont(UIUtils.FONT_SUBTITLE);
        transTitle.setForeground(UIUtils.TEXT_PRIMARY);
        transPanel.add(transTitle, BorderLayout.NORTH);

        String[] transCols = {"Order #", "Date/Time", "Mode", "Items", "Discount", "Payment", "Total"};
        transactionsTableModel = new DefaultTableModel(transCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable transTable = new JTable(transactionsTableModel);
        transTable.setRowHeight(38);
        transTable.setFont(UIUtils.FONT_BODY);
        transTable.setShowGrid(false);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(JLabel.RIGHT);

        transTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        transTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        transTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        transTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        transTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        transTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        transTable.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        transTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        transTable.getColumnModel().getColumn(5).setPreferredWidth(75);
        transTable.getColumnModel().getColumn(5).setCellRenderer(centerRender);
        transTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        transTable.getColumnModel().getColumn(6).setCellRenderer(rightRender);

        JScrollPane transScroll = new JScrollPane(transTable);
        transScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        transScroll.getViewport().setBackground(Color.WHITE);
        transPanel.add(transScroll, BorderLayout.CENTER);
        splitPane.setLeftComponent(transPanel);

        // Top Items Table
        JPanel topItemsPanel = new JPanel(new BorderLayout(0, 8));
        topItemsPanel.setOpaque(false);
        JLabel topTitle = new JLabel("Top-Selling Menu Items");
        topTitle.setFont(UIUtils.FONT_SUBTITLE);
        topTitle.setForeground(UIUtils.TEXT_PRIMARY);
        topItemsPanel.add(topTitle, BorderLayout.NORTH);

        String[] topCols = {"Product", "Units Sold", "Total Revenue"};
        topItemsTableModel = new DefaultTableModel(topCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable topTable = new JTable(topItemsTableModel);
        topTable.setRowHeight(38);
        topTable.setFont(UIUtils.FONT_BODY);
        topTable.setShowGrid(false);

        topTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        topTable.getColumnModel().getColumn(1).setCellRenderer(centerRender);
        topTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        topTable.getColumnModel().getColumn(2).setCellRenderer(rightRender);

        JScrollPane topScroll = new JScrollPane(topTable);
        topScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        topScroll.getViewport().setBackground(Color.WHITE);
        topItemsPanel.add(topScroll, BorderLayout.CENTER);
        splitPane.setRightComponent(topItemsPanel);

        centerPanel.add(splitPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createPeriodButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_SMALL_BOLD);
        btn.setFocusPainted(false);
        updatePeriodStyle(btn, active);
        return btn;
    }

    private void updatePeriodStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(UIUtils.ACCENT_PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        } else {
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(UIUtils.TEXT_SECONDARY);
            btn.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        }
    }

    private void setPeriodActive(JButton active, JButton o1, JButton o2) {
        updatePeriodStyle(active, true);
        updatePeriodStyle(o1, false);
        updatePeriodStyle(o2, false);
    }

    private JPanel createMetricCard(String title, String initialVal, Color accentColor, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        titleLabel.setForeground(UIUtils.TEXT_SECONDARY);

        valueLabel.setFont(UIUtils.FONT_HERO);
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshReportData() {
        try {
            ReportDAO.SalesMetric metric;
            if ("WEEKLY".equals(selectedPeriod)) {
                metric = posService.getWeeklySales();
            } else if ("MONTHLY".equals(selectedPeriod)) {
                metric = posService.getMonthlySales();
            } else {
                metric = posService.getDailySales();
            }

            revenueMetricLabel.setText(metric.getFormattedRevenue());
            ordersMetricLabel.setText(String.valueOf(metric.getTotalOrders()));
            avgMetricLabel.setText(metric.getFormattedAverage());

            // Refresh Transactions Table
            transactionsTableModel.setRowCount(0);
            List<ReportDAO.TransactionRecord> transactions = posService.getTransactionHistory();
            for (ReportDAO.TransactionRecord tx : transactions) {
                transactionsTableModel.addRow(new Object[]{
                        tx.getOrderNumber(),
                        tx.getCreatedAt(),
                        tx.getServiceMode().replace('_', '-'),
                        tx.getItemCount(),
                        tx.getDiscountType(),
                        tx.getPaymentMethod(),
                        tx.getFormattedTotal()
                });
            }

            // Refresh Top Selling Items
            topItemsTableModel.setRowCount(0);
            List<ReportDAO.TopItem> topItems = posService.getTopSellingItems(10);
            for (ReportDAO.TopItem item : topItems) {
                topItemsTableModel.addRow(new Object[]{
                        item.getItemName(),
                        item.getUnitsSold(),
                        item.getFormattedRevenue()
                });
            }
        } catch (Exception e) {
            System.err.println("Error refreshing report data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
