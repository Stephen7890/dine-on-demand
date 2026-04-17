public class MenuItem {
    private final String name;
    private final double price;
    private final String category;
    private final String image;

    public MenuItem(String name, double price, String category, String image) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return name + " ($" + String.format("%.2f", price) + ")";
    }
}
