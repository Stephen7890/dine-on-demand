import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class DineOnDemandApp extends JFrame {
    private final OrderManager orderManager;
    private final List<MenuItem> menuItems;
    
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
        this.orderManager = new OrderManager();
        this.menuItems = MenuData.getInitialMenu();
        
        initializeUI();
    }

    private void initializeUI() {
        setTitle("DineOn-Demand POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout(10, 10));

        // LEFT: Menu Panel
        JPanel menuPanel = createMenuPanel();
        add(new JScrollPane(menuPanel), BorderLayout.CENTER);

        // RIGHT: Order Summary Panel
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // Bottom: Footer/Branding
        JPanel footer = new JPanel();
        footer.add(new JLabel("DineOn-Demand: A New Era in Meal Accessibility"));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(footer, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (MenuItem item : menuItems) {
            JButton itemButton = new JButton("<html><center><b>" + item.getName() + "</b><br>₱" + item.getPrice() + "</center></html>");
            itemButton.setPreferredSize(new Dimension(120, 80));
            itemButton.setBackground(new Color(240, 240, 240));
            itemButton.addActionListener(e -> addItemToOrder(item));
            panel.add(itemButton);
        }
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        // Table
        String[] columnNames = {"Item", "Price", "Qty", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        orderTable = new JTable(tableModel);
        panel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        // Controls
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        // Dine-in / Take-out
        gbc.gridy = 0;
        JPanel modePanel = new JPanel();
        dineInButton = new JRadioButton("Dine-In", true);
        takeOutButton = new JRadioButton("Take-Out");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(dineInButton);
        modeGroup.add(takeOutButton);
        modePanel.add(dineInButton);
        modePanel.add(takeOutButton);
        dineInButton.addActionListener(e -> orderManager.setDineIn(true));
        takeOutButton.addActionListener(e -> orderManager.setDineIn(false));
        controls.add(modePanel, gbc);

        // Discounts
        gbc.gridy = 1;
        JPanel discountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        discountPanel.add(new JLabel("Discount:"));
        discountCombo = new JComboBox<>(new String[]{"None", "Senior Citizen (20%)", "PWD (20%)", "Promo Code (10%)"});
        discountCombo.addActionListener(this::applyDiscountShortcut);
        discountPanel.add(discountCombo);
        controls.add(discountPanel, gbc);

        // Totals
        gbc.gridy = 2;
        JPanel totalsPanel = new JPanel(new GridLayout(3, 2));
        totalsPanel.add(new JLabel("Subtotal:"));
        subtotalLabel = new JLabel("₱0.00", SwingConstants.RIGHT);
        totalsPanel.add(subtotalLabel);
        
        totalsPanel.add(new JLabel("Discount:"));
        discountLabel = new JLabel("₱0.00", SwingConstants.RIGHT);
        totalsPanel.add(discountLabel);
        
        totalsPanel.add(new JLabel("<html><b>Total:</b></html>"));
        totalLabel = new JLabel("<html><b>₱0.00</b></html>", SwingConstants.RIGHT);
        totalsPanel.add(totalLabel);
        controls.add(totalsPanel, gbc);

        // Actions
        gbc.gridy = 3;
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton clearButton = new JButton("Clear Order");
        clearButton.addActionListener(e -> clearOrder());
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setBackground(new Color(76, 175, 80));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.addActionListener(e -> processCheckout());
        actionPanel.add(clearButton);
        actionPanel.add(checkoutButton);
        controls.add(actionPanel, gbc);

        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private void addItemToOrder(MenuItem item) {
        orderManager.addItem(item, 1);
        updateOrderDisplay();
    }

    private void updateOrderDisplay() {
        tableModel.setRowCount(0);
        for (OrderLine line : orderManager.getItems()) {
            tableModel.addRow(new Object[]{
                line.getItem().getName(),
                String.format("%.2f", line.getItem().getPrice()),
                line.getQuantity(),
                String.format("%.2f", line.getSubtotal())
            });
        }
        
        subtotalLabel.setText(String.format("₱%.2f", orderManager.calculateSubtotal()));
        discountLabel.setText(String.format("-₱%.2f", orderManager.calculateDiscountAmount()));
        totalLabel.setText(String.format("<html><b>₱%.2f</b></html>", orderManager.calculateTotal()));
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
    }

    private void processCheckout() {
        if (orderManager.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order is empty!");
            return;
        }

        // Simple Payment Selection Dialog
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
        
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Receipt", JOptionPane.INFORMATION_MESSAGE);
        
        clearOrder();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DineOnDemandApp().setVisible(true);
        });
    }
}
