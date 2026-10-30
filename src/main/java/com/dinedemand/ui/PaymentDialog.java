package com.dinedemand.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Modern modal payment dialog supporting Cash, GCash, and Card with validation.
 */
public class PaymentDialog extends JDialog {
    private final double totalDue;
    private boolean paymentCompleted = false;

    private String selectedMethod = "CASH";
    private String paymentReference = "";
    private Double amountTendered = null;
    private Double changeAmount = null;

    // UI Elements
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    // Cash components
    private JTextField cashTenderedField;
    private JLabel changeLabel;
    private JLabel cashValidationLabel;

    // GCash components
    private JTextField gcashMobileField;
    private JTextField gcashRefField;

    // Card components
    private JTextField cardNameField;
    private JTextField cardLast4Field;
    private JTextField cardApprovalField;

    private JButton completePaymentBtn;

    public PaymentDialog(Frame parent, double totalDue) {
        super(parent, "Process Payment", true);
        this.totalDue = BigDecimal.valueOf(totalDue).setScale(2, RoundingMode.HALF_UP).doubleValue();

        setSize(520, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(UIUtils.PRIMARY_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. Header: Total Due Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(241, 245, 249));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel totalTitle = new JLabel("TOTAL DUE");
        totalTitle.setFont(UIUtils.FONT_SMALL_BOLD);
        totalTitle.setForeground(UIUtils.TEXT_SECONDARY);

        JLabel totalAmountLabel = new JLabel(String.format("₱%,.2f", totalDue));
        totalAmountLabel.setFont(UIUtils.FONT_HERO);
        totalAmountLabel.setForeground(UIUtils.ACCENT_PRIMARY);

        headerPanel.add(totalTitle, BorderLayout.NORTH);
        headerPanel.add(totalAmountLabel, BorderLayout.CENTER);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center: Method Selector & Form Cards
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Method Toggle Buttons
        JPanel methodTogglePanel = new JPanel(new GridLayout(1, 3, 10, 0));
        methodTogglePanel.setOpaque(false);

        JButton cashBtn = createMethodButton("Cash", true);
        JButton gcashBtn = createMethodButton("GCash", false);
        JButton cardBtn = createMethodButton("Card", false);

        methodTogglePanel.add(cashBtn);
        methodTogglePanel.add(gcashBtn);
        methodTogglePanel.add(cardBtn);
        centerPanel.add(methodTogglePanel, BorderLayout.NORTH);

        // Form Cards
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);

        cardsPanel.add(createCashPanel(), "CASH");
        cardsPanel.add(createGCashPanel(), "GCASH");
        cardsPanel.add(createCardPanel(), "CARD");
        centerPanel.add(cardsPanel, BorderLayout.CENTER);

        // Button Click Listeners
        cashBtn.addActionListener(e -> {
            selectedMethod = "CASH";
            setMethodActive(cashBtn, gcashBtn, cardBtn);
            cardLayout.show(cardsPanel, "CASH");
            validatePayment();
        });

        gcashBtn.addActionListener(e -> {
            selectedMethod = "GCASH";
            setMethodActive(gcashBtn, cashBtn, cardBtn);
            cardLayout.show(cardsPanel, "GCASH");
            validatePayment();
        });

        cardBtn.addActionListener(e -> {
            selectedMethod = "CARD";
            setMethodActive(cardBtn, cashBtn, gcashBtn);
            cardLayout.show(cardsPanel, "CARD");
            validatePayment();
        });

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Footer: Actions
        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        footerPanel.setOpaque(false);
        footerPanel.setPreferredSize(new Dimension(0, 48));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(UIUtils.FONT_BODY_BOLD);
        cancelBtn.setBackground(new Color(241, 245, 249));
        cancelBtn.setForeground(UIUtils.TEXT_PRIMARY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> {
            paymentCompleted = false;
            dispose();
        });

        completePaymentBtn = new JButton("Complete Transaction");
        completePaymentBtn.setFont(UIUtils.FONT_BODY_BOLD);
        completePaymentBtn.setBackground(UIUtils.ACCENT_PRIMARY);
        completePaymentBtn.setForeground(Color.WHITE);
        completePaymentBtn.setFocusPainted(false);
        completePaymentBtn.addActionListener(e -> handlePaymentCompletion());

        footerPanel.add(cancelBtn);
        footerPanel.add(completePaymentBtn);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
        validatePayment();
    }

