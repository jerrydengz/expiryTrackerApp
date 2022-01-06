package expiryTracker.webappserver.controllers;

import expiryTracker.webappserver.control.ConsumableManager;
import expiryTracker.webappserver.model.Consumable;
import expiryTracker.webappserver.model.Drink;
import expiryTracker.webappserver.model.Food;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;


// https://www.youtube.com/watch?v=rXBsnNCH59o&ab_channel=BrianFraser

/**
 * A RESTful API Controller for the corresponding client program to access using the provided endpoints
 */
@RestController
public class ConsumableController {
    private final ConsumableManager consumableManager = ConsumableManager.getInstance();

    /**
     * A GET request endpoint to determine if the server is online.
     * <p>
     * Loads the information from the file into the <code>ConsumableManager</code> <code>List <Consumable> fridge</code>
     * </p>
     *
     * @return a <code>String</code> indicating that the server is online
     */
    @GetMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    public String pingServer() {
//        consumableManager.readInFile();
        return "System is up!";
    }

    /**
     * A GET request endpoint to signal the server to save the list of items stored in the control to a file.
     */
    @GetMapping("/exit")
    @ResponseStatus(HttpStatus.OK)
    public void exitRequest() {
        consumableManager.writeToFile();
    }

    /**
     * A GET request endpoint for the program's list of <code>Consumable</code> items
     *
     * @return a <code>String</code> representing the program's list of <code>Consumable</code> items
     * as a JSON Array object.
     */
    @GetMapping("/listAll")
    @ResponseStatus(HttpStatus.OK)
    public String getFridge() {
        return consumableManager.toJSONArray(consumableManager.getFridge());
    }

    /**
     * A GET request endpoint for a filtered list of Expired <code>Consumable</code> items
     *
     * @return a <code>String</code> representing a filtered list of Expired <code>Consumable</code> items
     * as a JSON Array object.
     */
    @GetMapping("/listExpired")
    @ResponseStatus(HttpStatus.OK)
    public String getExpired() {
        List<Consumable> filteredList = consumableManager.filterList("Expired");
        return consumableManager.toJSONArray(filteredList);
    }

    /**
     * A GET request endpoint for a filtered list of Non-Expired <code>Consumable</code> items
     *
     * @return a <code>String</code> representing a filtered list of Non-Expired <code>Consumable</code> items
     * as a JSON Array object.
     */
    @GetMapping("/listNonExpired")
    @ResponseStatus(HttpStatus.OK)
    public String getNonExpired() {
        List<Consumable> filteredList = consumableManager.filterList("Not Expired");
        return consumableManager.toJSONArray(filteredList);
    }

    /**
     * A GET request endpoint for a filtered list of <code>Consumable</code> items expiring in 7 days
     *
     * @return a <code>String</code> representing a filtered list of <code>Consumable</code> items
     * expiring in 7 days as a JSON Array object.
     */
    @GetMapping("/listExpiringIn7Days")
    @ResponseStatus(HttpStatus.OK)
    public String getExpiring7Days() {
        List<Consumable> filteredList = consumableManager.filterList("Expiring in 7 Days");
        return consumableManager.toJSONArray(filteredList);
    }

    /**
     * A POST request endpoint for creating a new <code>Food</code> item
     *
     * @param item a <code>String</code> JSON object representing the passed in data from the POST request
     * @return a <code>String</code> representing the program's updated list of <code>Consumable</code> items
     * as a JSON Array object
     */
    @PostMapping("/addItem/Food")
    @ResponseStatus(HttpStatus.CREATED)
    public String addFoodItem(@RequestBody String item) {
        // deserialize the json object
        Type typeConsumable = new TypeToken<Food>() {
        }.getType();
        Food foodItem = consumableManager.getCustomGsonObj().fromJson(item, typeConsumable);
        foodItem.setItemId(UUID.randomUUID());

        consumableManager.addConsumableItem(foodItem);
        consumableManager.sortFridge();

        // return updated list as serialized json array string
        return consumableManager.toJSONArray(consumableManager.getFridge());
    }

    /**
     * A POST request endpoint for creating a new <code>Drink/code> item
     *
     * @param item a <code>String</code> JSON object representing the passed in data from the POST request
     * @return a <code>String</code> representing the program's updated list of <code>Consumable</code> items
     * as a JSON Array object
     */
    @PostMapping("/addItem/Drink")
    @ResponseStatus(HttpStatus.CREATED)
    public String addDrinkItem(@RequestBody String item) {
        // deserialize the json object
        Type typeConsumable = new TypeToken<Drink>() {
        }.getType();
        Drink drinkItem = consumableManager.getCustomGsonObj().fromJson(item, typeConsumable);
        drinkItem.setItemId(UUID.randomUUID());

        consumableManager.addConsumableItem(drinkItem);
        consumableManager.sortFridge();

        // return updated list as serialized json array string
        return consumableManager.toJSONArray(consumableManager.getFridge());
    }

    /**
     * A POST request endpoint to remove an item in the program's list of <code>Consumable</code> items
     *
     * @param itemId a <code>String</code> id in the format of a <code>UUID</code> representing
     *               the id of the object to be removed
     * @return a <code>String</code> representing the program's updated list of <code>Consumable</code> items
     * as a JSON Array object
     */
    @PostMapping("/removeItem/{uuid}")
    @ResponseStatus(HttpStatus.CREATED)
    public String deleteItem(@PathVariable("uuid") String itemId) {
        Consumable itemToRemove = null;
        for (Consumable item : consumableManager.getFridge()) {
            if (item.getItemId().equals(UUID.fromString(itemId))) {
                itemToRemove = item;
            }
        }

        if (itemToRemove != null) {
            consumableManager.removeConsumableItem(itemToRemove);
        } else {
            throw new IllegalArgumentException();
        }

        return consumableManager.toJSONArray(consumableManager.getFridge());
    }

    /**
     * Response Status Handler for all IllegalArgumentExceptions thrown by
     * the POST request localhost:8080/removeItem/{uuid}
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Requested Item ID Not Found")
    @ExceptionHandler(IllegalArgumentException.class)
    public void invalidIdExceptionHandler() {
    }
}
