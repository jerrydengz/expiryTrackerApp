package expiryTracker.webappserver.control;

import expiryTracker.webappserver.control.utils.RuntimeTypeAdapterFactory;
import expiryTracker.webappserver.model.Consumable;
import expiryTracker.webappserver.model.Drink;
import expiryTracker.webappserver.model.Food;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Manages the state of a stored <code>List<Consumable</code> as a Singleton Class.
 * <p>
 * Responsible for listing, adding, and removal of <code>Consumable</code> objects
 * stored in private field <code>List<Consumable> fridge</code>.
 * <p>
 * Responsible for reading to and from a .json file using <code>Gson</code>
 * regarding the <code>List<Consumable></code>
 * <p>
 */
public class ConsumableManager {
    private List<Consumable> fridge = new ArrayList<>();
    private final List<Consumable> basket = new ArrayList<>();
    public final Gson customGsonObj = newCustomGsonObj();
    private final String FILE_PATH = ".\\itemList.json";
    private static ConsumableManager instance;

    /**
     * Constructs a private <code>ConsumableManager</code> Singleton for the purpose
     * of reading from a JSON file to load (if existing)
     * <code>Consumable</code> objects of type subclass type <code>Food</code> or <code>Drink</code>
     */
    private ConsumableManager() {
        readInFile();
    }

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
     * Getter for an instance of the main list of <code>Consumable</code> objects
     *
     * @return an instance of the main list of <code>Consumable</code> objects
     */
    public List<Consumable> getFridge() {
        return this.fridge;
    }

    /**
     * Inserts an object of base type <code>Consumable</code> into the <code>List<Consumable> fridge </code>
     *
     * @param item a <code>Consumable</code> object representing either a Food or Drink item
     */
    public void addConsumableItem(Consumable item) {
        fridge.add(item);
    }

    /**
     * Removes an object of base type <code>Consumable</code> from <code>List<Consumable> fridge</code>
     *
     * @param consumableItem An object of base type <code>Consumable</code> located in
     *                       <code>List<Consumable> fridge</code>
     */
    public void removeConsumableItem(Consumable consumableItem) {
        fridge.remove(consumableItem);
    }

    /**
     * Sorts the private field <code>List<Consumable> fridge</code> in natural order
     * * through the use of the <code>Comparable</code> interface, defined by
     * * the method <code>compareTo(<code>Consumable</code> object)</code>
     */
    public void sortFridge() {
        Collections.sort(fridge);
    }

    /**
     * Filters <code>Consumable</code> objects from <code>List<Consumable> fridge</code> to <code>List<Consumable> basket</code>
     * depending on the specified parameter query
     *
     * @param mode a <code>String</code> specifying specific <code>Consumable</code> objects to put into <code>List<Consumable> basket</code>
     * @return a <code>List<Consumable></code> containing specific <code>Consumable</code> objects
     */
    public List<Consumable> filterList(String mode) {
        basket.clear();
        LocalDate todayDate = LocalDateTime.now().toLocalDate();

        // filter consumable items into a temporary list of consumables
        for (Consumable item : fridge) {
            switch (mode) {
                case "Expired" -> {
                    if (todayDate.isAfter(item.getExpiryDate().toLocalDate())) {
                        basket.add(item);
                    }
                }
                case "Not Expired" -> {
                    if (todayDate.isBefore(item.getExpiryDate().toLocalDate()) ||
                            todayDate.isEqual(item.getExpiryDate().toLocalDate())
                    ) {
                        basket.add(item);
                    }
                }
                case "Expiring in 7 Days" -> {
                    int daysToExpiry = (int) DAYS.between(todayDate, item.getExpiryDate());

                    final int NUM_DAYS_IN_WEEK = 7;
                    if (daysToExpiry >= 0 && daysToExpiry <= NUM_DAYS_IN_WEEK) {
                        basket.add(item);
                    }
                }
            }
        }
        return basket;
    }

