package expiryTracker.client.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A class modeled after the Static Factory Design pattern
 * for the purpose of creating and instantiating new <code>Food</code> and
 * <code>Drink</code> objects of base class <code>Consumable</code>
 */
public class ConsumableFactory {

    /**
     * Generate a new object of either class type <code>Food</code> or <code>Drink</code>, with values
     * obtained through user input in the <code>Java Swing GUI</code> menu, defined by parameter <code>consumableType</code>.
     *
     * @param consumableType An <code>String</code> representing the objects type
     *                       of either <code>Food</code> or <code>Drink</code>
     * @param name           Represents the name of the item of type defined by parameter <code>consumableType</code>.
     * @param notes          Represents details about the item of type <code>String</code>.
     * @param price          Represents the price of the item of type <code>double</code>
     * @param matter         Represents the matter of the item of type <code>double</code>
     * @param expiryDate     Represents the expiry date of the item of type <code>LocalDateTime</code>>
     * @return A newly created <code>Food</code> or <code>Drink</code> object
     */
    public Consumable getInstance(String consumableType, String name,
                                  String notes, double price, double matter, LocalDateTime expiryDate) {
        // modified from: https://www.tutorialspoint.com/design_pattern/factory_pattern.htm
        if (Objects.equals(consumableType, "")) {
            return null;
        }

        final String DRINK_CONSUMABLE = "Drink";
        final String FOOD_CONSUMABLE = "Food";

        if (Objects.equals(consumableType, FOOD_CONSUMABLE)) {
            return new Food(name, notes, price, matter, expiryDate);
        } else if (Objects.equals(consumableType, DRINK_CONSUMABLE)) {
            return new Drink(name, notes, price, matter, expiryDate);
        }
        return null;
    }

}
