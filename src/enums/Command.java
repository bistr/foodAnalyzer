package enums;

public enum Command {
    GET_FOOD, GET_FOOD_REPORT, GET_FOOD_WITHOUT,GET_FOOD_BY_BARCODE,QUIT,STOP;

    public String toString(){
        switch(this){
            case GET_FOOD :
                return "get-food";
            case GET_FOOD_REPORT :
                return "get-food-report";
            case GET_FOOD_WITHOUT :
                return "get-food-without";
            case GET_FOOD_BY_BARCODE:
                return "get-food-by-barcode";
            case QUIT:
                return "quit";
            case STOP:
                return "stop";
        }
        return null;
    }

}
