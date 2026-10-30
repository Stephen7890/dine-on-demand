package com.dinedemand.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain model representing an itemized order line with financial arithmetic.
 */
public class OrderLine {
    private final MenuItem item;
    private int quantity;

    public OrderLine(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = Math.max(1, quantity);
    }

    public MenuItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public void addQuantity(int amount) {
        this.quantity = Math.max(1, this.quantity + amount);
    }

    public void decrement() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    public BigDecimal getSubtotalBigDecimal() {
        return BigDecimal.valueOf(item.getPrice())
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public double getSubtotal() {
        return getSubtotalBigDecimal().doubleValue();
    }

    public String getFormattedSubtotal() {
        return String.format("₱%.2f", getSubtotal());
    }
}
