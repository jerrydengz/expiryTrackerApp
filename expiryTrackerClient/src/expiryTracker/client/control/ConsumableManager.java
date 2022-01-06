package expiryTracker.client.control;

import expiryTracker.client.model.Consumable;
import expiryTracker.client.model.ConsumableFactory;
import expiryTracker.client.model.Drink;
import expiryTracker.client.model.Food;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// https://www.baeldung.com/java-9-http-client

/**
 * Manages the state of a stored <code>List<Consumable</code> as a Singleton Class.
 * <p>
 * Responsible for making request to list, add, and remove <code>Consumable</code> objects
 * stored in the applications corresponding server.
 */
public class ConsumableManager {
    private final List<Consumable> fridge = new ArrayList<>();
    private final List<Consumable> basket = new ArrayList<>();
    private static ConsumableManager instance;
    private final Gson customGsonObj = newCustomGsonObj();
    private final ConsumableFactory consumableFactory = new ConsumableFactory();

    // objects for making Http Requests to the server
    private HttpRequest httpRequest;
    private HttpResponse<?> httpResponse;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String SERVER_URL = "http://localhost:8080";
    private static final String HEADER_NAME = "Content-Type";
    private static final String HEADER_VALUE = "application/json";

    /**
     * Gets an instance of <code>ConsumableManger</code> Singleton
     *
     * @return an instance of <code>ConsumableManger</code>
     */
    public static ConsumableManager getInstance() {
        if (instance == null) {
            instance = new ConsumableManager();
        }
        return instance;
    }

    /**
     * Getter for the current local system date of type <code>LocalDateTime</code>
     *
     * @return an instance of the current local system date of type <code>LocalDateTime</code>
     */
    public static LocalDateTime getCurrentDate() {
        return LocalDateTime.now();
    }

    /**
     * Filters <code>Consumable</code> objects from <code>List<Consumable> fridge</code> to <code>List<Consumable> basket</code>
     * depending on the specified parameter query
     *
     * @param mode a <code>String</code> specifying specific <code>Consumable</code> objects to put into <code>List<Consumable> basket</code>
     * @return a <code>List<Consumable></code> containing specific <code>Consumable</code> objects
     */
    public List<Consumable> getFilteredList(String mode) {
        basket.clear();

        // filter consumable items into a temporary list of consumables
        switch (mode) {
            case "Expired" -> basket.addAll(getListFromRequest("/listExpired"));
            case "Not Expired" -> basket.addAll(getListFromRequest("/listNonExpired"));
            case "Expiring in 7 Days" -> basket.addAll(getListFromRequest("/listExpiringIn7Days"));
            case "All" -> basket.addAll(getListFromRequest("/listAll"));
        }
        return basket;
    }

    /**
     * A GET request to determine if the server is online.
     *
     * @return a <code>boolean</code> to determine if the server is online
     */
    public boolean isServerUp() {
        makeHttpGETRequest("/ping");
        return httpResponse != null;
    }

