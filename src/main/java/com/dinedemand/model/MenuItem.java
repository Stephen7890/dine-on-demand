package com.dinedemand.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain model representing a product in the DineOn-Demand menu catalog.
 */
public class MenuItem {
    private int id;
    private String name;
    private String category;
    private double price;
    private String imagePath;
    private int stockQuantity;

    public MenuItem(int id, String name, String category, double price, String imagePath, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.imagePath = imagePath;
        this.stockQuantity = stockQuantity;
    }

    public MenuItem(String name, String category, double price, String imagePath, int stockQuantity) {
        this(0, name, category, price, imagePath, stockQuantity);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public BigDecimal getPriceBigDecimal() {
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = Math.max(0, stockQuantity);
    }

    public boolean isAvailable() {
        return stockQuantity > 0;
    }

    public String getFormattedPrice() {
        return String.format("₱%.2f", price);
    }

    @Override
    public String toString() {
        return name + " (" + getFormattedPrice() + ")";
    }
}
