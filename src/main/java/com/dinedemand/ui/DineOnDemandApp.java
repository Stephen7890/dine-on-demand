package com.dinedemand.ui;

import com.dinedemand.db.DatabaseManager;
import com.dinedemand.service.PosService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window for DineOn-Demand POS.
 * Hosts the slim left navigation rail and coordinates view transitions via CardLayout.
 */
public class DineOnDemandApp extends JFrame {
    private final PosService posService;

    // View Panels
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private PosTerminalPanel posTerminalPanel;
    private InventoryPanel inventoryPanel;
    private ReportsPanel reportsPanel;

    // Rail Buttons
    private final List<JButton> railButtons = new ArrayList<>();

    public DineOnDemandApp() {
        // Initialize SQLite persistence & FlatLaf Theme
        initSystem();

        this.posService = new PosService();

        setTitle("DineOn-Demand POS - A New Era in Meal Accessibility");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        buildLayout();
    }

    private void initSystem() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.selectionBackground", new Color(241, 245, 249));
            UIManager.put("Table.selectionForeground", UIUtils.TEXT_PRIMARY);
        } catch (Exception e) {
            System.err.println("Could not configure FlatLaf: " + e.getMessage());
        }

        // Initialize SQLite schema and initial data
        DatabaseManager.initializeDatabase();
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.PRIMARY_BG);

        // 1. Slim Left Navigation Rail (~72px)
        root.add(createNavigationRail(), BorderLayout.WEST);

        // 2. Central Card Content Panel
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        posTerminalPanel = new PosTerminalPanel(this, posService, () -> {
            inventoryPanel.refreshTableData();
            reportsPanel.refreshReportData();
        });

        inventoryPanel = new InventoryPanel(posService, () -> {
            posTerminalPanel.refreshCatalog();
        });

        reportsPanel = new ReportsPanel(posService);

        mainContentPanel.add(posTerminalPanel, "POS");
        mainContentPanel.add(inventoryPanel, "INVENTORY");
        mainContentPanel.add(reportsPanel, "REPORTS");

        root.add(mainContentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createNavigationRail() {
        JPanel rail = new JPanel(new BorderLayout());
        rail.setPreferredSize(new Dimension(74, 0));
        rail.setBackground(UIUtils.SIDEBAR_BG);
        rail.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 41, 59)));

        // Top Brand Icon
        JPanel brandIconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        brandIconPanel.setOpaque(false);
        JLabel brandLetter = new JLabel("D");
        brandLetter.setFont(UIUtils.FONT_HERO);
        brandLetter.setForeground(Color.WHITE);
        brandIconPanel.add(brandLetter);
        rail.add(brandIconPanel, BorderLayout.NORTH);

        // Center Navigation Buttons
        JPanel navButtonsPanel = new JPanel();
        navButtonsPanel.setLayout(new BoxLayout(navButtonsPanel, BoxLayout.Y_AXIS));
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        JButton posBtn = createRailButton("🛒", "Terminal", "POS", true);
        JButton invBtn = createRailButton("📦", "Stock", "INVENTORY", false);
        JButton repBtn = createRailButton("📊", "Reports", "REPORTS", false);

        navButtonsPanel.add(posBtn);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(invBtn);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(repBtn);

        rail.add(navButtonsPanel, BorderLayout.CENTER);

        // Bottom Info / Status
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 14));
        bottomPanel.setOpaque(false);
        JLabel vLabel = new JLabel("v1.0");
        vLabel.setFont(UIUtils.FONT_SMALL);
        vLabel.setForeground(UIUtils.TEXT_MUTED);
        bottomPanel.add(vLabel);
        rail.add(bottomPanel, BorderLayout.SOUTH);

        return rail;
    }

    private JButton createRailButton(String icon, String label, String cardName, boolean active) {
        JButton btn = new JButton("<html><center style='font-size:16px;'>" + icon + "<br><span style='font-size:9px;'>" + label + "</span></center></html>");
        btn.setPreferredSize(new Dimension(58, 58));
        btn.setMaximumSize(new Dimension(58, 58));
        btn.setFont(UIUtils.FONT_SMALL_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(label);
        updateRailButtonStyle(btn, active);

        btn.addActionListener(e -> {
            cardLayout.show(mainContentPanel, cardName);
            for (JButton b : railButtons) {
                updateRailButtonStyle(b, b == btn);
            }
            if ("INVENTORY".equals(cardName)) {
                inventoryPanel.refreshTableData();
            } else if ("REPORTS".equals(cardName)) {
                reportsPanel.refreshReportData();
            } else if ("POS".equals(cardName)) {
                posTerminalPanel.refreshCatalog();
            }
        });

        railButtons.add(btn);
        return btn;
    }

    private void updateRailButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(UIUtils.SIDEBAR_ACTIVE);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, UIUtils.ACCENT_PRIMARY),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)
            ));
        } else {
            btn.setBackground(UIUtils.SIDEBAR_BG);
            btn.setForeground(new Color(148, 163, 184));
            btn.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DineOnDemandApp().setVisible(true);
        });
    }
}
