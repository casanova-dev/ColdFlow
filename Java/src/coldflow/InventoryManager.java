package coldflow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryManager implements InventoryOperations {

    private final FEFOHeap inventory;

    // Configurable thresholds
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int EXPIRY_WARNING_DAYS = 7;

    public InventoryManager() {
        inventory = new FEFOHeap();
    }

    // =========================================================
    // CO2 - INVENTORY OPERATIONS INTERFACE IMPLEMENTATION
    // =========================================================

    @Override
    public void addBatch(Batch batch) {
        inventory.addBatch(batch);
    }

    @Override
    public void removeBatch(String batchId) {
        inventory.removeByBatchId(batchId);
    }

    @Override
    public Batch getNextDispatchBatch() {
        return getNextBatch();
    }

    @Override
    public void displayInventory() {
        inventory.displayInventory();
    }

    @Override
    public int getInventoryCount() {
        return inventory.size();
    }

    // =========================================================
    // EXISTING METHODS
    // =========================================================

    public Batch getNextBatch() {
        return inventory.peekNextBatch();
    }

    // Dispatch the earliest-expiring batch that is not expired.
    public Batch dispatchNextBatch() {

        LocalDate today = LocalDate.now();

        List<Batch> batches = inventory.getSnapshot();

        Batch nextEligibleBatch = null;

        for (Batch batch : batches) {

            if (batch.getQuantity() <= 0) {
                continue;
            }

            if (batch.getExpiryDate() == null) {
                continue;
            }

            if (batch.getExpiryDate().isBefore(today)) {
                continue;
            }

            if (nextEligibleBatch == null
                    || batch.compareTo(nextEligibleBatch) < 0) {

                nextEligibleBatch = batch;
            }
        }

        if (nextEligibleBatch == null) {
            System.out.println(
                    "No valid, unexpired batch is available for dispatch."
            );
            return null;
        }

        // Remove the selected batch from the heap.
        return inventory.removeByBatchId(
                nextEligibleBatch.getBatchId()
        );
    }

    public int getBatchCount() {
        return inventory.size();
    }

    // =========================================================
    // CHECK WHETHER INVENTORY IS EMPTY
    // =========================================================

    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    // =========================================================
    // SEARCH BY BATCH ID
    // =========================================================

    public Batch searchBatch(String batchId) {
        return inventory.findByBatchId(batchId);
    }

    // =========================================================
    // SEARCH BY PRODUCT + ZONE
    // =========================================================

    public List<Batch> searchBatch(
            String productName,
            String zoneName) {

        List<Batch> result = new ArrayList<>();

        for (Batch batch : inventory.getSnapshot()) {

            boolean productMatches =
                    batch.getProductName() != null
                    && batch.getProductName()
                            .equalsIgnoreCase(productName);

            boolean zoneMatches =
                    batch.getZoneName() != null
                    && batch.getZoneName()
                            .equalsIgnoreCase(zoneName);

            if (productMatches && zoneMatches) {
                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // SEARCH BY PRODUCT NAME
    // =========================================================

    public List<Batch> searchProduct(String productName) {
        return inventory.findByProductName(productName);
    }

    // =========================================================
    // UPDATE BATCH QUANTITY
    // =========================================================

    public boolean updateBatchQuantity(
            String batchId,
            int newQuantity) {

        return inventory.updateQuantity(
                batchId,
                newQuantity
        );
    }

    // =========================================================
    // DELETE BATCH BY ID
    // =========================================================

    public Batch deleteBatch(String batchId) {
        return inventory.removeByBatchId(batchId);
    }

    // =========================================================
    // GET ALL BATCHES
    // =========================================================

    public List<Batch> getAllBatches() {
        return inventory.getSnapshot();
    }

    // =========================================================
    // GET BATCHES STORED IN A PARTICULAR ZONE
    // =========================================================

    public List<Batch> getBatchesByZone(String zoneName) {

        List<Batch> result = new ArrayList<>();

        for (Batch batch : inventory.getSnapshot()) {

            if (batch.getZoneName() != null
                    && batch.getZoneName()
                            .equalsIgnoreCase(zoneName)) {

                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // DISPLAY BATCHES BELONGING TO A PARTICULAR ZONE
    // =========================================================

    public void displayZoneInventory(String zoneName) {

        List<Batch> batches = getBatchesByZone(zoneName);

        System.out.println("\n--- " + zoneName + " Inventory ---");

        if (batches.isEmpty()) {
            System.out.println("No batches in this zone.");
            return;
        }

        for (Batch batch : batches) {
            System.out.println(batch);
        }
    }

    // =========================================================
    // FEATURE 1: GET EXPIRED BATCHES
    // =========================================================

    public List<Batch> getExpiredBatches() {

        List<Batch> result = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (Batch batch : inventory.getSnapshot()) {

            if (batch.getExpiryDate() != null
                    && batch.getExpiryDate().isBefore(today)) {

                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // FEATURE 2: GET BATCHES EXPIRING SOON
    // =========================================================

    public List<Batch> getBatchesExpiringSoon() {

        List<Batch> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(EXPIRY_WARNING_DAYS);

        for (Batch batch : inventory.getSnapshot()) {

            LocalDate expiryDate = batch.getExpiryDate();

            if (expiryDate != null
                    && !expiryDate.isBefore(today)
                    && !expiryDate.isAfter(warningDate)) {

                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // FEATURE 3: GET LOW-STOCK BATCHES
    // =========================================================

    public List<Batch> getLowStockBatches() {

        List<Batch> result = new ArrayList<>();

        for (Batch batch : inventory.getSnapshot()) {

            if (batch.getQuantity() > 0
                    && batch.getQuantity() <= LOW_STOCK_THRESHOLD) {

                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // FEATURE 4: GET OUT-OF-STOCK BATCHES
    // =========================================================

    public List<Batch> getOutOfStockBatches() {

        List<Batch> result = new ArrayList<>();

        for (Batch batch : inventory.getSnapshot()) {

            if (batch.getQuantity() <= 0) {
                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // DISPLAY EXPIRY ALERTS
    // =========================================================

    public void displayExpiryAlerts() {

        List<Batch> expired = getExpiredBatches();
        List<Batch> expiringSoon = getBatchesExpiringSoon();

        System.out.println("\n========== EXPIRY ALERTS ==========");

        System.out.println("\nExpired batches: " + expired.size());

        for (Batch batch : expired) {
            System.out.println("[EXPIRED] " + batch);
        }

        System.out.println(
                "\nBatches expiring within "
                + EXPIRY_WARNING_DAYS
                + " days: "
                + expiringSoon.size()
        );

        for (Batch batch : expiringSoon) {
            System.out.println("[EXPIRING SOON] " + batch);
        }

        System.out.println("===================================");
    }

    // =========================================================
    // DISPLAY STOCK ALERTS
    // =========================================================

    public void displayStockAlerts() {

        List<Batch> lowStock = getLowStockBatches();
        List<Batch> outOfStock = getOutOfStockBatches();

        System.out.println("\n========== STOCK ALERTS ==========");

        System.out.println("\nLow-stock batches: " + lowStock.size());

        for (Batch batch : lowStock) {
            System.out.println("[LOW STOCK] " + batch);
        }

        System.out.println("\nOut-of-stock batches: " + outOfStock.size());

        for (Batch batch : outOfStock) {
            System.out.println("[OUT OF STOCK] " + batch);
        }

        System.out.println("==================================");
    }
}

