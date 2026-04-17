import java.util.ArrayList;
import java.util.List;

public class MenuData {
    public static List<MenuItem> getInitialMenu() {
        List<MenuItem> menu = new ArrayList<>();
        menu.add(new MenuItem("Classic Burger", 150.00, "Mains", "images/classic-burger.png"));
        menu.add(new MenuItem("Cheeseburger", 180.00, "Mains", "images/cheese-burger.png"));
        menu.add(new MenuItem("Chicken Burger", 170.00, "Mains", "images/chicken-burger.png"));
        menu.add(new MenuItem("French Fries", 60.00, "Sides", "images/fries.png"));
        menu.add(new MenuItem("Onion Rings", 75.00, "Sides", "images/onion-rings.png"));
        menu.add(new MenuItem("Coleslaw", 45.00, "Sides", "images/coleslaw.png"));
        menu.add(new MenuItem("Coke 500ml", 40.00, "Drinks", "images/coke.png"));
        menu.add(new MenuItem("Iced Tea", 35.00, "Drinks", "images/iced-tea.png"));
        menu.add(new MenuItem("Mineral Water", 20.00, "Drinks", "images/water.png"));
        menu.add(new MenuItem("Choco Sundae", 55.00, "Desserts", "images/choco-sundae.png"));
        menu.add(new MenuItem("Apple Pie", 65.00, "Desserts", "images/apple-pie.png"));
        return menu;
    }
}
