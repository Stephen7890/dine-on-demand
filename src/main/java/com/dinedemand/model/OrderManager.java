package com.dinedemand.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * State manager for the active customer order/cart session.
 * Enforces fixed-point precision financial calculations using BigDecimal.
 */
public class OrderManager {
    private final List<OrderLine> items;
    private boolean isDineIn = true;
    private String discountType = "None";
    private double discountRate = 0.0;

    public OrderManager() {
        this.items = new ArrayList<>();
    }

    public synchronized boolean addItem(MenuItem item, int quantity) {
        if (quantity <= 0) return false;

        int currentQtyInCart = getQuantityForItem(item);
        if (currentQtyInCart + quantity > item.getStockQuantity()) {
            return false; // Exceeds available stock
        }

        for (OrderLine line : items) {
            if (line.getItem().getId() == item.getId() || line.getItem().getName().equals(item.getName())) {
                line.addQuantity(quantity);
                return true;
            }
        }

        items.add(new OrderLine(item, quantity));
        return true;
    }

    public synchronized void decreaseItem(MenuItem item, int quantity) {
        for (int i = 0; i < items.size(); i++) {
            OrderLine line = items.get(i);
            if (line.getItem().getId() == item.getId() || line.getItem().getName().equals(item.getName())) {
                int newQty = line.getQuantity() - quantity;
                if (newQty <= 0) {
                    items.remove(i);
                } else {
                    line.setQuantity(newQty);
                }
                return;
            }
        }
    }

    public synchronized void updateQuantity(MenuItem item, int newQty) {
        if (newQty <= 0) {
            removeItem(item);
            return;
        }

        int cappedQty = Math.min(newQty, item.getStockQuantity());
        for (OrderLine line : items) {
            if (line.getItem().getId() == item.getId() || line.getItem().getName().equals(item.getName())) {
                line.setQuantity(cappedQty);
                return;
            }
        }

        items.add(new OrderLine(item, cappedQty));
    }

    public synchronized void removeItem(MenuItem item) {
        items.removeIf(line -> line.getItem().getId() == item.getId() || line.getItem().getName().equals(item.getName()));
    }

    public synchronized int getQuantityForItem(MenuItem item) {
        for (OrderLine line : items) {
            if (line.getItem().getId() == item.getId() || line.getItem().getName().equals(item.getName())) {
                return line.getQuantity();
            }
        }
        return 0;
    }

    public synchronized List<OrderLine> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public boolean isDineIn() {
        return isDineIn;
    }

    public void setDineIn(boolean dineIn) {
        isDineIn = dineIn;
    }

    public String getServiceMode() {
        return isDineIn ? "DINE_IN" : "TAKE_OUT";
    }

    public void setDiscount(String type, double rate) {
        this.discountType = type != null ? type : "None";
        this.discountRate = Math.max(0.0, rate);
    }

    public String getDiscountType() {
        return discountType;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public BigDecimal calculateSubtotalBigDecimal() {
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (OrderLine line : items) {
            subtotal = subtotal.add(line.getSubtotalBigDecimal());
        }
        return subtotal;
    }

    public double calculateSubtotal() {
        return calculateSubtotalBigDecimal().doubleValue();
    }

    public BigDecimal calculateDiscountAmountBigDecimal() {
        BigDecimal subtotal = calculateSubtotalBigDecimal();
        BigDecimal rate = BigDecimal.valueOf(discountRate);
        return subtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public double calculateDiscountAmount() {
        return calculateDiscountAmountBigDecimal().doubleValue();
    }

    public BigDecimal calculateTotalBigDecimal() {
        BigDecimal total = calculateSubtotalBigDecimal().subtract(calculateDiscountAmountBigDecimal());
        return total.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public double calculateTotal() {
        return calculateTotalBigDecimal().doubleValue();
    }

    public synchronized void clear() {
        items.clear();
        isDineIn = true;
        discountType = "None";
        discountRate = 0.0;
    }
}
