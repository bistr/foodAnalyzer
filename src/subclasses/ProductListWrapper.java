package subclasses;

import com.google.gson.annotations.SerializedName;

import java.util.Queue;
import java.util.Set;

public class ProductListWrapper {
    @SerializedName("list")
    public ProductList productList;

    public ProductListWrapper() {
    }

    public ProductList getProductList()
    {
        return productList;
    }

    @Override
    public String toString() {
        return productList.toString();
    }
}
