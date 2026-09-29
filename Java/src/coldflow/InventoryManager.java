package coldflow;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager implements InventoryOperations {

    private final FEFOHeap inventory;

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
        return inventory.peekNextBatch();
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

    public Batch dispatchNextBatch() {
        return inventory.dispatchNextBatch();
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
    // METHOD OVERLOADING - SEARCH BY BATCH ID
    // =========================================================

    public Batch searchBatch(String batchId) {

        return inventory.findByBatchId(batchId);
    }

    // =========================================================
    // METHOD OVERLOADING - SEARCH BY PRODUCT + ZONE
    // =========================================================

    public List<Batch> searchBatch(
            String productName,
            String zoneName) {

        List<Batch> result = new ArrayList<>();

        for (Batch batch : inventory.getSnapshot()) {

            boolean productMatches =
                    batch.getProductName()
                            .equalsIgnoreCase(productName);

            boolean zoneMatches =
                    batch.getZoneName()
                            .equalsIgnoreCase(zoneName);

            if (productMatches && zoneMatches) {

                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // EXISTING SEARCH BY PRODUCT NAME
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

            if (batch.getZoneName()
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

        List<Batch> batches =
                getBatchesByZone(zoneName);

        System.out.println(
                "\n--- "
                        + zoneName
                        + " Inventory ---"
        );

        if (batches.isEmpty()) {

            System.out.println(
                    "No batches in this zone."
            );

            return;
        }

        for (Batch batch : batches) {

            System.out.println(batch);
        }
    }
}