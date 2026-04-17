import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class DineOnDemandApp extends JFrame {
    private final OrderManager orderManager;
    private final List<MenuItem> menuItems;
    
    // UI Colors - Black & White Theme
    private static final Color PRIMARY_BG = Color.WHITE;
    private static final Color SECONDARY_BG = Color.WHITE;
    private static final Color ACCENT_COLOR = Color.BLACK;
    private static final Color SUCCESS_COLOR = Color.BLACK;
    private static final Color DANGER_COLOR = new Color(240, 240, 240);
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Color TEXT_LIGHT = new Color(150, 150, 150);
    private static final Color BORDER_LIGHT = new Color(240, 240, 240);

    // Fonts - Geist Custom Font
    private static Font GEIST_BOLD;
    private static Font GEIST_MEDIUM;
    private static Font GEIST_REGULAR;
    
    private static Font HERO_FONT;
    private static Font TITLE_FONT;
    private static Font SUBTITLE_FONT;
    private static Font NORMAL_FONT;
    private static Font BUTTON_FONT;

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
        loadCustomFonts();
        // Initialize FlatLaf for a modern look
        try {
            FlatLightLaf.setup();
            // Global UI Customizations
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("Table.selectionBackground", new Color(245, 245, 245));
            UIManager.put("Table.selectionForeground", Color.BLACK);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 8);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        this.orderManager = new OrderManager();
        this.menuItems = MenuData.getInitialMenu();
        initializeUI();
    }

    private void loadCustomFonts() {
        try {
            GEIST_BOLD = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Geist-Bold.ttf"));
            GEIST_MEDIUM = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Geist-Medium.ttf"));
            GEIST_REGULAR = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Geist-Regular.ttf"));
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(GEIST_BOLD);
            ge.registerFont(GEIST_MEDIUM);
            ge.registerFont(GEIST_REGULAR);

            HERO_FONT = GEIST_BOLD.deriveFont(32f);
            TITLE_FONT = GEIST_BOLD.deriveFont(24f);
            SUBTITLE_FONT = GEIST_BOLD.deriveFont(16f);
            NORMAL_FONT = GEIST_REGULAR.deriveFont(14f);
            BUTTON_FONT = GEIST_MEDIUM.deriveFont(13f);
        } catch (Exception e) {
            System.err.println("Could not load Geist fonts, falling back to Segoe UI");
            HERO_FONT = new Font("Segoe UI", Font.BOLD, 32);
            TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
            SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
            NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
            BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
        }
    }

    private void initializeUI() {
        setTitle("DineOn-Demand POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 800);
        getContentPane().setBackground(PRIMARY_BG);
        setLayout(new BorderLayout(0, 0));

        // Header Panel
        add(createHeader(), BorderLayout.NORTH);

        // LEFT: Menu Panel (Centered)
        JPanel menuContainer = new JPanel(new GridBagLayout());
        menuContainer.setOpaque(false);
        
        JPanel menuPanel = createMenuPanel();
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setBorder(null);
        menuScroll.getVerticalScrollBar().setUnitIncrement(20);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setOpaque(false);
        
        // Ensure the scroll pane takes the available space but content is centered
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        menuContainer.add(menuScroll, gbc);
        
        add(menuContainer, BorderLayout.CENTER);

        // RIGHT: Order Summary Panel
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Footer
        JPanel footer = new JPanel();
        footer.setBackground(SECONDARY_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LIGHT));
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
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 40));

        // Logo Stylized
        JLabel logo = new JLabel("DineOn-Demand");
        logo.setFont(TITLE_FONT);
        header.add(logo, BorderLayout.WEST);

        // Simple Date Info (Alternative to "random things")
        JLabel dateLabel = new JLabel(java.time.LocalDate.now().toString());
        dateLabel.setFont(NORMAL_FONT);
        dateLabel.setForeground(TEXT_LIGHT);
        header.add(dateLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMenuPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 60, 40));

        // Get Categories
        List<String> categories = menuItems.stream()
                .map(MenuItem::getCategory)
                .distinct()
                .toList();

        for (String category : categories) {
            // Category Header
            JLabel header = new JLabel(category);
            header.setFont(TITLE_FONT);
            header.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(header);

            // Grid Layout for items in this category - updated to 3 columns
            JPanel grid = new JPanel(new GridLayout(0, 3, 30, 30));
            grid.setOpaque(false);
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (MenuItem item : menuItems) {
                if (item.getCategory().equals(category)) {
                    grid.add(new MenuItemPanel(item));
                }
            }
            mainPanel.add(grid);
        }

        return mainPanel;
    }

    // New Inner Class for Item Cards
    private class MenuItemPanel extends JPanel {
        private final MenuItem item;
        private int selectionQty = 1;
        private final JLabel qtyLabel;

        public MenuItemPanel(MenuItem item) {
            this.item = item;
            setLayout(new BorderLayout(8, 8));
            setBackground(new Color(248, 248, 248)); // Slight gray background
            setPreferredSize(new Dimension(180, 330));
            
            // Modern Border / Card Look with More Pronounced Round Corners
            setBorder(BorderFactory.createCompoundBorder(
                new com.formdev.flatlaf.ui.FlatLineBorder(new Insets(1, 1, 1, 1), new Color(235, 235, 235), 1, 25),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            // Square Image Placeholder with Custom Image Support
            JPanel imagePlaceholder = new JPanel(new BorderLayout());
            imagePlaceholder.setPreferredSize(new Dimension(160, 160));
            imagePlaceholder.setBackground(Color.WHITE);
            
            try {
                java.io.File imgFile = new java.io.File(item.getImage());
                if (imgFile.exists()) {
                    ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                    JLabel imgLabel = new JLabel(new ImageIcon(img));
                    imagePlaceholder.add(imgLabel, BorderLayout.CENTER);
                }
            } catch (Exception e) {
                // Fallback to white space if image fails to load
            }
            
            add(imagePlaceholder, BorderLayout.NORTH);

            // Container for Information & Controls
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel priceLabel = new JLabel(String.format("₱%.2f", item.getPrice()));
            priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            priceLabel.setForeground(TEXT_LIGHT);
            priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            contentPanel.add(nameLabel);
            contentPanel.add(Box.createVerticalStrut(2));
            contentPanel.add(priceLabel);
            contentPanel.add(Box.createVerticalStrut(12)); // Strict spacing from price

            // Controls Area (+/-)
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            controls.setOpaque(false);
            controls.setPreferredSize(new Dimension(180, 36));
            controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); // Stop vertical stretching
            
            JButton minusBtn = new JButton("-");
            styleControlBtn(minusBtn);
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
            styleControlBtn(plusBtn);
            plusBtn.addActionListener(e -> {
                selectionQty++;
                updateQtyDisplay();
            });

            controls.add(minusBtn);
            controls.add(qtyLabel);
            controls.add(plusBtn);
            contentPanel.add(controls);
            contentPanel.add(Box.createVerticalStrut(12)); // "Slight more spacing between it again"

            // Add to Order Button
            JButton addBtn = new JButton("Add to Order");
            stylePrimaryButton(addBtn);
            addBtn.setPreferredSize(new Dimension(140, 42));
            addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            addBtn.addActionListener(e -> {
                orderManager.addItem(item, selectionQty);
                updateOrderDisplay();
                selectionQty = 1;
                updateQtyDisplay();
            });
            contentPanel.add(addBtn);
            contentPanel.add(Box.createVerticalGlue()); // Absorbs remaining void at the bottom

            add(contentPanel, BorderLayout.CENTER);
        }

        private void styleControlBtn(JButton btn) {
            btn.setPreferredSize(new Dimension(36, 36));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btn.setForeground(ACCENT_COLOR);
            btn.setBackground(new Color(230, 230, 230));
            btn.setFocusPainted(false);
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.putClientProperty("JButton.arc", 999); // Fully circular
            btn.setBorderPainted(false);
            btn.setMargin(new Insets(0, 0, 0, 0));
        }

        public void updateQtyDisplay() {
            qtyLabel.setText(String.valueOf(selectionQty));
        }
    }


    private void refreshMenuCards() {
        // Updated to reflect new card design if needed, but simplified for now
        repaint();
    }

    private JPanel createRightPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setPreferredSize(new Dimension(450, 0));
        container.setBackground(SECONDARY_BG);
        container.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_LIGHT));

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
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
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
        discountCombo.setPreferredSize(new Dimension(0, 40));
        discountCombo.addActionListener(this::applyDiscountShortcut);
        discPanel.add(discountCombo, BorderLayout.CENTER);
        controls.add(discPanel, gbc);

        // Totals
        gbc.gridy = 2;
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 0, 5));
        totalsPanel.setOpaque(false);
        totalsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_LIGHT),
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
        actionPanel.setPreferredSize(new Dimension(0, 45));
        controls.add(actionPanel, gbc);

        container.add(controls, BorderLayout.SOUTH);
        return container;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(55);
        table.setFont(NORMAL_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(SECONDARY_BG);
        header.setForeground(TEXT_LIGHT);
        header.setFont(NORMAL_FONT.deriveFont(12f));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        // Center item names and others with padding
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel label) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        };
        
        table.getColumnModel().getColumn(0).setCellRenderer(paddedRenderer);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Qty
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer); // Price
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Total

        // Column Config - Adjusted for 450px width
        table.getColumnModel().getColumn(0).setPreferredWidth(180); // Item Name
        table.getColumnModel().getColumn(1).setPreferredWidth(70);  // Price
        table.getColumnModel().getColumn(2).setPreferredWidth(50);  // Qty
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Total
        table.getColumnModel().getColumn(4).setMinWidth(90);        // Action Column
    }

    private JLabel createTotalLabel(String text, boolean isMain) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(isMain ? TITLE_FONT : SUBTITLE_FONT);
        if (isMain) label.setForeground(ACCENT_COLOR);
        return label;
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JButton.arc", 6);
        button.setBorderPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(230, 230, 230));
        button.setForeground(ACCENT_COLOR);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JButton.arc", 6);
        button.setBorderPainted(false);
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
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            JButton btn = new JButton("Remove");
            btn.setFont(GEIST_BOLD.deriveFont(10f));
            btn.setBackground(new Color(220, 50, 50));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(70, 26));
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.putClientProperty("JButton.arc", 999);
            btn.setBorderPainted(false);
            
            panel.add(btn);
            return panel;
        }
    }

    private class TableActionEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton btn;
        private int currentRow;

        public TableActionEditor() {
            super(new JCheckBox());
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(true);
            
            btn = new JButton("Remove");
            btn.setFont(GEIST_BOLD.deriveFont(10f));
            btn.setBackground(new Color(220, 50, 50));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(70, 26));
            btn.putClientProperty("JButton.buttonType", "roundRect");
            btn.putClientProperty("JButton.arc", 999);
            btn.setBorderPainted(false);
            
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
            panel.add(btn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
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
