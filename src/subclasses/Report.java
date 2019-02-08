package subclasses;

public class Report {

    public Food getFood() {
        return food;
    }


    public Food food;

    public Report()
    {
    }


    @Override
    public String toString() {
        return food.getReportString();
    }
}