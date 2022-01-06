package expiryTracker.webappserver.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Represents the base class/ parent class
 * of subclasses <code>Food</code> and <code>Drink</code>.
 * <p>
 * Contains information about the <code>Consumable</code> item and implements
 * the <code>Comparable</code> interface for the purpose of enabling comparisons of
 * itself and other objects of type <code>Consumable</code>
 */
public class Consumable implements Comparable<Consumable> {
    protected UUID itemId;
    protected String name;
    protected String notes;
    protected double price;
    protected LocalDateTime expiryDate;

    // for Gson to determine the subclass type when serializing/deserializing
    protected String type = getClass().getName();

    /**
     * Sets the subclass type for <code>Gson</code> to determine an object of subclass type <code>Food</code>
     * or <code>Drink</code> during serialization and deserialization
     *
     * @param type a <code>String</code> defined by <code>classObject.getClass().getName()</code>
     */
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Gets the expiry date of the Consumable item.
     *
     * @return Returns a <code>LocalDateTime</code> field
     * representing an instantiation of this class's expiry date
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Gets the itemId of the Consumable item.
     * @return a <code>UUID</code> representing the object's <code>itemId</code>
     */
    public UUID getItemId() {
        return itemId;
    }

    /**
     * Sets the itemId of the Consumable item.
     * @param itemId a <code>UUID</code> to represent the object's <code>itemId</code>
     */
    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    /**
     * Compare this object to other objects of type <code>Consumable</code> for the purpose
     * of <code>Collections.sort()</code> to determine a natural ordering between custom object fields
     * <p>
     * Returns an <code>int</code> of -1, 0, or 1:
     * -1: the object to be compared is greater than this object
     * 0: the object to be compared is equal to this object
     * 1: the object to be compared is less than this object
     *
     * @param object an object of type <code>Consumable</code> being compared to
     * @return an <code>int</code> of either -1, 0, or 1 determine the natural ordering
     * between this object and the object being compared
     */
    @Override
    public int compareTo(Consumable object) {
        return (int) DAYS.between(object.expiryDate.toLocalDate(), this.expiryDate.toLocalDate());
    }
}

