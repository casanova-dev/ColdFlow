package coldflow;

import java.time.LocalDate;

public class Batch implements Comparable<Batch> {

    // =====================================================
    // ENCAPSULATED DATA
    // =====================================================

    private final String batchId;
    private final String productName;
    private int quantity;
    private final LocalDate expiryDate;
    private final String zoneName;

    // =====================================================
    // CONSTRUCTOR OVERLOADING
    // =====================================================

    // Full constructor
    public Batch(
            String batchId,
            String productName,
            int quantity,
            LocalDate expiryDate,
            String zoneName) {

        this.batchId = batchId;
        this.productName = productName;
        this.quantity = Math.max(0, quantity);
        this.expiryDate = expiryDate;
        this.zoneName = zoneName;
    }

    // Constructor without zone
    public Batch(
            String batchId,
            String productName,
            int quantity,
            LocalDate expiryDate) {

        this(
                batchId,
                productName,
                quantity,
                expiryDate,
                "Unassigned"
        );
    }

    // Constructor with default quantity
    public Batch(
            String batchId,
            String productName,
            LocalDate expiryDate) {

        this(
                batchId,
                productName,
                0,
                expiryDate,
                "Unassigned"
        );
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public String getBatchId() {
        return batchId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getZoneName() {
        return zoneName;
    }

    // =====================================================
    // ENCAPSULATED QUANTITY OPERATIONS
    // =====================================================

    public void reduceQuantity(int amount) {

        if (amount <= 0) {
            return;
        }

        if (amount >= quantity) {
            quantity = 0;
        } else {
            quantity -= amount;
        }
    }

    public void increaseQuantity(int amount) {

        if (amount <= 0) {
            return;
        }

        quantity += amount;
    }

    // =====================================================
    // FEFO COMPARISON
    // =====================================================

    @Override
    public int compareTo(Batch other) {

        return this.expiryDate.compareTo(
                other.expiryDate
        );
    }

    // =====================================================
    // DISPLAY
    // =====================================================

    @Override
    public String toString() {

        return String.format(
                "Batch ID: %s | Product: %s | Quantity: %d | Expiry: %s | Zone: %s",
                batchId,
                productName,
                quantity,
                expiryDate,
                zoneName
        );
    }
}