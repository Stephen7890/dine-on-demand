package com.dinedemand.ui;

import com.dinedemand.model.Order;
import com.dinedemand.model.OrderLine;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Modal receipt preview dialog displaying an itemized fiscal receipt with export/print support.
 */
public class ReceiptDialog extends JDialog {
    private final String receiptText;

    public ReceiptDialog(Frame parent, Order order, List<OrderLine> lines) {
        super(parent, "Transaction Receipt - " + order.getOrderNumber(), true);
        setSize(460, 640);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        this.receiptText = buildReceiptText(order, lines);
        buildUI();
    }

    private void buildUI() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBackground(UIUtils.PRIMARY_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTextArea receiptArea = new JTextArea(receiptText);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(250, 250, 250));
        receiptArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        footerPanel.setOpaque(false);
        footerPanel.setPreferredSize(new Dimension(0, 44));

        JButton saveBtn = new JButton("Save Receipt (TXT)");
        saveBtn.setFont(UIUtils.FONT_BODY_BOLD);
        saveBtn.setBackground(new Color(241, 245, 249));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveReceiptToFile());

        JButton doneBtn = new JButton("Done / New Order");
        doneBtn.setFont(UIUtils.FONT_BODY_BOLD);
        doneBtn.setBackground(UIUtils.ACCENT_PRIMARY);
        doneBtn.setForeground(Color.WHITE);
        doneBtn.setFocusPainted(false);
        doneBtn.addActionListener(e -> dispose());

        footerPanel.add(saveBtn);
        footerPanel.add(doneBtn);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private String buildReceiptText(Order order, List<OrderLine> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("             DINEON-DEMAND                \n");
        sb.append("     A New Era in Meal Accessibility      \n");
        sb.append("        University Campus POS Station     \n");
        sb.append("==========================================\n");
        sb.append(String.format("Order No:   %-28s\n", order.getOrderNumber()));
        sb.append(String.format("Date/Time:  %-28s\n", order.getCreatedAt()));
        sb.append(String.format("Service:    %-28s\n", order.getServiceMode().replace('_', '-')));
        sb.append("------------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s %9s\n", "Item", "Qty", "Price", "Total"));
        sb.append("------------------------------------------\n");

        for (OrderLine line : lines) {
            String name = line.getItem().getName();
            if (name.length() > 18) {
                name = name.substring(0, 15) + "...";
            }
            sb.append(String.format("%-18s %4d %8.2f %9.2f\n",
                    name, line.getQuantity(), line.getItem().getPrice(), line.getSubtotal()));
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("Subtotal:                       ₱%9.2f\n", order.getSubtotal()));
        if (!"None".equalsIgnoreCase(order.getDiscountType()) && order.getDiscountAmount() > 0) {
            sb.append(String.format("Discount (%-14s):      -₱%9.2f\n",
                    order.getDiscountType(), order.getDiscountAmount()));
        }
        sb.append(String.format("TOTAL AMOUNT:                   ₱%9.2f\n", order.getTotalAmount()));
        sb.append("==========================================\n");
        sb.append(String.format("Payment Method:  %-24s\n", order.getPaymentMethod()));

        if (order.getPaymentReference() != null && !order.getPaymentReference().isEmpty()) {
            sb.append(String.format("Reference Info:  %-24s\n", order.getPaymentReference()));
        }

        if (order.getAmountTendered() != null && "CASH".equalsIgnoreCase(order.getPaymentMethod())) {
            sb.append(String.format("Cash Tendered:                  ₱%9.2f\n", order.getAmountTendered()));
            sb.append(String.format("Change Given:                   ₱%9.2f\n",
                    order.getChangeAmount() != null ? order.getChangeAmount() : 0.0));
        }

        sb.append("==========================================\n");
        sb.append("       Thank you for dining with us!      \n");
        sb.append("         Please come back soon!           \n");
        sb.append("==========================================\n");
        return sb.toString();
    }

    private void saveReceiptToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Receipt-" + System.currentTimeMillis() + ".txt"));
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(receiptText);
                JOptionPane.showMessageDialog(this,
                        "Receipt saved successfully to:\n" + file.getAbsolutePath(),
                        "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save receipt file: " + e.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