    /**
     * Makes a POST request passing a JSON object formed by the passed in parameter values:
     *
     * @param consumableType An <code>int</code> representing the objects type of either
     *                       <code>Food</code> or <code>Drink</code>
     * @param name           Represents the name of the item of type <code>String</code>
     * @param notes          Represents details about the item of type <code>String</code>
     * @param price          Represents the price of the item of type <code>double</code>
     * @param matter         Represents the matter of the item of type <code>double</code>
     * @param expiryDate     Represents the expiry date of the item of type <code>LocalDateTime</code>
     */
    public void addConsumableItemRequest(String consumableType, String name, String notes,
                                         double price, double matter, LocalDateTime expiryDate) {

        String jsonObject = customGsonObj.toJson(consumableFactory.getInstance(
                consumableType, name, notes, price, matter, expiryDate));

        try {
            if (Objects.equals(consumableType, "Food")) {
                httpRequest = HttpRequest.newBuilder(new URI(SERVER_URL + "/addItem/Food"))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObject))
                        .header(HEADER_NAME, HEADER_VALUE)
                        .build();
            } else {
                httpRequest = HttpRequest.newBuilder(new URI(SERVER_URL + "/addItem/Drink"))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObject))
                        .header(HEADER_NAME, HEADER_VALUE)
                        .build();
            }
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException ignored) {
        }

        fridge.clear();
        fridge.addAll(fromJSONArray(httpResponse.body().toString()));
    }

    /**
     * Makes a POST request to remove an object of base type <code>Consumable</code> from <code>List<Consumable> fridge</code>
     *
     * @param itemIdToRemove A <code>UUID</code> representing the itemId of the object to be removed
     */
    public void removeConsumableItemRequest(UUID itemIdToRemove) {
        String urlRequest = SERVER_URL + "/removeItem/" + itemIdToRemove;

        try {
            httpRequest = HttpRequest.newBuilder(new URI(urlRequest))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header(HEADER_NAME, HEADER_VALUE)
                    .build();
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException ignored) {
        }

        fridge.clear();
        fridge.addAll(fromJSONArray(httpResponse.body().toString()));
    }

    /**
     * A GET request to signal the server to save the list of items stored in the control to a file server-side
     * <p>
     * This method is called on termination of the client-side program
     */
    public void exitRequest() {
        makeHttpGETRequest("/exit");
    }

    /**
     * A helper method to handle receiving and deserializing of a GET request response of a <code>List<Consumable></code>
     *
     * @param listRequestType a <code>String</code> to indicate the type of list that is requested
     * @return a <code>List <Consumable></code> representing the passed back deserialized JSON Array object
     */
    private List<Consumable> getListFromRequest(String listRequestType) {
        makeHttpGETRequest(listRequestType);
        return fromJSONArray(httpResponse.body().toString());
    }

    /**
     * Helper method for making GET requests
     *
     * @param requestedEndpointMapping a <code>String</code> indicating a specific GET request endpoint
     */
    private void makeHttpGETRequest(String requestedEndpointMapping) {
        try {
            httpRequest = HttpRequest.newBuilder(new URI(SERVER_URL + requestedEndpointMapping))
                    .GET()
                    .header(HEADER_NAME, HEADER_VALUE)
                    .build();
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException ignored) {
        }
    }

    /**
     * Constructs a custom Gson object with <code>TypeAdapters</code> for classes LocalDateTime and
     * <code>RuntimeTypeAdapterFactory</code> for <code>Consumable</code>
     * and its subclasses <code>Food</code> and <code>Drink</code>.
     *
     * @return a new custom <code>Gson</code> object for serializing/deserializing of class <code>LocalDateTime</code>
     * and subclasses <code>Food</code> & <code>Drink</code> objects of <code>Consumable</code> superclass
     */
    private Gson newCustomGsonObj() {
        // modified code provided to determine types of subclass objects reading into superclass list
        // https://mvnrepository.com/artifact/com.google.code.gson/gson-extras/2.8.5
        // https://stackoverflow.com/questions/48855124/unable-to-deserialize-with-runtimetypeadapterfactory-does-not-define-a-field
        RuntimeTypeAdapterFactory<Consumable> adapter =
                RuntimeTypeAdapterFactory
                        .of(Consumable.class, "type")
                        .registerSubtype(Food.class, Food.class.getName())
                        .registerSubtype(Drink.class, Drink.class.getName());

        return new GsonBuilder().registerTypeAdapterFactory(adapter).registerTypeAdapter(LocalDateTime.class,
                new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter,
                                      LocalDateTime localDateTime) throws IOException {
                        jsonWriter.value(localDateTime.toString());
                    }

                    @Override
                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
                        return LocalDateTime.parse(jsonReader.nextString());
                    }
                }).create();
    }

    /**
     * Converts passed in JSON Array object into a <code>List<Conusmable></code>.
     * <p>
     * Reassigns the object's subclass name back <code>Consumable</code> field <code>type</code> to each object
     * for <code>Gson</code> to serialize after deserializing for the 2nd and subsequent runs of the application.
     *
     * @param jsonArrayObject a <code>String</code> JSON Array object to be deserialized
     */
    private List<Consumable> fromJSONArray(String jsonArrayObject) {

        // convert JSON file to a List<Consumable>
        // https://attacomsian.com/blog/gson-read-json-file
        // https://stackoverflow.com/questions/18544133/parsing-json-array-into-java-util-list-with-gson
        Type typeListConsumable = new TypeToken<List<Consumable>>() {
        }.getType();
        List<Consumable> list = customGsonObj.fromJson(jsonArrayObject, typeListConsumable);

        // set the type back to each object for gson to serialize after deserializing
        for (Consumable consumable : fridge) {
            if (consumable instanceof Food) {
                consumable.setType(consumable.getClass().getName());
            } else if (consumable instanceof Drink) {
                consumable.setType(consumable.getClass().getName());
            }
        }

        return list;
    }
}

