package subclasses;

public class ResponseWrapper {

    private Report report;

    public ResponseWrapper()
    {

    }


    @Override
    public String toString() {
        return report.toString();
    }

    public Food getFood()
    {
        return report.getFood();
    }

}