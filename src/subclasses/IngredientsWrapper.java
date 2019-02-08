package subclasses;

public class IngredientsWrapper {

    private String desc;


    @Override
    public String toString() {
        if(desc==null) return "";
        StringBuilder res = new StringBuilder();
        for(String ingredient:desc.replaceAll("[.]", ",").split(",\\s"))
        {
            res.append(ingredient);
            res.append("\n");
        }
        return res.toString();
    }

    public boolean contains(String ingredient)
    {
        return desc.toLowerCase().contains(ingredient.toLowerCase());
    }
}
