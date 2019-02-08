import enums.Message;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import server.CommandExecutionServer;
import subclasses.Food;
import subclasses.ProductList;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerTest {

    private static CommandExecutionServer server;


    @BeforeClass
    public static void setUp() throws IOException {
        server = new CommandExecutionServer(4444);
        File cacheFile = new File("resources/cache.txt");
        if (cacheFile.exists()) cacheFile.delete();
    }


    //public void after() {
        //server.writeCacheToFile();
    //}

    @Test
    public void getFoodByNDBNOTest() {
        final String ndbno = "01009";
        Food result = server.getFoodByNDBNO(ndbno);
        assertEquals(ndbno, result.getNdbno());
        final String badNdbno = "12345";
        result = server.getFoodByNDBNO(badNdbno);
        assertNull(result);
    }

    @Test
    public void getFoodListTest() {
        final String name = "nutella";
        ProductList result = server.getFoodListByName(name);
        for (Food food : result.getProducts()) {
            assertTrue(food.getName().toLowerCase().contains(name.toLowerCase()));
        }
    }

    @Test
    public void getFoodWithoutTest() {
        final String name = "sauerkraut salad";
        final String allergen = "cilantro";
        ProductList result = server.getFoodListWithout(name, allergen);
        for (Food food : result.getProducts()) {
            assertTrue(!food.contains(allergen));
        }

    }

    @Test
    public void multipleWordSearchTest() {
        final String name = "sauerkraut salad";
        String[] nameParts = name.toLowerCase().split("\\s");
        ProductList result = server.getFoodListByName(name);
        for (Food food : result.getProducts()) {
            for (String namePart : nameParts) {
                assertTrue(food.getName().toLowerCase().contains(namePart));
            }

        }
    }

    @Test
    public void addToCacheTest() {
        String ndbno = "35196";
        int oldSize = server.getCacheSize();
        Food food = server.getFoodByNDBNO(ndbno);
        server.addToCache(food);
        assertEquals(oldSize + 1, server.getCacheSize());
        oldSize = server.getCacheSize();
        server.addToCache(food);
        assertEquals(oldSize, server.getCacheSize());
    }

    @Test
    public void getFoodByUPC() {
        final String testNDBNO = "123";
        final String testUPC = "567083";
        final String testName = "test upc:" + testUPC;
        final String testManufacturer = "Coca-Cola";
        Food testFood = new Food(testNDBNO, testName, testManufacturer);
        server.addToCache(testFood);
        Food result = server.getFoodByUPC(testUPC);
        assertEquals(result.getName(), testName);
    }

    @Test
    public void extractUPCTest()
    {
        final String imagePath="resources/image.png";
        String extractedUPC = server.extractUPCFromImage(imagePath);
        String expectedUPC = "009800800124";
        assertEquals(expectedUPC,extractedUPC);
    }

    @Test
    public void getByImagePathTest()
    {
        final String name = "nutella";
        final String imagePath = "resources/image.png";
        ProductList nutellaFoods = server.getFoodListByName(name);
        server.addToCache(nutellaFoods);
        String extractedUPC = server.extractUPCFromImage(imagePath);
        Food result = server.getFoodByUPC(extractedUPC);
        assertTrue(result.getName().toLowerCase().contains(name.toLowerCase()));

    }

    @Test
    public void displayFoodTest()
    {
        Food food = null;
        final String testNDBNO = "01009";
        assertEquals(Message.NOT_FOUND.toString(),server.display(food));
        food = server.getFoodByNDBNO(testNDBNO);
        assertEquals(food.getReportString(),server.display(food));
    }


}
