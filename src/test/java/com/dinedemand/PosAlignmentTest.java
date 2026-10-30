package com.dinedemand;

import com.dinedemand.db.DatabaseManager;
import com.dinedemand.db.MenuDAO;
import com.dinedemand.db.OrderDAO;
import com.dinedemand.db.ReportDAO;
import com.dinedemand.model.MenuItem;
import com.dinedemand.model.Order;
import com.dinedemand.model.OrderLine;
import com.dinedemand.model.OrderManager;
import com.dinedemand.service.PosService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PosAlignmentTest {

    private static PosService posService;
    private static MenuDAO menuDAO;
    private static OrderDAO orderDAO;
    private static ReportDAO reportDAO;

    @BeforeAll
    public static void setup() {
        DatabaseManager.initializeDatabase();
        posService = new PosService();
        menuDAO = new MenuDAO();
        orderDAO = new OrderDAO();
        reportDAO = new ReportDAO();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    public void testDatabaseInitializationAndSeeding() {
        List<MenuItem> items = menuDAO.getAllMenuItems();
        assertNotNull(items);
        assertTrue(items.size() >= 11, "Database should contain at least the 11 initial seeded items");

        List<String> categories = menuDAO.getAllCategories();
        assertTrue(categories.contains("Mains"));
        assertTrue(categories.contains("Sides"));
        assertTrue(categories.contains("Drinks"));
        assertTrue(categories.contains("Desserts"));

        MenuItem burger = items.stream()
                .filter(i -> i.getName().equals("Classic Burger"))
                .findFirst()
                .orElse(null);
        assertNotNull(burger);
        assertEquals(150.00, burger.getPrice(), 0.001);
        assertEquals(25, burger.getStockQuantity());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    public void testOrderManagerArithmeticAndDiscounts() {
        OrderManager cart = new OrderManager();
        MenuItem burger = new MenuItem(1, "Classic Burger", "Mains", 150.00, "/images/classic-burger.png", 25);
        MenuItem fries = new MenuItem(4, "French Fries", "Sides", 60.00, "/images/fries.png", 25);

        // Add 2 burgers (300.00) and 1 fries (60.00) => Subtotal: 360.00
        assertTrue(cart.addItem(burger, 2));
        assertTrue(cart.addItem(fries, 1));
        assertEquals(360.00, cart.calculateSubtotal(), 0.001);

        // Test None discount
        cart.setDiscount("None", 0.0);
        assertEquals(0.00, cart.calculateDiscountAmount(), 0.001);
        assertEquals(360.00, cart.calculateTotal(), 0.001);

        // Test Senior Citizen / PWD 20% discount (360.00 * 0.20 = 72.00 => Total: 288.00)
        cart.setDiscount("PWD", 0.20);
        assertEquals(72.00, cart.calculateDiscountAmount(), 0.001);
        assertEquals(288.00, cart.calculateTotal(), 0.001);

        // Test Promo 10% discount (360.00 * 0.10 = 36.00 => Total: 324.00)
        cart.setDiscount("Promo Code", 0.10);
        assertEquals(36.00, cart.calculateDiscountAmount(), 0.001);
        assertEquals(324.00, cart.calculateTotal(), 0.001);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    public void testServiceModeSync() {
        OrderManager cart = new OrderManager();
        assertTrue(cart.isDineIn());
        assertEquals("DINE_IN", cart.getServiceMode());

        cart.setDineIn(false);
        assertFalse(cart.isDineIn());
        assertEquals("TAKE_OUT", cart.getServiceMode());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    public void testStockConstraintValidation() {
        OrderManager cart = new OrderManager();
        MenuItem limitedItem = new MenuItem(99, "Special Meal", "Mains", 200.00, "/images/classic-burger.png", 3);

        // Adding 2 should succeed
        assertTrue(cart.addItem(limitedItem, 2));
        assertEquals(2, cart.getQuantityForItem(limitedItem));

        // Adding 2 more (total 4) should fail because stock is only 3
        assertFalse(cart.addItem(limitedItem, 2));
        assertEquals(2, cart.getQuantityForItem(limitedItem));

        // Adding 1 more should succeed (total 3)
        assertTrue(cart.addItem(limitedItem, 1));
        assertEquals(3, cart.getQuantityForItem(limitedItem));
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    public void testAtomicOrderPersistenceAndStockDecrement() throws SQLException {
        List<MenuItem> items = posService.getCatalog("Mains", null);
        MenuItem burger = items.stream().filter(i -> i.getName().equals("Classic Burger")).findFirst().orElseThrow();
        int initialStock = burger.getStockQuantity();

        OrderManager cart = new OrderManager();
        cart.addItem(burger, 2);
        cart.setDineIn(false); // Take-out
        cart.setDiscount("Senior Citizen", 0.20);

        String orderNo = posService.getNextOrderNumber();
        Order order = new Order();
        order.setOrderNumber(orderNo);
        order.setServiceMode(cart.getServiceMode());
        order.setSubtotal(cart.calculateSubtotal());
        order.setDiscountType(cart.getDiscountType());
        order.setDiscountRate(cart.getDiscountRate());
        order.setDiscountAmount(cart.calculateDiscountAmount());
        order.setTotalAmount(cart.calculateTotal());
        order.setPaymentMethod("GCASH");
        order.setPaymentReference("GCASH-REF-TEST-9988");
        order.setAmountTendered(cart.calculateTotal());
        order.setChangeAmount(0.0);

        boolean completed = posService.completeOrder(order, cart.getItems());
        assertTrue(completed);

        // Verify stock was decremented in database
        List<MenuItem> refreshedItems = posService.getCatalog("Mains", null);
        MenuItem refreshedBurger = refreshedItems.stream().filter(i -> i.getName().equals("Classic Burger")).findFirst().orElseThrow();
        assertEquals(initialStock - 2, refreshedBurger.getStockQuantity(), "Stock must be deducted by ordered quantity");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    public void testInventoryManagementRestockAndPriceUpdate() {
        List<MenuItem> items = posService.getCatalog("Sides", null);
        MenuItem fries = items.stream().filter(i -> i.getName().equals("French Fries")).findFirst().orElseThrow();
        int stockBefore = fries.getStockQuantity();

        // Restock +15
        assertTrue(posService.restockItem(fries.getId(), 15));
        MenuItem restockedFries = posService.getCatalog("Sides", null).stream().filter(i -> i.getName().equals("French Fries")).findFirst().orElseThrow();
        assertEquals(stockBefore + 15, restockedFries.getStockQuantity());

        // Update Price to 65.00
        assertTrue(posService.updatePrice(fries.getId(), 65.00));
        MenuItem repricedFries = posService.getCatalog("Sides", null).stream().filter(i -> i.getName().equals("French Fries")).findFirst().orElseThrow();
        assertEquals(65.00, repricedFries.getPrice(), 0.001);
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    public void testSalesReportsAndAnalytics() {
        ReportDAO.SalesMetric daily = posService.getDailySales();
        assertNotNull(daily);
        assertTrue(daily.getTotalOrders() >= 1, "Should have at least 1 order recorded today");
        assertTrue(daily.getTotalRevenue() > 0.0, "Gross revenue should be greater than zero");

        ReportDAO.SalesMetric weekly = posService.getWeeklySales();
        assertNotNull(weekly);
        assertTrue(weekly.getTotalOrders() >= 1);

        ReportDAO.SalesMetric monthly = posService.getMonthlySales();
        assertNotNull(monthly);
        assertTrue(monthly.getTotalOrders() >= 1);

        List<ReportDAO.TopItem> topItems = posService.getTopSellingItems(5);
        assertNotNull(topItems);
        assertFalse(topItems.isEmpty());
        assertEquals("Classic Burger", topItems.get(0).getItemName());

        List<ReportDAO.TransactionRecord> history = posService.getTransactionHistory();
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals("TAKE_OUT", history.get(0).getServiceMode());
        assertEquals("GCASH", history.get(0).getPaymentMethod());
    }
}
