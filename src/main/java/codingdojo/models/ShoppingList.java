package codingdojo.models;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShoppingList {
    private final List<String> products;

    public ShoppingList(String... products) {
        this.products = Arrays.asList(products);
    }

    public List<String> getProducts() {
        return products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShoppingList that = (ShoppingList) o;

        return Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products);
    }

}
