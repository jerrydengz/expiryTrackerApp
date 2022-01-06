package expiryTracker.client.model;

import expiryTracker.client.control.ConsumableManager;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * A Subclass <code>Drink</code> inheriting from <code>Consumable</code>
 * <p>
 * Stores a <code>double</code> volume field.
 */
public class Drink extends Consumable {
    private double volume;

    /**
     * Constructs a <code>Drink</code> object with the passed in parameters.
     *
     * @param name       Represents the name of the food item.
     * @param notes      Represents details about the drink item.
     * @param price      Represents the price of the drink item.
     * @param volume     Represents the volume of the drink item
     * @param expiryDate Represents the expiry date of the drink item
     */
    public Drink(String name, String notes, double price, double volume, LocalDateTime expiryDate) {
        this.name = name;
        this.notes = notes;
        this.price = price;
        this.volume = volume;
        this.expiryDate = expiryDate;
    }

    /**
     * A helper method to assist with returning the correct expiry message of the <code>Drink</code> object.
     *
     * @param todayDate    The current local system date
     * @param daysToExpiry The difference between the object's expiry date and the current local system date
     * @return a string containing the correct message corresponding to the object's expiry status
     */
    private String expiryMessage(LocalDate todayDate, int daysToExpiry) {
        if (todayDate.isAfter(expiryDate.toLocalDate())) {
            return "This drink is expired for " + (-1) * daysToExpiry + " day(s).";
        } else if (todayDate.isBefore(expiryDate.toLocalDate())) {
            return "This drink expires in " + daysToExpiry + " days(s).";
        }
        return "This drink expires today.";
    }

    /**
     * Displays this <code>Drink</code> object's name, notes,
     * price, volume, expiry date and days till expiry message.
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

        return "Name: " + name +
                "\nNotes: " + notes +
                "\nPrice: " + doubleFormatting.format(price) +
                "\nVolume: " + doubleFormatting.format(volume) +
                "\nExpiry Date: " + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(expiryDate.toLocalDate())
                + "\n" + expiryMessage(todayDate, daysToExpiry);
    }
}
