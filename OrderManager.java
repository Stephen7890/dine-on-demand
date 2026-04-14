import java.util.ArrayList;
import java.util.List;

public class OrderManager {
    private final List<OrderLine> items;
    private boolean isDineIn = true;
    private double discountRate = 0.0; // 0.20 for 20%
    private String discountType = "None";

    public OrderManager() {
        this.items = new ArrayList<>();
    }

    public void addItem(MenuItem item, int quantity) {
        for (OrderLine line : items) {
            if (line.getItem().getName().equals(item.getName())) {
                line.addQuantity(quantity);
                return;
            }
        }
        items.add(new OrderLine(item, quantity));
    }

    public void decreaseItem(MenuItem item, int quantity) {
        for (int i = 0; i < items.size(); i++) {
            OrderLine line = items.get(i);
            if (line.getItem().getName().equals(item.getName())) {
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

    public int getQuantityForItem(MenuItem item) {
        for (OrderLine line : items) {
            if (line.getItem().getName().equals(item.getName())) {
                return line.getQuantity();
            }
        }
        return 0;
    }

    public void removeItem(MenuItem item) {
        items.removeIf(line -> line.getItem().getName().equals(item.getName()));
    }

    public List<OrderLine> getItems() {
        return items;
    }

    public void setDineIn(boolean dineIn) {
        isDineIn = dineIn;
    }

    public boolean isDineIn() {
        return isDineIn;
    }

    public void setDiscount(String type, double rate) {
        this.discountType = type;
        this.discountRate = rate;
    }

    public String getDiscountType() {
        return discountType;
    }

    public double calculateSubtotal() {
        double subtotal = 0;
        for (OrderLine line : items) {
            subtotal += line.getSubtotal();
        }
        return subtotal;
    }

    public double calculateDiscountAmount() {
        return calculateSubtotal() * discountRate;
    }

    public double calculateTotal() {
        return calculateSubtotal() - calculateDiscountAmount();
    }

    public void clear() {
        items.clear();
        isDineIn = true;
        discountRate = 0.0;
        discountType = "None";
    }
}
