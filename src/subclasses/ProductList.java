package subclasses;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ProductList {
    @SerializedName("item")
    private Set<Food> products;

    public ProductList() {
        products = new HashSet<>();
    }

    public void addProduct(Food food)
    {
        products.add(food);
    }

    public int getSize()
    {
        return products.size();
    }

    public Set<Food> getProducts() {
        return products;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for(Food product: products)
        {
            res.append(product.toString());
            res.append("\n");
        }
        return res.toString();
    }

}