    private JButton createMethodButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 42));
        updateButtonStyle(btn, active);
        return btn;
    }

    private void updateButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(UIUtils.ACCENT_PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        } else {
            btn.setBackground(new Color(248, 250, 252));
            btn.setForeground(UIUtils.TEXT_SECONDARY);
            btn.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        }
    }

    private void setMethodActive(JButton activeBtn, JButton other1, JButton other2) {
        updateButtonStyle(activeBtn, true);
        updateButtonStyle(other1, false);
        updateButtonStyle(other2, false);
    }

    private JPanel createCashPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel tenderedLabel = new JLabel("Cash Tendered (₱):");
        tenderedLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        tenderedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cashTenderedField = new JTextField();
        cashTenderedField.setFont(UIUtils.FONT_TITLE);
        cashTenderedField.setPreferredSize(new Dimension(0, 44));
        cashTenderedField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cashTenderedField.setAlignmentX(Component.LEFT_ALIGNMENT);

        cashTenderedField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validatePayment(); }
            public void removeUpdate(DocumentEvent e) { validatePayment(); }
            public void changedUpdate(DocumentEvent e) { validatePayment(); }
        });

        // Quick Cash Buttons Panel
        JPanel quickCashPanel = new JPanel(new GridLayout(1, 4, 8, 0));
        quickCashPanel.setOpaque(false);
        quickCashPanel.setPreferredSize(new Dimension(0, 36));
        quickCashPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        quickCashPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton exactBtn = new JButton("Exact");
        exactBtn.addActionListener(e -> cashTenderedField.setText(String.format("%.2f", totalDue)));

        JButton plus50 = new JButton("+₱50");
        plus50.addActionListener(e -> addCashToTendered(50));

        JButton plus100 = new JButton("+₱100");
        plus100.addActionListener(e -> addCashToTendered(100));

        JButton plus500 = new JButton("+₱500");
        plus500.addActionListener(e -> addCashToTendered(500));

        for (JButton b : new JButton[]{exactBtn, plus50, plus100, plus500}) {
            b.setFont(UIUtils.FONT_SMALL_BOLD);
            b.setBackground(new Color(241, 245, 249));
            b.setFocusPainted(false);
            quickCashPanel.add(b);
        }

        // Change Display
        JPanel changePanel = new JPanel(new BorderLayout());
        changePanel.setOpaque(false);
        changePanel.setBorder(BorderFactory.createEmptyBorder(14, 0, 4, 0));
        changePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel changeTitle = new JLabel("Change Due:");
        changeTitle.setFont(UIUtils.FONT_BODY_BOLD);
        changeLabel = new JLabel("₱0.00");
        changeLabel.setFont(UIUtils.FONT_TITLE);
        changeLabel.setForeground(UIUtils.SUCCESS_GREEN);

        changePanel.add(changeTitle, BorderLayout.WEST);
        changePanel.add(changeLabel, BorderLayout.EAST);

        cashValidationLabel = new JLabel(" ");
        cashValidationLabel.setFont(UIUtils.FONT_SMALL);
        cashValidationLabel.setForeground(UIUtils.DANGER_RED);
        cashValidationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(tenderedLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cashTenderedField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(quickCashPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(changePanel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cashValidationLabel);

        return panel;
    }

    private void addCashToTendered(double amount) {
        try {
            double current = cashTenderedField.getText().trim().isEmpty() ? 0.0 :
                    Double.parseDouble(cashTenderedField.getText().trim());
            cashTenderedField.setText(String.format("%.2f", current + amount));
        } catch (NumberFormatException e) {
            cashTenderedField.setText(String.format("%.2f", amount));
        }
    }

    private JPanel createGCashPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel mobileLabel = new JLabel("Customer GCash Mobile No. (e.g., 09171234567):");
        mobileLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        mobileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        gcashMobileField = new JTextField();
        gcashMobileField.setFont(UIUtils.FONT_BODY);
        gcashMobileField.setPreferredSize(new Dimension(0, 40));
        gcashMobileField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        gcashMobileField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel refLabel = new JLabel("GCash Reference Number (e.g., MP-20261030-9821):");
        refLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        refLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        gcashRefField = new JTextField();
        gcashRefField.setFont(UIUtils.FONT_BODY);
        gcashRefField.setPreferredSize(new Dimension(0, 40));
        gcashRefField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        gcashRefField.setAlignmentX(Component.LEFT_ALIGNMENT);

        gcashRefField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validatePayment(); }
            public void removeUpdate(DocumentEvent e) { validatePayment(); }
            public void changedUpdate(DocumentEvent e) { validatePayment(); }
        });

        panel.add(mobileLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(gcashMobileField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(refLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(gcashRefField);

        return panel;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel nameLabel = new JLabel("Cardholder Name:");
        nameLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardNameField = new JTextField();
        cardNameField.setFont(UIUtils.FONT_BODY);
        cardNameField.setPreferredSize(new Dimension(0, 40));
        cardNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 68));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel col1 = new JPanel();
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setOpaque(false);
        JLabel last4Label = new JLabel("Card Last 4 Digits:");
        last4Label.setFont(UIUtils.FONT_SMALL_BOLD);
        cardLast4Field = new JTextField();
        cardLast4Field.setFont(UIUtils.FONT_BODY);
        col1.add(last4Label);
        col1.add(Box.createVerticalStrut(6));
        col1.add(cardLast4Field);

        JPanel col2 = new JPanel();
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        col2.setOpaque(false);
        JLabel appCodeLabel = new JLabel("Terminal Approval Code:");
        appCodeLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        cardApprovalField = new JTextField();
        cardApprovalField.setFont(UIUtils.FONT_BODY);
        col2.add(appCodeLabel);
        col2.add(Box.createVerticalStrut(6));
        col2.add(cardApprovalField);

        row.add(col1);
        row.add(col2);

        cardLast4Field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validatePayment(); }
            public void removeUpdate(DocumentEvent e) { validatePayment(); }
            public void changedUpdate(DocumentEvent e) { validatePayment(); }
        });

        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cardNameField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(row);

        return panel;
    }

    private void validatePayment() {
        if ("CASH".equals(selectedMethod)) {
            String txt = cashTenderedField.getText().trim();
            if (txt.isEmpty()) {
                cashValidationLabel.setText("Please enter amount tendered.");
                changeLabel.setText("₱0.00");
                completePaymentBtn.setEnabled(false);
                return;
            }
            try {
                double tendered = Double.parseDouble(txt);
                if (tendered < totalDue) {
                    double shortAmount = totalDue - tendered;
                    cashValidationLabel.setText(String.format("Insufficient payment. Short by ₱%.2f", shortAmount));
                    changeLabel.setText("₱0.00");
                    completePaymentBtn.setEnabled(false);
                } else {
                    double change = tendered - totalDue;
                    cashValidationLabel.setText(" ");
                    changeLabel.setText(String.format("₱%,.2f", change));
                    completePaymentBtn.setEnabled(true);
                }
            } catch (NumberFormatException e) {
                cashValidationLabel.setText("Invalid number format.");
                completePaymentBtn.setEnabled(false);
            }
        } else if ("GCASH".equals(selectedMethod)) {
            String ref = gcashRefField != null ? gcashRefField.getText().trim() : "";
            completePaymentBtn.setEnabled(!ref.isEmpty());
        } else if ("CARD".equals(selectedMethod)) {
            String last4 = cardLast4Field != null ? cardLast4Field.getText().trim() : "";
            completePaymentBtn.setEnabled(!last4.isEmpty());
        }
    }

    private void handlePaymentCompletion() {
        if ("CASH".equals(selectedMethod)) {
            try {
                double tendered = Double.parseDouble(cashTenderedField.getText().trim());
                if (tendered < totalDue) return;
                this.amountTendered = tendered;
                this.changeAmount = tendered - totalDue;
                this.paymentReference = "CASH-" + System.currentTimeMillis() % 100000;
                this.paymentCompleted = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid cash amount.", "Payment Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("GCASH".equals(selectedMethod)) {
            String ref = gcashRefField.getText().trim();
            String mobile = gcashMobileField.getText().trim();
            if (ref.isEmpty()) {
                JOptionPane.showMessageDialog(this, "GCash reference number is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            this.paymentReference = ref + (mobile.isEmpty() ? "" : " (Mobile: " + mobile + ")");
            this.amountTendered = totalDue;
            this.changeAmount = 0.0;
            this.paymentCompleted = true;
            dispose();
        } else if ("CARD".equals(selectedMethod)) {
            String last4 = cardLast4Field.getText().trim();
            String appCode = cardApprovalField.getText().trim();
            String name = cardNameField.getText().trim();
            if (last4.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Card last 4 digits are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            this.paymentReference = "Card ************" + last4 +
                    (appCode.isEmpty() ? "" : " | Appr: " + appCode) +
                    (name.isEmpty() ? "" : " | " + name);
            this.amountTendered = totalDue;
            this.changeAmount = 0.0;
            this.paymentCompleted = true;
            dispose();
        }
    }

    public boolean isPaymentCompleted() { return paymentCompleted; }
    public String getSelectedMethod() { return selectedMethod; }
    public String getPaymentReference() { return paymentReference; }
    public Double getAmountTendered() { return amountTendered; }
    public Double getChangeAmount() { return changeAmount; }
}
