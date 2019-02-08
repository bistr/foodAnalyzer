package subclasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class Food {

    public String ndbno;
    public String name;
    @SerializedName("manu")
    public String manufacturer;
    public IngredientsWrapper ing;
    public List<Nutrient> nutrients = null;
    public String upc;


    public Food(String ndbno, String name, String manufacturer) {
        this.ndbno = ndbno;
        this.name = name;
        this.manufacturer = manufacturer;
    }

    public Food() {
    }

    public String getNdbno() {
        return ndbno;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }


    @Override
    public String toString() {
        return "------\nNAME:\t"+getName()+"\n------\nNDBNO:\t"+getNdbno()+"\n------\nMANUFACTURER:\t"+getManufacturer();
    }

    public String getReportString()
    {
        return "------\nNAME:\t"+getName()+"\n------\nINGREDIENTS:\n------\n"+getIngredients()+"------\nNUTRIENTS:\n------\n"+getNutrients();
    }

    public String getNutrients()
    {
        if(nutrients==null) return null;
        StringBuilder res = new StringBuilder();
        for(Nutrient nutrient: nutrients)
            if (nutrient.name.contains("Energy") ||
                    nutrient.name.contains("Protein") ||
                    nutrient.name.contains("lipid") ||
                    nutrient.name.contains("Carbohydrate") ||
                    nutrient.name.contains("Fiber")) {
                res.append(nutrient.name);
                res.append("\t");
                res.append(nutrient.value);
                res.append(nutrient.unit);
                res.append("\n");
            }
        return res.toString();
    }

    public String getIngredients()
    {
        if(ing==null) return "";
        return ing.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Food)) return false;
        Food food = (Food) o;
        return ndbno.equals(food.ndbno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ndbno);
    }

    public boolean contains(String ingredient)
    {
        //just to be safe
        if(ing==null) return true;
        return ing.contains(ingredient);
    }

}