    /**
     * Getter for the current local system date of type <code>LocalDateTime</code>
     *
     * @return an instance of the local system date of type <code>LocalDateTime</code>
     */
    public static LocalDateTime getCurrentDate() {
        return LocalDateTime.now();
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
                }).setPrettyPrinting().create();
    }

    /**
     * Gets a custom Gson object with <code>TypeAdapters</code> for classes LocalDateTime and
     * <code>RuntimeTypeAdapterFactory</code> for <code>Consumable</code>
     * and its subclasses <code>Food</code> and <code>Drink</code>.
     *
     * @return a <code>Gson</code> object for serializing/deserializing of class <code>LocalDateTime</code>
     * and subclasses <code>Food</code> & <code>Drink</code> objects of <code>Consumable</code>
     */
    public Gson getCustomGsonObj() {
        return customGsonObj;
    }

    /**
     * Reads from a specified json file and initializes <code>List<Consumable> fridge</code>.
     * Otherwise, creates a json file for saving food item objects if the file doesn't exist.
     * <p>
     * Reassigns the object's subclass name back <code>Consumable</code> field <code>type</code> to each object
     * for <code>Gson</code> to serialize after deserializing for the 2nd and subsequent runs of the application.
     */
    public void readInFile() {
        Path filePath = Path.of(FILE_PATH);

        try {
            File fileReaderObj = new File(String.valueOf(filePath));

            // if file already exists, read from json file, else make new file & initialize the consumable item list
            if (!fileReaderObj.createNewFile()) {
                Reader fileReader = Files.newBufferedReader(filePath);

                // convert JSON file to a List<FoodItem>
                // https://attacomsian.com/blog/gson-read-json-file
                // https://stackoverflow.com/questions/18544133/parsing-json-array-into-java-util-list-with-gson
                Type typeConsumable = new TypeToken<List<Consumable>>() {
                }.getType();
                fridge = customGsonObj.fromJson(fileReader, typeConsumable);

                // if file does exist but is empty, re-instantiate list
                if (fridge == null) {
                    fridge = new ArrayList<>();
                }

                // Convert item subtypes from server to client class name structure
                typeServerToClient(true);

                fileReader.close();
            }
        } catch (IOException e) {
            System.out.println("File can't be read!");
        }
    }

    /**
     * Writes to a json file for saving <code>Consumable</code> objects
     * generated during the duration of the program.
     */
    public void writeToFile() {
        Path filePath = Paths.get(FILE_PATH);

        // temporarily Convert item subtypes from client to server class name structure
        typeServerToClient(false);

        // make deep copy of fridge for serializing
        List<Consumable> listToSerialize = newListToSerialize();

        // reconvert fridge subTypes back to client class types
        typeServerToClient(true);

        // https://docs.oracle.com/javase/7/docs/api/java/io/FileWriter.html
        // https://attacomsian.com/blog/gson-write-json-file
        try {
            Writer fileWriterObj = new FileWriter(String.valueOf(filePath));
            customGsonObj.toJson(listToSerialize, fileWriterObj);
            fileWriterObj.close();
        } catch (IOException e) {
            System.out.println("Unable to write to save consumable data to file: " + filePath);
        }
    }

    /**
     * Helper method to create a deep copy of <code>List<Consumable> fridge</code>
     * and reinitializing the field <code>type</code> subtypes of each <code>Consumable</code> object
     * <p>
     * The method to create a deep copy makes use of the fact that serializing and deserializing an
     * object will remove all references of the "copied" item and create a new object with same values,
     * but with different references
     * </p>
     *
     * @return a deep copied <code>List<Consumable></code> of <code>List<Consumable> fridge</code>
     */
    private List<Consumable> newListToSerialize() {
        // https://www.geeksforgeeks.org/how-to-clone-a-list-in-java/
        List<Consumable> listToSerialize = customGsonObj.fromJson(
                customGsonObj.toJson(fridge),
                new TypeToken<List<Consumable>>() {
                }.getType()
        );

        // reinitialize the subtypes
        for (Consumable consumable : listToSerialize) {
            if (consumable instanceof Food) {
                consumable.setType(consumable.getClass().getName());
            } else if (consumable instanceof Drink) {
                consumable.setType(consumable.getClass().getName());
            }
        }
        return listToSerialize;
    }

    /**
     * Converts the passed in <code>List<Consumable></code> parameter to a JSON Array object of type <code>String</code>
     *
     * @param list a <code>List<Consumable></code> to be converted into a JSON Array object
     * @return a <code>String</code> representing the <code>List<Consumable> list</code> as a JSON Array object
     */
    public String toJSONArray(List<Consumable> list) {
        return customGsonObj.toJson(list);
    }

    /**
     * Helper method for converting the object's, of <code>List<Consumable> fridge</code>, type
     * relative to the server's class subtype to the clients class subtype of <code>Food</code> or <code>Drink</code>
     * and vice versa.
     */
    private void typeServerToClient(boolean isForwardConvert) {
        // Convert item subtypes from server to client class name structure
        for (Consumable consumable : fridge) {
            final String CLIENT_FOOD_TYPE_NAME = "ca.cmpt213.a4.client.model.Food";
            final String CLIENT_DRINK_TYPE_NAME = "ca.cmpt213.a4.client.model.Drink";
            if (isForwardConvert) {
                if (consumable instanceof Food) {
                    consumable.setType(CLIENT_FOOD_TYPE_NAME);
                } else if (consumable instanceof Drink) {
                    consumable.setType(CLIENT_DRINK_TYPE_NAME);
                }
            } else {
                if (Objects.equals(consumable.getType(), CLIENT_FOOD_TYPE_NAME)) {
                    consumable.setType(Food.class.getName());
                } else if (Objects.equals(consumable.getType(), CLIENT_DRINK_TYPE_NAME)) {
                    consumable.setType(Drink.class.getName());
                }
            }
        }
    }
}


