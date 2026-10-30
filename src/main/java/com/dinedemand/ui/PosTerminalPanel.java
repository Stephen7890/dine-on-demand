package com.dinedemand.ui;

import com.dinedemand.model.MenuItem;
import com.dinedemand.model.Order;
import com.dinedemand.model.OrderLine;
import com.dinedemand.model.OrderManager;
import com.dinedemand.service.PosService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Point of Sale (POS) Ordering Terminal.
 * Features real-time search, category filtering, stock availability enforcement,
 * inline cart steppers, dine-in/take-out synchronization, and payment processing.
 */
public class PosTerminalPanel extends JPanel {
    private final PosService posService;
    private final OrderManager orderManager;
    private final Frame parentFrame;
    private final Runnable onOrderCompletedCallback;

    // State
    private String selectedCategory = "All Items";
    private String currentSearchQuery = "";
    private String currentOrderNumber;

    // UI Components - Catalog
    private JPanel cardsGrid;
    private JTextField searchField;
    private JPanel categoryPillsContainer;
    private List<JButton> pillButtons = new ArrayList<>();

    // UI Components - Cart
    private JLabel orderNumberHeaderLabel;
    private JRadioButton dineInRadio;
    private JRadioButton takeOutRadio;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JComboBox<String> discountComboBox;
    private JLabel subtotalValueLabel;
    private JLabel discountValueLabel;
    private JLabel totalValueLabel;
    private JButton checkoutButton;
    private JButton clearCartButton;

    public PosTerminalPanel(Frame parentFrame, PosService posService, Runnable onOrderCompletedCallback) {
        this.parentFrame = parentFrame;
        this.posService = posService;
        this.orderManager = new OrderManager();
        this.onOrderCompletedCallback = onOrderCompletedCallback;
        this.currentOrderNumber = posService.getNextOrderNumber();

        setLayout(new BorderLayout());
        setBackground(UIUtils.SECONDARY_BG);

        buildUI();
        refreshCatalog();
    }

    private void buildUI() {
        // TOP: Global Header
        add(createHeaderBar(), BorderLayout.NORTH);

        // CENTER: Catalog Browser (Search, Category Pills, Cards Grid)
        add(createCatalogPanel(), BorderLayout.CENTER);

        // EAST: Right Order Sidebar
        add(createCartSidebar(), BorderLayout.EAST);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UIUtils.PRIMARY_BG);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Brand & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("DineOn-Demand");
        logoLabel.setFont(UIUtils.FONT_TITLE);
        logoLabel.setForeground(UIUtils.TEXT_PRIMARY);

        JLabel badge = new JLabel(" POS TERMINAL ");
        badge.setFont(UIUtils.FONT_SMALL_BOLD);
        badge.setOpaque(true);
        badge.setBackground(UIUtils.ACCENT_LIGHT);
        badge.setForeground(UIUtils.ACCENT_PRIMARY);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        brandPanel.add(logoLabel);
        brandPanel.add(badge);
        header.add(brandPanel, BorderLayout.WEST);

        // Center: Real-time Search Field
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        searchContainer.setOpaque(false);

