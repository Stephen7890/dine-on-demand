package com.dinedemand.ui;

import com.dinedemand.model.MenuItem;
import com.dinedemand.service.PosService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Administrative panel for stock inventory management, pricing updates, and catalog expansion.
 */
public class InventoryPanel extends JPanel {
    private final PosService posService;
    private final Runnable onDataChangedCallback;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;

    public InventoryPanel(PosService posService, Runnable onDataChangedCallback) {
        this.posService = posService;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.PRIMARY_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        buildUI();
        refreshTableData();
    }

    private void buildUI() {
        // 1. Header & Actions Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(16, 0));
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Inventory & Menu Management");
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(UIUtils.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Monitor real-time stock levels, adjust retail prices, and manage menu catalog entries.");
        subtitleLabel.setFont(UIUtils.FONT_SMALL);
        subtitleLabel.setForeground(UIUtils.TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Action Buttons Toolbar
        JPanel actionsToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsToolbar.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshTableData());

        JButton restockBtn = new JButton("Restock / Adjust");
        restockBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        restockBtn.setBackground(UIUtils.ACCENT_PRIMARY);
        restockBtn.setForeground(Color.WHITE);
        restockBtn.setFocusPainted(false);
        restockBtn.addActionListener(e -> handleRestockDialog());

        JButton updatePriceBtn = new JButton("Update Price");
        updatePriceBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        updatePriceBtn.setBackground(new Color(241, 245, 249));
        updatePriceBtn.setFocusPainted(false);
        updatePriceBtn.addActionListener(e -> handleUpdatePriceDialog());

        JButton addItemBtn = new JButton("+ Add New Item");
        addItemBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        addItemBtn.setBackground(UIUtils.SIDEBAR_BG);
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.setFocusPainted(false);
        addItemBtn.addActionListener(e -> handleAddNewItemDialog());

        actionsToolbar.add(refreshBtn);
        actionsToolbar.add(restockBtn);
        actionsToolbar.add(updatePriceBtn);
        actionsToolbar.add(addItemBtn);

        topPanel.add(actionsToolbar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. Center: Search Filter Bar & Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Search bar
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBarPanel.setOpaque(false);
        JLabel searchIcon = new JLabel("Search Catalog:");
        searchIcon.setFont(UIUtils.FONT_SMALL_BOLD);
        searchIcon.setForeground(UIUtils.TEXT_SECONDARY);

        searchField = new JTextField(24);
        searchField.setFont(UIUtils.FONT_BODY);
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        searchBarPanel.add(searchIcon);
        searchBarPanel.add(searchField);
        centerPanel.add(searchBarPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Item Name", "Category", "Price", "Stock Quantity", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(44);
        inventoryTable.setFont(UIUtils.FONT_BODY);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.setShowGrid(false);
        inventoryTable.setIntercellSpacing(new Dimension(0, 0));

        rowSorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(rowSorter);

        // Column Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        inventoryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(220);

        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        inventoryTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        inventoryTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        inventoryTable.getColumnModel().getColumn(5).setCellRenderer(new StockStatusRenderer());

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    public void refreshTableData() {
        tableModel.setRowCount(0);
        List<MenuItem> items = posService.getCatalog("All", null);
        for (MenuItem item : items) {
            String status = item.getStockQuantity() == 0 ? "Out of Stock" :
                    (item.getStockQuantity() <= 5 ? "Low Stock" : "In Stock");

            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    String.format("₱%.2f", item.getPrice()),
                    item.getStockQuantity(),
                    status
            });
        }
    }

    private void handleRestockDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table first.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = inventoryTable.convertRowIndexToModel(selectedRow);
        int itemId = (int) tableModel.getValueAt(modelRow, 0);
        String itemName = (String) tableModel.getValueAt(modelRow, 1);
        int currentStock = (int) tableModel.getValueAt(modelRow, 4);

        String[] options = {"Add Units (Restock)", "Set Exact Stock Level", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Current stock for " + itemName + " is " + currentStock + " units.\nChoose an action:",
                "Restock: " + itemName,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == 0) { // Add Units
            String input = JOptionPane.showInputDialog(this, "Enter quantity to add (e.g. 20):", "Add Stock", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int addQty = Integer.parseInt(input.trim());
                    if (addQty > 0) {
                        posService.restockItem(itemId, addQty);
                        refreshTableData();
                        if (onDataChangedCallback != null) onDataChangedCallback.run();
                        JOptionPane.showMessageDialog(this, "Successfully added " + addQty + " units to " + itemName + ".");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (choice == 1) { // Set exact
            String input = JOptionPane.showInputDialog(this, "Enter new total stock count:", "Set Stock", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int newStock = Integer.parseInt(input.trim());
                    if (newStock >= 0) {
                        posService.updateStock(itemId, newStock);
                        refreshTableData();
                        if (onDataChangedCallback != null) onDataChangedCallback.run();
                        JOptionPane.showMessageDialog(this, "Stock for " + itemName + " updated to " + newStock + ".");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleUpdatePriceDialog() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table first.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = inventoryTable.convertRowIndexToModel(selectedRow);
        int itemId = (int) tableModel.getValueAt(modelRow, 0);
        String itemName = (String) tableModel.getValueAt(modelRow, 1);
        String currentPriceStr = (String) tableModel.getValueAt(modelRow, 3);

        String input = JOptionPane.showInputDialog(this,
                "Enter new retail price in ₱ (Current: " + currentPriceStr + "):",
                "Update Price: " + itemName, JOptionPane.PLAIN_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double newPrice = Double.parseDouble(input.replace("₱", "").trim());
                if (newPrice > 0) {
                    posService.updatePrice(itemId, newPrice);
                    refreshTableData();
                    if (onDataChangedCallback != null) onDataChangedCallback.run();
                    JOptionPane.showMessageDialog(this, "Price updated successfully.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAddNewItemDialog() {
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 12));
        JTextField nameField = new JTextField();
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Mains", "Sides", "Drinks", "Desserts"});
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField("25");
        JTextField imageField = new JTextField("/images/classic-burger.png");

        form.add(new JLabel("Item Name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(categoryCombo);
        form.add(new JLabel("Price (₱):"));
        form.add(priceField);
        form.add(new JLabel("Initial Stock:"));
        form.add(stockField);
        form.add(new JLabel("Image Resource Path:"));
        form.add(imageField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Menu Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String cat = (String) categoryCombo.getSelectedItem();
            String priceText = priceField.getText().trim();
            String stockText = stockField.getText().trim();
            String img = imageField.getText().trim();

            if (name.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceText);
                int stock = Integer.parseInt(stockText);
                MenuItem newItem = new MenuItem(name, cat, price, img, stock);
                boolean success = posService.addMenuItem(newItem);
                if (success) {
                    refreshTableData();
                    if (onDataChangedCallback != null) onDataChangedCallback.run();
                    JOptionPane.showMessageDialog(this, "Item '" + name + "' added successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save item.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number entered for price or stock.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class StockStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(CENTER);
            label.setFont(UIUtils.FONT_SMALL_BOLD);

            String status = value != null ? value.toString() : "";
            if ("In Stock".equalsIgnoreCase(status)) {
                label.setForeground(UIUtils.SUCCESS_GREEN);
            } else if ("Low Stock".equalsIgnoreCase(status)) {
                label.setForeground(UIUtils.WARNING_AMBER);
            } else {
                label.setForeground(UIUtils.DANGER_RED);
            }
            return label;
        }
    }
}
