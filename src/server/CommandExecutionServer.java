package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import enums.Command;
import enums.Message;
import exceptions.UnavailableLogException;
import subclasses.Food;
import subclasses.ProductList;
import subclasses.ProductListWrapper;
import subclasses.ResponseWrapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;


public class CommandExecutionServer implements AutoCloseable {

    public static final int SERVER_PORT = 4444;
    private static final int BUFFER_SIZE = 1024;
    private static String API_URL = "https://api.nal.usda.gov/ndb/";
    private static String API_KEY = "7e4BU7gCHAFgoIFLRsACWZmMv6dWa16vOcXv3ece";
    private static final String logPath = "resources/log.txt";
    private static final String cachePath = "resources/cache.txt";
    public static boolean runServer = true;
    private static BufferedWriter logWriter;
    private static Map<String, Food> cachedItemsMap;
    private final String lineEnd = "\nEND";
    private Selector selector;
    private ByteBuffer commandBuffer;
    private ServerSocketChannel ssc;
    private HttpClient client;
    private File logFile;
    private File cacheFile;
    private boolean cacheHasChanged;


    public CommandExecutionServer(int port) throws IOException {
        selector = Selector.open();
        commandBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(port));
        client = HttpClient.newHttpClient();
        logFile = new File(logPath);
        cacheFile = new File(cachePath);
        logFile.createNewFile();
        cacheFile.createNewFile();
        cachedItemsMap =  new HashMap<>();
        FileOutputStream logOutputStream = new FileOutputStream(new File(
                logPath), true);
        logWriter = new BufferedWriter(new OutputStreamWriter(logOutputStream));
    }

    public CommandExecutionServer() throws IOException {
        selector = Selector.open();
        commandBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(4444));
        client = HttpClient.newHttpClient();
        logFile = new File(logPath);
        cacheFile = new File(cachePath);
        logFile.createNewFile();
        cacheFile.createNewFile();

        FileOutputStream logOutputStream = new FileOutputStream(new File(
                logPath), true);
        logWriter = new BufferedWriter(new OutputStreamWriter(logOutputStream));
    }

    private static void log(Exception exception) {
        try {
            if (exception.getMessage() != null) {
                logWriter.write(exception.getMessage());
                logWriter.write(System.lineSeparator());
            }
            if (exception.getStackTrace() != null) {
                logWriter.write(Arrays.toString(exception.getStackTrace()));
                logWriter.write(System.lineSeparator());
            }
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.out.println(Message.CANT_WRITE_LOG);
            throw new UnavailableLogException(e);
        }
    }

    private static void log(Message message) {
        log(message.toString());
    }

    private static void log(String string) {
        System.out.println(string);
        try {
            logWriter.write(string);
            logWriter.write(System.lineSeparator());
            logWriter.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.out.println(Message.CANT_WRITE_LOG);
            throw new UnavailableLogException(e);
        }
    }

    public static void main(String[] args) {
        try (CommandExecutionServer es = new CommandExecutionServer(SERVER_PORT)) {
            es.start();
        } catch (ConnectException ce) {
            log(Message.CANT_CONNECT);
            log(ce);
        } catch (IOException ioe) {
            System.out.println(Message.CANT_WRITE_LOG);

        } catch (Exception e) {
            log(Message.ERROR);
            log(e);
        }
    }

    public static void stop() {
        runServer = false;
    }

    /*public static void setServerPort(int serverPort) {
        SERVER_PORT = serverPort;
    }

    public static void setBufferSize(int bufferSize) {
        BUFFER_SIZE = bufferSize;
    }

    public static void setApiUrl(String apiUrl) {
        API_URL = apiUrl;
    }

    public static void setApiKey(String apiKey) {
        API_KEY = apiKey;
    }*/

    public void start() throws IOException {
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        //Utilities.setUp();
        readCache();
        ServerCommandReader commandReader = new ServerCommandReader();
        Thread commandReaderThread = new Thread(commandReader);
        commandReaderThread.setDaemon(true);
        commandReaderThread.start();
        while (runServer) {
            int readyChannels = selector.select();
            if (readyChannels <= 0) continue;

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext() && runServer) {
                SelectionKey key = keyIterator.next();
                if (key.isReadable() && runServer) {
                    this.read(key);
                } else if (key.isAcceptable() && runServer) {
                    this.accept(key);
                }
                keyIterator.remove();
            }
        }
        writeCacheToFile();
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) {

        SocketChannel sc = (SocketChannel) key.channel();
        try {
            commandBuffer.clear();
            int r = sc.read(commandBuffer);
            if (r == -1) return;

            commandBuffer.flip();
            String message = Charset.forName("UTF-8").decode(commandBuffer).toString();
            String result = executeCommand(message);
            result += lineEnd;
            log("message:" + message);
            log("result:" + result);
            commandBuffer.clear();
            byte[] bytesToSend = (result + System.lineSeparator()).getBytes();
            commandBuffer = ByteBuffer.allocate(bytesToSend.length);
            commandBuffer.put(bytesToSend);
            commandBuffer.flip();
            sc.write(commandBuffer);
        } catch (IOException e) {
            stop();
            log(Message.FAIL_TO_INIT);
            log(e);
        }
    }

    public String display(Food food) {
        if (food != null) return food.getReportString();
        return Message.NOT_FOUND.toString();
    }

    private String display(ProductList list) {
        if (list != null) return list.toString();
        return Message.NOT_FOUND.toString();
    }

    private String displayFoodReport(String ndbno) {
        Food result = getFoodByNDBNO(ndbno);
        return display(result);
    }

    private String displayFoodList(String words) {
        ProductList result = getFoodListByName(words);
        return display(result);
    }

    private String displayFoodWithoutIngredient(String command) {
        String[] cmdParts = command.split("\\s");
        int commandWords = cmdParts.length;
        if (commandWords == 1) return Message.MISSING_ARGUMENT.toString();
        String ingredientToAvoid = cmdParts[commandWords - 1];
        int commandLength = command.length();
        command = command.substring(0, commandLength - ingredientToAvoid.length());
        ProductList safeFoods = getFoodListWithout(command, ingredientToAvoid);
        return display(safeFoods);
    }

    private String displayFoodByImage(String imagePath) {
        if (!new File(imagePath).exists()) return Message.CANT_OPEN_IMAGE.toString();
        String upc = extractUPCFromImage(imagePath);
        if (upc == null) return Message.NOT_FOUND.toString();
        return displayFoodByUPC(upc);
    }

    private String displayFoodByUPC(String upc) {
        Food result = getFoodByUPC(upc);
        return display(result);
    }

    private String executeCommand(String recvMsg) {
        if (recvMsg == null) return null;
        String[] cmdParts = recvMsg.split("\\s");
        if (cmdParts.length == 1) {
            return Message.MISSING_ARGUMENT.toString();
        }

        String command = cmdParts[0].trim();

        if (command.equalsIgnoreCase(Command.GET_FOOD_REPORT.toString())) {
            return displayFoodReport(cmdParts[1]);
        }
        if (command.equalsIgnoreCase(Command.GET_FOOD.toString())) {
            return displayFoodList(recvMsg.substring(command.length() + 1));
        }
        if (command.equalsIgnoreCase(Command.GET_FOOD_BY_BARCODE.toString())) {
            String argumentName = "--upc";
            if (cmdParts[1].contains(argumentName)) {
                String upc = cmdParts[1].substring(argumentName.length() + 1);
                return displayFoodByUPC(upc);
            }
            String imagePath = cmdParts[1].substring(argumentName.length() + 1);
            return displayFoodByImage(imagePath);
        }
        if (command.equalsIgnoreCase(Command.GET_FOOD_WITHOUT.toString())) {
            int length = cmdParts.length;
            final int minimumArgumentsCount = 3;
            if (length < minimumArgumentsCount) return Message.MISSING_ARGUMENT.toString();

            return displayFoodWithoutIngredient(recvMsg.substring(command.length() + 1));
        } else {
            return Message.UNKNOWN_COMMAND.toString();
        }
    }

    private void addToCache(ResponseWrapper wrapper) {
        addToCache(wrapper.getFood());
    }

    public void addToCache(ProductList productList) {
        productList.getProducts().forEach(food -> addToCache(getFoodByNDBNO(food.getNdbno())));
    }

    public void addToCache(Food food) {
        cachedItemsMap.put(food.getNdbno(), food);
        cacheHasChanged = true;
    }

    private void readCache() {
        Gson gson = new Gson();
        String jsonCache;
        try (InputStream cacheInputStream = new FileInputStream(new File(
                cachePath))) {
            BufferedReader cacheReader = new BufferedReader(new InputStreamReader(cacheInputStream));
            jsonCache = cacheReader.readLine();
            if (jsonCache == null) return;
            log(jsonCache);
            List<Food> cachedFoodList;
            cachedFoodList = gson.fromJson(jsonCache, new TypeToken<List<Food>>() {
            }.getType());
            cachedFoodList.forEach(food -> cachedItemsMap.put(food.getNdbno(), food));
        } catch (IOException ioe) {
            log(Message.CANT_READ_CACHE);
            log(ioe);
        }
    }

    private void writeCacheToFile() {
        if (!cacheHasChanged) return;
        Gson gson = new GsonBuilder().create();

        try (FileOutputStream cacheOutputStream = new FileOutputStream(cacheFile);
             BufferedWriter cacheWriter = new BufferedWriter(new OutputStreamWriter(cacheOutputStream))
        ) {
            gson.toJson(cachedItemsMap.values(), cacheWriter);
            cacheWriter.flush();
        } catch (IOException ioe) {
            log(Message.CANT_WRITE_CACHE);
            log(ioe);
        }

    }

    public Food getFoodByUPC(String upc) {
        ProductList productList = new ProductList();
        cachedItemsMap.values().stream().filter(f -> f.getName().contains(upc)).forEach(productList::addProduct);
        if (productList.getSize() == 0) return null;
        return productList.getProducts().iterator().next();

    }

    private String sendRequest(String URL) {
        String result;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();
        log(Message.SENDING_REQUEST);
        log(request.toString());
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404 || response.statusCode() == 400) return null;
            result = response.body();
        } catch (Exception e) {
            log(e);
            return Message.ERROR_REQUEST.toString();
        }

        return result;

    }

    public ProductList getFoodListByName(String name) {
        String[] searchWords = name.trim().split("\\s");
        String searchTerm;
        if (searchWords.length == 1) searchTerm = name.trim();
        else {
            searchTerm = String.join("+", searchWords);
        }
        String URL = API_URL + "/search?q=" + searchTerm + "&api_key=" + API_KEY;
        Gson gson = new Gson();
        String jsonResponse = sendRequest(URL);
        if (jsonResponse == null) return null;
        ProductList result = gson.fromJson(jsonResponse, ProductListWrapper.class).getProductList();
        if (result == null) return null;
        addToCache(result);

        return result;
    }

    public Food getFoodByNDBNO(String ndbno) {
        if (cachedItemsMap.containsKey(ndbno)) {
            return cachedItemsMap.get(ndbno);
        }
        String URL = API_URL + "reports/?ndbno=" + ndbno + "&api_key=" + API_KEY;
        String jsonResponse = sendRequest(URL);
        if (jsonResponse == null) return null;
        Gson gson = new Gson();
        ResponseWrapper result = gson.fromJson(jsonResponse, ResponseWrapper.class);
        addToCache(result);
        return result.getFood();
    }

    public ProductList getFoodListWithout(String name, String ingredient) {
        ProductList allFoods = getFoodListByName(name);
        if (allFoods == null) return null;
        ProductList safeFoods = new ProductList();

        for (Food food : allFoods.getProducts()) {
            Food tmp = getFoodByNDBNO(food.getNdbno());
            if (tmp == null) continue;
            if (!tmp.contains(ingredient)) safeFoods.addProduct(tmp);
        }
        return safeFoods;
    }

    @Override
    public void close() throws Exception {
        ssc.close();
        selector.close();
    }

    public String extractUPCFromImage(String path) {
        File imgFile = new File(path);
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(imgFile);
        } catch (IOException ioe) {
            log(Message.CANT_OPEN_IMAGE);
            log(ioe);
            return null;
        }
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            log(Message.NO_BARCODE);
            return null;
        }
    }

    public int getCacheSize() {
        return cachedItemsMap.size();
    }

}