        searchField = new JTextField(22);
        searchField.setFont(UIUtils.FONT_BODY);
        searchField.setPreferredSize(new Dimension(320, 40));
        searchField.putClientProperty("JTextField.placeholderText", "Search items or categories...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            public void changedUpdate(DocumentEvent e) { updateSearch(); }
        });

        searchContainer.add(searchField);
        header.add(searchContainer, BorderLayout.CENTER);

        // East: Live Clock & Next Order Indicator
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        statusPanel.setOpaque(false);

        JLabel clockLabel = new JLabel();
        clockLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clockLabel.setForeground(UIUtils.TEXT_PRIMARY);

        JLabel orderIndicator = new JLabel("Active Order: #" + currentOrderNumber);
        orderIndicator.setFont(UIUtils.FONT_SMALL);
        orderIndicator.setHorizontalAlignment(SwingConstants.RIGHT);
        orderIndicator.setForeground(UIUtils.TEXT_MUTED);

        statusPanel.add(clockLabel);
        statusPanel.add(orderIndicator);
        header.add(statusPanel, BorderLayout.EAST);

        // Clock timer
        Timer timer = new Timer(1000, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, MMM dd  •  hh:mm:ss a")));
        });
        timer.start();
        clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, MMM dd  •  hh:mm:ss a")));

        return header;
    }

    private JPanel createCatalogPanel() {
        JPanel catalogPanel = new JPanel(new BorderLayout(0, 12));
        catalogPanel.setOpaque(true);
        catalogPanel.setBackground(UIUtils.SECONDARY_BG);
        catalogPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        // Category Pills
        categoryPillsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoryPillsContainer.setOpaque(false);
        categoryPillsContainer.setPreferredSize(new Dimension(800, 44));
        rebuildCategoryPills();
        catalogPanel.add(categoryPillsContainer, BorderLayout.NORTH);

        // Scrollable Cards Grid
        cardsGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        cardsGrid.setOpaque(false);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(cardsGrid, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        catalogPanel.add(scrollPane, BorderLayout.CENTER);
        return catalogPanel;
    }

    private void rebuildCategoryPills() {
        try {
            categoryPillsContainer.removeAll();
            pillButtons.clear();

            List<String> categories = new ArrayList<>();
            categories.add("All Items");
            categories.addAll(posService.getCategories());

            for (String cat : categories) {
                JButton pill = new JButton(cat);
                pill.setFont(UIUtils.FONT_BODY_BOLD);
                pill.setFocusPainted(false);
                pill.setCursor(new Cursor(Cursor.HAND_CURSOR));
                updatePillStyle(pill, cat.equalsIgnoreCase(selectedCategory));

                pill.addActionListener(e -> {
                    selectedCategory = cat;
                    for (JButton b : pillButtons) {
                        updatePillStyle(b, b.getText().equalsIgnoreCase(selectedCategory));
                    }
                    refreshCatalog();
                });

                pillButtons.add(pill);
                categoryPillsContainer.add(pill);
            }
            categoryPillsContainer.revalidate();
            categoryPillsContainer.repaint();
        } catch (Exception e) {
            System.err.println("Error rebuilding category pills: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePillStyle(JButton pill, boolean active) {
        if (active) {
            pill.setBackground(UIUtils.ACCENT_PRIMARY);
            pill.setForeground(Color.WHITE);
            pill.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        } else {
            pill.setBackground(Color.WHITE);
            pill.setForeground(UIUtils.TEXT_SECONDARY);
            pill.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1),
                    BorderFactory.createEmptyBorder(7, 15, 7, 15)
            ));
        }
    }

    private void updateSearch() {
        this.currentSearchQuery = searchField.getText().trim();
        refreshCatalog();
    }

    public void refreshCatalog() {
        try {
            cardsGrid.removeAll();
            List<MenuItem> items = posService.getCatalog(selectedCategory, currentSearchQuery);

            if (items.isEmpty()) {
                JPanel emptyPanel = new JPanel(new GridBagLayout());
                emptyPanel.setOpaque(false);
                JLabel emptyLabel = new JLabel("No menu items found.");
                emptyLabel.setFont(UIUtils.FONT_TITLE);
                emptyLabel.setForeground(UIUtils.TEXT_MUTED);
                emptyPanel.add(emptyLabel);
                cardsGrid.setLayout(new BorderLayout());
                cardsGrid.add(emptyPanel, BorderLayout.CENTER);
            } else {
                cardsGrid.setLayout(new GridLayout(0, 3, 16, 16));
                for (MenuItem item : items) {
                    cardsGrid.add(new ProductCard(item));
                }
            }

            cardsGrid.revalidate();
            cardsGrid.repaint();
        } catch (Exception e) {
            System.err.println("Error refreshing catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Product Card Widget
    private class ProductCard extends JPanel {
        private final MenuItem item;
        private int quantity = 1;
        private final JLabel qtyLabel;
        private final JButton addBtn;

        public ProductCard(MenuItem item) {
            this.item = item;
            setLayout(new BorderLayout(0, 8));
            setPreferredSize(new Dimension(210, 320));
            setMinimumSize(new Dimension(190, 290));
            setOpaque(true);
            setBackground(UIUtils.CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));

            // 1. Thumbnail Image Container
            JPanel imageContainer = new JPanel(new BorderLayout());
            imageContainer.setPreferredSize(new Dimension(0, 140));
            imageContainer.setBackground(new Color(248, 250, 252));

            ImageIcon icon = UIUtils.getScaledImage(item.getImagePath(), 140, 140);
            JLabel imgLabel = new JLabel(icon, SwingConstants.CENTER);
            imageContainer.add(imgLabel, BorderLayout.CENTER);
            add(imageContainer, BorderLayout.NORTH);

            // 2. Info Panel
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            // Name and Category
            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setFont(UIUtils.FONT_BODY_BOLD);
            nameLabel.setForeground(UIUtils.TEXT_PRIMARY);

            JLabel catLabel = new JLabel(item.getCategory());
            catLabel.setFont(UIUtils.FONT_SMALL);
            catLabel.setForeground(UIUtils.TEXT_MUTED);

            // Price and Stock Badge Row
            JPanel metaRow = new JPanel(new BorderLayout());
            metaRow.setOpaque(false);

            JLabel priceLabel = new JLabel(item.getFormattedPrice());
            priceLabel.setFont(UIUtils.FONT_TITLE);
            priceLabel.setForeground(UIUtils.ACCENT_PRIMARY);

            int remainingStock = item.getStockQuantity() - orderManager.getQuantityForItem(item);
            JLabel stockBadge = new JLabel();
            stockBadge.setFont(UIUtils.FONT_SMALL_BOLD);
            stockBadge.setOpaque(true);

            if (remainingStock <= 0) {
                stockBadge.setText(" Out of Stock ");
                stockBadge.setBackground(UIUtils.DANGER_BG);
                stockBadge.setForeground(UIUtils.DANGER_RED);
            } else if (remainingStock <= 5) {
                stockBadge.setText(" Low Stock: " + remainingStock + " ");
                stockBadge.setBackground(UIUtils.WARNING_BG);
                stockBadge.setForeground(UIUtils.WARNING_AMBER);
            } else {
                stockBadge.setText(" Stock: " + remainingStock + " ");
                stockBadge.setBackground(UIUtils.SUCCESS_BG);
                stockBadge.setForeground(UIUtils.SUCCESS_GREEN);
            }
            stockBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            metaRow.add(priceLabel, BorderLayout.WEST);
            metaRow.add(stockBadge, BorderLayout.EAST);

            infoPanel.add(nameLabel);
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(catLabel);
            infoPanel.add(Box.createVerticalStrut(8));
            infoPanel.add(metaRow);
            infoPanel.add(Box.createVerticalStrut(10));

            // Stepper and Add Button
            JPanel actionRow = new JPanel(new BorderLayout(8, 0));
            actionRow.setOpaque(false);

            JPanel stepper = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            stepper.setOpaque(false);

            qtyLabel = new JLabel("1", SwingConstants.CENTER);
            qtyLabel.setFont(UIUtils.FONT_BODY_BOLD);
            qtyLabel.setPreferredSize(new Dimension(28, 28));

            JButton minusBtn = new JButton("-");
            styleMiniBtn(minusBtn);
            minusBtn.addActionListener(e -> {
                if (quantity > 1) {
                    quantity--;
                    qtyLabel.setText(String.valueOf(quantity));
                }
            });

            JButton plusBtn = new JButton("+");
            styleMiniBtn(plusBtn);
            plusBtn.addActionListener(e -> {
                int currentInCart = orderManager.getQuantityForItem(item);
                if (currentInCart + quantity < item.getStockQuantity()) {
                    quantity++;
                    qtyLabel.setText(String.valueOf(quantity));
                }
            });

            stepper.add(minusBtn);
            stepper.add(qtyLabel);
            stepper.add(plusBtn);

            addBtn = new JButton("+ Add");
            addBtn.setFont(UIUtils.FONT_SMALL_BOLD);
            addBtn.setBackground(UIUtils.ACCENT_COLOR);
            addBtn.setForeground(Color.WHITE);
            addBtn.setFocusPainted(false);
            addBtn.setPreferredSize(new Dimension(90, 32));

            boolean canAdd = remainingStock > 0;
            addBtn.setEnabled(canAdd);
            plusBtn.setEnabled(canAdd);

            addBtn.addActionListener(e -> {
                boolean added = orderManager.addItem(item, quantity);
                if (added) {
                    quantity = 1;
                    qtyLabel.setText("1");
                    updateCartDisplay();
                    refreshCatalog();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Cannot add " + item.getName() + ": Stock limit reached.",
                            "Stock Constraint", JOptionPane.WARNING_MESSAGE);
                }
            });

            actionRow.add(stepper, BorderLayout.WEST);
            actionRow.add(addBtn, BorderLayout.EAST);

            infoPanel.add(actionRow);
            add(infoPanel, BorderLayout.CENTER);
        }

        private void styleMiniBtn(JButton btn) {
            btn.setFont(UIUtils.FONT_BODY_BOLD);
            btn.setPreferredSize(new Dimension(30, 30));
            btn.setBackground(new Color(241, 245, 249));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        }
    }

    private JPanel createCartSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 0));
        sidebar.setPreferredSize(new Dimension(420, 820));
        sidebar.setMinimumSize(new Dimension(380, 500));
        sidebar.setOpaque(true);
        sidebar.setBackground(UIUtils.PRIMARY_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIUtils.BORDER_LIGHT));

        // 1. Sidebar Header
        JPanel sideHeader = new JPanel(new BorderLayout());
        sideHeader.setOpaque(false);
        sideHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        orderNumberHeaderLabel = new JLabel("Order #" + currentOrderNumber);
        orderNumberHeaderLabel.setFont(UIUtils.FONT_TITLE);
        orderNumberHeaderLabel.setForeground(UIUtils.TEXT_PRIMARY);

        JLabel currentOrderSub = new JLabel("Current Basket");
        currentOrderSub.setFont(UIUtils.FONT_SMALL);
        currentOrderSub.setForeground(UIUtils.TEXT_MUTED);

        JPanel orderTitles = new JPanel(new GridLayout(2, 1, 0, 2));
        orderTitles.setOpaque(false);
        orderTitles.add(orderNumberHeaderLabel);
        orderTitles.add(currentOrderSub);
        sideHeader.add(orderTitles, BorderLayout.WEST);

        // Service Mode Toggle (Dine-In vs Take-Out)
        JPanel serviceTogglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        serviceTogglePanel.setOpaque(false);

        dineInRadio = new JRadioButton("Dine-In", true);
        takeOutRadio = new JRadioButton("Take-Out", false);
        dineInRadio.setFont(UIUtils.FONT_SMALL_BOLD);
        takeOutRadio.setFont(UIUtils.FONT_SMALL_BOLD);
        dineInRadio.setOpaque(false);
        takeOutRadio.setOpaque(false);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(dineInRadio);
        modeGroup.add(takeOutRadio);

        // Active Event Listeners Synchronizing with OrderManager!
        dineInRadio.addActionListener(e -> orderManager.setDineIn(true));
        takeOutRadio.addActionListener(e -> orderManager.setDineIn(false));

        serviceTogglePanel.add(dineInRadio);
        serviceTogglePanel.add(takeOutRadio);
        sideHeader.add(serviceTogglePanel, BorderLayout.EAST);

        sidebar.add(sideHeader, BorderLayout.NORTH);

        // 2. Center: Cart Table with Action Renderers
        String[] cols = {"Item", "Price", "Qty", "Subtotal", "Actions"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // Only action controls editable
            }
        };

        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(48);
        cartTable.setFont(UIUtils.FONT_BODY);
        cartTable.setShowGrid(false);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(JLabel.RIGHT);

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(rightRender);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRender);

        cartTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        cartTable.getColumnModel().getColumn(4).setCellRenderer(new CartActionRenderer());
        cartTable.getColumnModel().getColumn(4).setCellEditor(new CartActionEditor());

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(null);
        cartScroll.getViewport().setBackground(Color.WHITE);
        sidebar.add(cartScroll, BorderLayout.CENTER);

        // 3. Footer: Discounts, Totals & Checkout
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(16, 20, 20, 20)
        ));

        // Discount Selector
        JPanel discRow = new JPanel(new BorderLayout(10, 0));
        discRow.setOpaque(false);
        JLabel discLabel = new JLabel("Discount / Promo:");
        discLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        discLabel.setForeground(UIUtils.TEXT_SECONDARY);

        discountComboBox = new JComboBox<>(new String[]{
                "None (0%)",
                "Senior Citizen (20%)",
                "PWD (20%)",
                "Promo Code (10%)"
        });
        discountComboBox.setFont(UIUtils.FONT_BODY);
        discountComboBox.setPreferredSize(new Dimension(0, 36));
        discountComboBox.addActionListener(e -> applyDiscount());

        discRow.add(discLabel, BorderLayout.WEST);
        discRow.add(discountComboBox, BorderLayout.CENTER);
        footer.add(discRow);
        footer.add(Box.createVerticalStrut(14));

        // Breakdown Rows
        footer.add(createSummaryRow("Subtotal:", subtotalValueLabel = new JLabel("₱0.00"), false));
        footer.add(Box.createVerticalStrut(6));
        footer.add(createSummaryRow("Discount Applied:", discountValueLabel = new JLabel("-₱0.00"), false));
        discountValueLabel.setForeground(UIUtils.SUCCESS_GREEN);
        footer.add(Box.createVerticalStrut(8));

        // Final Total
        JPanel totalRow = createSummaryRow("TOTAL DUE:", totalValueLabel = new JLabel("₱0.00"), true);
        totalValueLabel.setFont(UIUtils.FONT_HERO);
        totalValueLabel.setForeground(UIUtils.ACCENT_PRIMARY);
        footer.add(totalRow);
        footer.add(Box.createVerticalStrut(16));

        // Action Buttons Row
        JPanel actionsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        actionsRow.setOpaque(false);
        actionsRow.setPreferredSize(new Dimension(0, 48));

        clearCartButton = new JButton("Clear");
        clearCartButton.setFont(UIUtils.FONT_BODY_BOLD);
        clearCartButton.setBackground(new Color(241, 245, 249));
        clearCartButton.setForeground(UIUtils.TEXT_PRIMARY);
        clearCartButton.setFocusPainted(false);
        clearCartButton.addActionListener(e -> handleClearCart());

        checkoutButton = new JButton("Take Payment");
        checkoutButton.setFont(UIUtils.FONT_BODY_BOLD);
        checkoutButton.setBackground(UIUtils.ACCENT_PRIMARY);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.setEnabled(false);
        checkoutButton.addActionListener(e -> handleCheckout());

        actionsRow.add(clearCartButton);
        actionsRow.add(checkoutButton);
        footer.add(actionsRow);

        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createSummaryRow(String title, JLabel valueLabel, boolean isMain) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(isMain ? UIUtils.FONT_TITLE : UIUtils.FONT_BODY);
        titleLbl.setForeground(isMain ? UIUtils.TEXT_PRIMARY : UIUtils.TEXT_SECONDARY);

        valueLabel.setFont(isMain ? UIUtils.FONT_TITLE : UIUtils.FONT_BODY_BOLD);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(titleLbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private void applyDiscount() {
        String selected = (String) discountComboBox.getSelectedItem();
        if (selected == null || selected.startsWith("None")) {
            orderManager.setDiscount("None", 0.0);
        } else if (selected.startsWith("Senior")) {
            orderManager.setDiscount("Senior Citizen", 0.20);
        } else if (selected.startsWith("PWD")) {
            orderManager.setDiscount("PWD", 0.20);
        } else if (selected.startsWith("Promo")) {
            orderManager.setDiscount("Promo Code", 0.10);
        }
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartTableModel.setRowCount(0);
        for (OrderLine line : orderManager.getItems()) {
            cartTableModel.addRow(new Object[]{
                    line.getItem().getName(),
                    String.format("%.2f", line.getItem().getPrice()),
                    line.getQuantity(),
                    String.format("%.2f", line.getSubtotal()),
                    line
            });
        }

        subtotalValueLabel.setText(String.format("₱%,.2f", orderManager.calculateSubtotal()));
        discountValueLabel.setText(String.format("-₱%,.2f", orderManager.calculateDiscountAmount()));
        totalValueLabel.setText(String.format("₱%,.2f", orderManager.calculateTotal()));

        boolean hasItems = !orderManager.getItems().isEmpty();
        checkoutButton.setEnabled(hasItems);
    }

    private void handleClearCart() {
        if (orderManager.getItems().isEmpty()) return;

        int res = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the entire current order?",
                "Confirm Clear Cart", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            orderManager.clear();
            discountComboBox.setSelectedIndex(0);
            dineInRadio.setSelected(true);
            updateCartDisplay();
            refreshCatalog();
        }
    }

    private void handleCheckout() {
        if (orderManager.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order is empty!", "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PaymentDialog paymentDialog = new PaymentDialog(parentFrame, orderManager.calculateTotal());
        paymentDialog.setVisible(true);

        if (paymentDialog.isPaymentCompleted()) {
            Order order = new Order();
            order.setOrderNumber(currentOrderNumber);
            order.setServiceMode(orderManager.getServiceMode());
            order.setSubtotal(orderManager.calculateSubtotal());
            order.setDiscountType(orderManager.getDiscountType());
            order.setDiscountRate(orderManager.getDiscountRate());
            order.setDiscountAmount(orderManager.calculateDiscountAmount());
            order.setTotalAmount(orderManager.calculateTotal());
            order.setPaymentMethod(paymentDialog.getSelectedMethod());
            order.setPaymentReference(paymentDialog.getPaymentReference());
            order.setAmountTendered(paymentDialog.getAmountTendered());
            order.setChangeAmount(paymentDialog.getChangeAmount());

            try {
                // Save Order and Deduct Stock Atomically
                posService.completeOrder(order, orderManager.getItems());

                // Show Receipt Dialog
                ReceiptDialog receiptDialog = new ReceiptDialog(parentFrame, order, orderManager.getItems());
                receiptDialog.setVisible(true);

                // Reset Cart & Prep Next Order
                orderManager.clear();
                discountComboBox.setSelectedIndex(0);
                dineInRadio.setSelected(true);
                currentOrderNumber = posService.getNextOrderNumber();
                orderNumberHeaderLabel.setText("Order #" + currentOrderNumber);

                updateCartDisplay();
                refreshCatalog();

                if (onOrderCompletedCallback != null) {
                    onOrderCompletedCallback.run();
                }

            } catch (Exception ex) {
                System.err.println("Transaction checkout error: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Failed to record transaction:\n" + ex.getMessage(),
                        "Transaction Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Inline Cart Actions Renderer & Editor ---
    private class CartActionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            panel.setOpaque(true);
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JButton minusBtn = new JButton("-");
            JButton plusBtn = new JButton("+");
            JButton delBtn = new JButton("×");

            styleInlineBtn(minusBtn, new Color(241, 245, 249), UIUtils.TEXT_PRIMARY);
            styleInlineBtn(plusBtn, new Color(241, 245, 249), UIUtils.TEXT_PRIMARY);
            styleInlineBtn(delBtn, UIUtils.DANGER_BG, UIUtils.DANGER_RED);

            panel.add(minusBtn);
            panel.add(plusBtn);
            panel.add(delBtn);
            return panel;
        }

        private void styleInlineBtn(JButton btn, Color bg, Color fg) {
            btn.setFont(UIUtils.FONT_SMALL_BOLD);
            btn.setPreferredSize(new Dimension(28, 28));
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        }
    }

    private class CartActionEditor extends DefaultCellEditor {
        private final JPanel panel;
        private OrderLine currentLine;

        public CartActionEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);

            JButton minusBtn = new JButton("-");
            JButton plusBtn = new JButton("+");
            JButton delBtn = new JButton("×");

            styleInlineBtn(minusBtn, new Color(241, 245, 249), UIUtils.TEXT_PRIMARY);
            styleInlineBtn(plusBtn, new Color(241, 245, 249), UIUtils.TEXT_PRIMARY);
            styleInlineBtn(delBtn, UIUtils.DANGER_BG, UIUtils.DANGER_RED);

            minusBtn.addActionListener(e -> {
                if (currentLine != null) {
                    orderManager.decreaseItem(currentLine.getItem(), 1);
                    fireEditingStopped();
                    updateCartDisplay();
                    refreshCatalog();
                }
            });

            plusBtn.addActionListener(e -> {
                if (currentLine != null) {
                    int currentInCart = orderManager.getQuantityForItem(currentLine.getItem());
                    if (currentInCart < currentLine.getItem().getStockQuantity()) {
                        orderManager.addItem(currentLine.getItem(), 1);
                        fireEditingStopped();
                        updateCartDisplay();
                        refreshCatalog();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Stock limit reached for this item.", "Stock Limit", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });

            delBtn.addActionListener(e -> {
                if (currentLine != null) {
                    orderManager.removeItem(currentLine.getItem());
                    fireEditingStopped();
                    updateCartDisplay();
                    refreshCatalog();
                }
            });

            panel.add(minusBtn);
            panel.add(plusBtn);
            panel.add(delBtn);
        }

        private void styleInlineBtn(JButton btn, Color bg, Color fg) {
            btn.setFont(UIUtils.FONT_SMALL_BOLD);
            btn.setPreferredSize(new Dimension(28, 28));
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            if (value instanceof OrderLine) {
                this.currentLine = (OrderLine) value;
            }
            return panel;
        }
    }
}
