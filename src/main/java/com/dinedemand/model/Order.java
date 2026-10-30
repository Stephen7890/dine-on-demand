package com.dinedemand.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a finalized customer transaction.
 */
public class Order {
    private int id;
    private String orderNumber;
    private String serviceMode; // "DINE_IN" or "TAKE_OUT"
    private double subtotal;
    private String discountType;
    private double discountRate;
    private double discountAmount;
    private double totalAmount;
    private String paymentMethod; // "CASH", "GCASH", "CARD"
    private String paymentReference;
    private Double amountTendered;
    private Double changeAmount;
    private String createdAt;
    private List<OrderLine> lines = new ArrayList<>();

    public Order() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = BigDecimal.valueOf(subtotal).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = BigDecimal.valueOf(discountAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Double getAmountTendered() {
        return amountTendered;
    }

    public void setAmountTendered(Double amountTendered) {
        this.amountTendered = amountTendered != null ?
                BigDecimal.valueOf(amountTendered).setScale(2, RoundingMode.HALF_UP).doubleValue() : null;
    }

    public Double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Double changeAmount) {
        this.changeAmount = changeAmount != null ?
                BigDecimal.valueOf(changeAmount).setScale(2, RoundingMode.HALF_UP).doubleValue() : null;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }
}
