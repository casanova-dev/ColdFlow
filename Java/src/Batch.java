package coldflow;

import java.time.LocalDate;

public class Batch implements Comparable<Batch> {

    private final String batchId;
    private final String productName;
    private int quantity;
    private final LocalDate expiryDate;

    // Storage location
    private final String zoneName;

    public Batch(
            String batchId,
            String productName,
            int quantity,
            LocalDate expiryDate,
            String zoneName) {

        this.batchId = batchId;
        this.productName = productName;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.zoneName = zoneName;
    }

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

    @Override
    public int compareTo(Batch other) {

        // FEFO:
        // Earlier expiry date = higher priority
        return this.expiryDate.compareTo(other.expiryDate);
    }

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