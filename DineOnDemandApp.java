import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class DineOnDemandApp extends JFrame {
    private final OrderManager orderManager;
    private final List<MenuItem> menuItems;
    
    // UI Colors - FlatLaf Compatible Palette
    private static final Color PRIMARY_BG = new Color(245, 246, 250);
    private static final Color SECONDARY_BG = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);

    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);

    // UI Components
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;
    private JComboBox<String> discountCombo;
    private JRadioButton dineInButton;
    private JRadioButton takeOutButton;

    public DineOnDemandApp() {
        // Initialize FlatLaf for a modern look
        try {
            FlatLightLaf.setup();
            // Global UI Customizations
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Table.selectionBackground", new Color(232, 244, 253));
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        this.orderManager = new OrderManager();
        this.menuItems = MenuData.getInitialMenu();
        
        initializeUI();
    }

    private void initializeUI() {
        setTitle("DineOn-Demand POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        getContentPane().setBackground(PRIMARY_BG);
        setLayout(new BorderLayout(20, 20));

        // Header Panel
        add(createHeader(), BorderLayout.NORTH);

        // LEFT: Menu Panel (with padding)
        JPanel menuContainer = new JPanel(new BorderLayout());
        menuContainer.setOpaque(false);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));
        
        JPanel menuPanel = createMenuPanel();
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setBorder(null);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setOpaque(false);
        menuContainer.add(menuScroll, BorderLayout.CENTER);
        
        add(menuContainer, BorderLayout.CENTER);

        // RIGHT: Order Summary Panel
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Footer
        JPanel footer = new JPanel();
        footer.setBackground(SECONDARY_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 221, 225)));
        JLabel footerLabel = new JLabel("DineOn-Demand: A New Era in Meal Accessibility");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerLabel.setForeground(TEXT_LIGHT);
        footer.add(footerLabel);
        add(footer, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SECONDARY_BG);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 221, 225)));

        JLabel logo = new JLabel("  DINEON-DEMAND");
        logo.setFont(TITLE_FONT);
        logo.setForeground(ACCENT_COLOR);
        header.add(logo, BorderLayout.WEST);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        info.setOpaque(false);
        JLabel dateLabel = new JLabel(java.time.LocalDate.now().toString());
        dateLabel.setFont(NORMAL_FONT);
        dateLabel.setForeground(TEXT_LIGHT);
        info.add(dateLabel);
        header.add(info, BorderLayout.EAST);

        return header;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 15, 15));
        panel.setOpaque(false);

        for (MenuItem item : menuItems) {
            panel.add(new MenuItemPanel(item));
        }
        return panel;
    }

    // New Inner Class for Item Cards
    private class MenuItemPanel extends JPanel {
        private final MenuItem item;
        private final JLabel qtyLabel;
        private int selectionQty = 1; // Local selector

        public MenuItemPanel(MenuItem item) {
            this.item = item;
            setLayout(new BorderLayout(8, 8));
            setBackground(SECONDARY_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));

            // Info Area (North)
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setOpaque(false);
            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setFont(SUBTITLE_FONT);
            JLabel priceLabel = new JLabel(String.format("₱%.2f", item.getPrice()));
            priceLabel.setFont(NORMAL_FONT);
            priceLabel.setForeground(ACCENT_COLOR);
            infoPanel.add(nameLabel);
            infoPanel.add(priceLabel);
            add(infoPanel, BorderLayout.NORTH);

            // Controls Area (Center)
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            controls.setOpaque(false);
            
            JButton minusBtn = new JButton("-");
            styleControlBtn(minusBtn, DANGER_COLOR);
            minusBtn.addActionListener(e -> {
                if (selectionQty > 1) {
                    selectionQty--;
                    updateQtyDisplay();
                }
            });

            qtyLabel = new JLabel("1");
            qtyLabel.setFont(SUBTITLE_FONT);
            qtyLabel.setPreferredSize(new Dimension(30, 30));
            qtyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JButton plusBtn = new JButton("+");
            styleControlBtn(plusBtn, SUCCESS_COLOR);
            plusBtn.addActionListener(e -> {
                selectionQty++;
                updateQtyDisplay();
            });

            controls.add(minusBtn);
            controls.add(qtyLabel);
            controls.add(plusBtn);
            add(controls, BorderLayout.CENTER);

            // Add to Order Button (South)
            JButton addBtn = new JButton("Add to Order");
            stylePrimaryButton(addBtn);
            addBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            addBtn.addActionListener(e -> {
                orderManager.addItem(item, selectionQty);
                updateOrderDisplay();
                // Reset to 1 after adding
                selectionQty = 1;
                updateQtyDisplay();
            });
            add(addBtn, BorderLayout.SOUTH);

            // Hover decoration only (no click action on card body)
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(new Color(250, 251, 252));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setBackground(SECONDARY_BG);
                }
            });
        }

        private void styleControlBtn(JButton btn, Color color) {
            btn.setPreferredSize(new Dimension(30, 30));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setForeground(color);
            btn.setBackground(SECONDARY_BG);
            btn.setFocusPainted(false);
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.setMargin(new Insets(0, 0, 0, 0));
        }

        public void updateQtyDisplay() {
            qtyLabel.setText(String.valueOf(selectionQty));
        }
    }

    private void refreshMenuCards() {
        // Find the menu panel container and force repainting of labels
        // For simplicity in this structure, we'll just trigger a global refresh approach
        // though a more efficient way would be using a Registry of cards.
        Component[] components = ((JPanel)((JScrollPane)((JPanel)getContentPane().getComponent(1)).getComponent(0)).getViewport().getView()).getComponents();
        for (Component c : components) {
            if (c instanceof MenuItemPanel) {
                ((MenuItemPanel) c).updateQtyDisplay();
            }
        }
    }

    private JPanel createRightPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setPreferredSize(new Dimension(420, 0));
        container.setBackground(SECONDARY_BG);
        container.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 221, 225)));

        JLabel title = new JLabel("Current Order");
        title.setFont(SUBTITLE_FONT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        container.add(title, BorderLayout.NORTH);

        // Table Styling - Adding "Action" column
        String[] columnNames = {"Item", "Price", "Qty", "Total", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 4; }
        };
        orderTable = new JTable(tableModel);
        styleTable(orderTable);
        
        // Add Button to Table Column
        orderTable.getColumnModel().getColumn(4).setCellRenderer(new TableActionRenderer());
        orderTable.getColumnModel().getColumn(4).setCellEditor(new TableActionEditor());

        JScrollPane tableScroll = new JScrollPane(orderTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        tableScroll.getViewport().setBackground(SECONDARY_BG);
        container.add(tableScroll, BorderLayout.CENTER);

        // Controls
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        controls.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        // Order Mode
        gbc.gridy = 0;
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        modePanel.setOpaque(false);
        dineInButton = new JRadioButton("Dine-In", true);
        takeOutButton = new JRadioButton("Take-Out");
        dineInButton.setOpaque(false);
        takeOutButton.setOpaque(false);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(dineInButton);
        modeGroup.add(takeOutButton);
        modePanel.add(dineInButton);
        modePanel.add(new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 0)));
        modePanel.add(takeOutButton);
        controls.add(modePanel, gbc);

        // Discounts
        gbc.gridy = 1;
        JPanel discPanel = new JPanel(new BorderLayout(10, 0));
        discPanel.setOpaque(false);
        JLabel discIco = new JLabel("Discount Type:");
        discIco.setFont(NORMAL_FONT);
        discPanel.add(discIco, BorderLayout.WEST);
        discountCombo = new JComboBox<>(new String[]{"None", "Senior Citizen (20%)", "PWD (20%)", "Promo Code (10%)"});
        discountCombo.addActionListener(this::applyDiscountShortcut);
        discPanel.add(discountCombo, BorderLayout.CENTER);
        controls.add(discPanel, gbc);

        // Totals
        gbc.gridy = 2;
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 0, 5));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(15, 0, 15, 0)
        ));

        subtotalLabel = createTotalLabel("₱0.00", false);
        discountLabel = createTotalLabel("₱0.00", false);
        totalLabel = createTotalLabel("₱0.00", true);
        
        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(new JLabel("Discount:"));
        totalsPanel.add(discountLabel);
        JLabel totalText = new JLabel("Total Amount:");
        totalText.setFont(SUBTITLE_FONT);
        totalsPanel.add(totalText);
        totalsPanel.add(totalLabel);
        controls.add(totalsPanel, gbc);

        // Actions
        gbc.gridy = 3;
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        actionPanel.setOpaque(false);
        
        JButton clearButton = new JButton("Clear Order");
        styleSecondaryButton(clearButton);
        clearButton.addActionListener(e -> clearOrder());
        
        JButton checkoutButton = new JButton("Checkout");
        stylePrimaryButton(checkoutButton);
        checkoutButton.addActionListener(e -> processCheckout());
        
        actionPanel.add(clearButton);
        actionPanel.add(checkoutButton);
        controls.add(actionPanel, gbc);

        container.add(controls, BorderLayout.SOUTH);
        return container;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(NORMAL_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setBackground(SECONDARY_BG);
        header.setForeground(TEXT_LIGHT);
        header.setFont(SUBTITLE_FONT);
        
        // Alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Qty
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer); // Price
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Total

        // Column Config
        table.getColumnModel().getColumn(0).setPreferredWidth(130); // Item Name
        table.getColumnModel().getColumn(2).setPreferredWidth(40);  // Qty
        table.getColumnModel().getColumn(4).setMinWidth(80);        // Action Column
    }

    private JLabel createTotalLabel(String text, boolean isMain) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(isMain ? TITLE_FONT : SUBTITLE_FONT);
        if (isMain) label.setForeground(ACCENT_COLOR);
        return label;
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(SECONDARY_BG);
        button.setForeground(DANGER_COLOR);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JButton.outlineColor", DANGER_COLOR);
    }

    private void addItemToOrder(MenuItem item) {
        orderManager.addItem(item, 1);
        updateOrderDisplay();
        refreshMenuCards();
    }

    private void updateOrderDisplay() {
        tableModel.setRowCount(0);
        for (OrderLine line : orderManager.getItems()) {
            tableModel.addRow(new Object[]{
                line.getItem().getName(),
                String.format("%.2f", line.getItem().getPrice()),
                line.getQuantity(),
                String.format("%.2f", line.getSubtotal()),
                "Remove" // Used by renderer
            });
        }
        
        subtotalLabel.setText(String.format("₱%.2f", orderManager.calculateSubtotal()));
        discountLabel.setText(String.format("-₱%.2f", orderManager.calculateDiscountAmount()));
        totalLabel.setText(String.format("₱%.2f", orderManager.calculateTotal()));
    }

    private void applyDiscountShortcut(ActionEvent e) {
        String selected = (String) discountCombo.getSelectedItem();
        if (selected.equals("None")) {
            orderManager.setDiscount("None", 0.0);
        } else if (selected.contains("Senior") || selected.contains("PWD")) {
            orderManager.setDiscount(selected, 0.20);
        } else if (selected.contains("Promo")) {
            orderManager.setDiscount("Promo", 0.10);
        }
        updateOrderDisplay();
    }

    private void clearOrder() {
        orderManager.clear();
        discountCombo.setSelectedIndex(0);
        dineInButton.setSelected(true);
        updateOrderDisplay();
        refreshMenuCards();
    }

    private void processCheckout() {
        if (orderManager.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order is empty!", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = {"Cash", "GCash", "Card"};
        int response = JOptionPane.showOptionDialog(this, "Choose Payment Method", "Payment",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (response == -1) return;

        String paymentMethod = options[response];
        String mockDetails = "";

        if (paymentMethod.equals("GCash")) {
            mockDetails = JOptionPane.showInputDialog(this, "Enter GCash Number (e.g., 0912xxxxxxx):");
            if (mockDetails == null || mockDetails.trim().isEmpty()) return;
        } else if (paymentMethod.equals("Card")) {
            mockDetails = JOptionPane.showInputDialog(this, "Enter Card Number (Mock):");
            if (mockDetails == null || mockDetails.trim().isEmpty()) return;
        }

        showReceipt(paymentMethod, mockDetails);
    }

    // --- Table Action Helpers ---
    private class TableActionRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton btn = new JButton("Remove");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btn.setForeground(DANGER_COLOR);
            btn.setFocusPainted(false);
            btn.putClientProperty("JButton.buttonType", "roundRect");
            return btn;
        }
    }

    private class TableActionEditor extends DefaultCellEditor {
        private final JButton btn;
        private int currentRow;

        public TableActionEditor() {
            super(new JCheckBox());
            btn = new JButton("Remove");
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                String itemName = (String) tableModel.getValueAt(currentRow, 0);
                for (MenuItem item : menuItems) {
                    if (item.getName().equals(itemName)) {
                        orderManager.removeItem(item);
                        break;
                    }
                }
                fireEditingStopped();
                updateOrderDisplay();
                refreshMenuCards();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return btn;
        }
    }

    private void showReceipt(String method, String details) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("-----------------------------\n");
        receipt.append("      DINEON-DEMAND         \n");
        receipt.append("-----------------------------\n");
        receipt.append("Mode: ").append(orderManager.isDineIn() ? "DINE-IN" : "TAKE-OUT").append("\n");
        receipt.append("-----------------------------\n");

        for (OrderLine line : orderManager.getItems()) {
            receipt.append(String.format("%-15s x%d  ₱%7.2f\n", 
                line.getItem().getName(), line.getQuantity(), line.getSubtotal()));
        }

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:         ₱%8.2f\n", orderManager.calculateSubtotal()));
        if (!orderManager.getDiscountType().equals("None")) {
            receipt.append(String.format("Discount (%s):   -₱%8.2f\n", 
                orderManager.getDiscountType(), orderManager.calculateDiscountAmount()));
        }
        receipt.append(String.format("TOTAL:            ₱%8.2f\n", orderManager.calculateTotal()));
        receipt.append("-----------------------------\n");
        receipt.append("Payment: ").append(method).append("\n");
        if (!details.isEmpty()) receipt.append("Details: ").append(details).append("\n");
        receipt.append("-----------------------------\n");
        receipt.append("      Thank You for dining!  \n");

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Transaction Successful", JOptionPane.INFORMATION_MESSAGE);
        
        clearOrder();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DineOnDemandApp().setVisible(true);
        });
    }
}
