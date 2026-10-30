# DineOn-Demand: A New Era in Meal Accessibility

Modern Point of Sale (POS), Inventory Control, and Sales Analytics System for University Course **CC 105 (Information Management)**.

---

## 🛠️ Tech Stack & Prerequisites
- **Language / Runtime:** Java 17+ (LTS) or Java 21+ / 27
- **Build Tool:** Apache Maven 3.8+
- **UI Toolkit:** Java Swing with FlatLaf Light Look & Feel
- **Database Engine:** Embedded SQLite with JDBC Driver (`dine_on_demand.db`)
- **Testing:** JUnit 5 (Jupiter Engine)

---

## 🚀 Quick Start Guide

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Execute Automated Verification Tests
```bash
mvn test
```

### 3. Launch the Application
```bash
mvn exec:java
```

---

## 📦 Project Architecture

```
dine-on-demand/
├── pom.xml
├── dine_on_demand.db                  # Local SQLite Embedded Database (Auto-initialized)
├── src/
│   ├── main/
│   │   ├── java/com/dinedemand/
│   │   │   ├── DineOnDemandApp.java  # Main Application Launcher
│   │   │   ├── model/                 # Domain Entities (MenuItem, OrderLine, Order, OrderManager)
│   │   │   ├── db/                    # Persistence Layer (DatabaseManager, MenuDAO, OrderDAO, ReportDAO)
│   │   │   ├── service/               # Application Business Logic (PosService)
│   │   │   └── ui/                    # Presentation Layer (DineOnDemandApp, PosTerminalPanel, InventoryPanel, ReportsPanel, etc.)
│   │   └── resources/
│   │       ├── fonts/                 # Custom Geist TTF Fonts (Loaded via Classpath)
│   │       └── images/                # Menu Product Thumbnails (Loaded via Classpath)
│   └── test/
│       └── java/com/dinedemand/       # Automated Architecture & Proposal Alignment Suite
```

---

## ✨ System Features
1. **POS Terminal**: Live product catalog, real-time search, category filter pills, stock availability validation, inline cart quantity stepper, and Dine-In vs. Take-Out toggle.
2. **Discounts & Promos**: Standard PWD (20%), Senior Citizen (20%), and Promotional Campaign (10%) discounts using exact-precision fixed-point calculations (`BigDecimal`).
3. **Payment Processing**: Modal checkout supporting Cash (with tendered validation and live change calculation), GCash, and Card with transaction reference logging.
4. **Fiscal Receipts**: Itemized on-screen slips with one-click TXT export.
5. **Inventory Management**: Real-time stock tracking, out-of-stock badges, restocking steppers, price updates, and new item creation.
6. **Sales Analytics**: Real-time KPI cards (Gross Revenue, Total Orders, Average Ticket) across Daily, Weekly, and Monthly periods with top-selling item rankings and complete transaction logs.
