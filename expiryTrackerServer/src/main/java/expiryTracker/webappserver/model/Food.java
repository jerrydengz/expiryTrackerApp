package expiryTracker.webappserver.model;

import expiryTracker.webappserver.control.ConsumableManager;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * A Subclass <code>Food</code> inheriting from <code>Consumable</code>
 * <p>
 * Stores a <code>double</code> weight field.
 */
public class Food extends Consumable {
    private double weight;

    /**
     * Constructs a <code>Food</code> object with the passed in parameters.
     *
     * @param name       Represents the name of the food item.
     * @param notes      Represents details about the food item.
     * @param price      Represents the price of the food item.
     * @param weight     Represents the weight of the food item
     * @param expiryDate Represents the expiry date of the food item
     */
    public Food(String name, String notes, double price, double weight, LocalDateTime expiryDate) {
        this.name = name;
        this.notes = notes;
        this.price = price;
        this.weight = weight;
        this.expiryDate = expiryDate;
    }

    /**
     * A helper method to assist with returning the correct expiry message of the <code>Food</code> object.
     *
     * @param todayDate   The current local system date
     * @param daysToExpiry The difference between the object's expiry date and the current local system date
     * @return a string containing the correct message corresponding to the object's expiry status
     */
    private String expiryMessage(LocalDate todayDate, int daysToExpiry) {
        if (todayDate.isAfter(expiryDate.toLocalDate())) {
            return "This food is expired for " + (-1) * daysToExpiry + " day(s).";
        } else if (todayDate.isBefore(expiryDate.toLocalDate())) {
            return "This food expires in " + daysToExpiry + " days(s).";
        }
        return "This food expires today.";
    }

    /**
     * Displays this <code>Food</code> object's name, notes,
     * price, weight, expiry date and days till expiry message.
     *
     * @return Returns a formatted string consisting of this class's private fields.
     */
    @Override
    public String toString() {
        LocalDate todayDate = ConsumableManager.getCurrentDate().toLocalDate();

        // https://docs.oracle.com/javase/8/docs/api/java/time/temporal/ChronoUnit.html
        int daysToExpiry = (int) DAYS.between(todayDate, expiryDate.toLocalDate());

        // https://docs.oracle.com/javase/8/docs/api/java/text/DecimalFormat.html#applyPattern-java.lang.String-
        DecimalFormat doubleFormatting = new DecimalFormat("#,###,##0.00");

        return          "Name: " + name +
                "\nNotes: " + notes +
                "\nPrice: " + doubleFormatting.format(price) +
                "\nWeight: " + doubleFormatting.format(weight) +
                "\nExpiry Date: " + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(expiryDate.toLocalDate())
                + "\n" + expiryMessage(todayDate, daysToExpiry);
    }
}
