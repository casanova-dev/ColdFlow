package coldflow;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private final FEFOHeap inventory;

    public InventoryManager() {

        inventory = new FEFOHeap();
    }

    // =========================================================
    // ADD A BATCH
    // =========================================================

    public void addBatch(Batch batch) {

        inventory.addBatch(batch);
    }

    // =========================================================
    // GET EARLIEST-EXPIRING BATCH
    // =========================================================

    public Batch getNextBatch() {

        return inventory.peekNextBatch();
    }

    // =========================================================
    // DISPATCH EARLIEST-EXPIRING BATCH - FEFO
    // =========================================================

    public Batch dispatchNextBatch() {

        return inventory.dispatchNextBatch();
    }

    // =========================================================
    // NUMBER OF BATCHES
    // =========================================================

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
    // SEARCH BATCH BY BATCH ID
    // =========================================================

    public Batch searchBatch(String batchId) {

        return inventory.findByBatchId(batchId);
    }

    // =========================================================
    // SEARCH BATCHES BY PRODUCT NAME
    // =========================================================

    public List<Batch> searchProduct(
            String productName) {

        return inventory.findByProductName(
                productName
        );
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

    public Batch deleteBatch(
            String batchId) {

        return inventory.removeByBatchId(
                batchId
        );
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

    public List<Batch> getBatchesByZone(
            String zoneName) {

        List<Batch> result =
                new ArrayList<>();

        for (Batch batch :
                inventory.getSnapshot()) {

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

    public void displayZoneInventory(
            String zoneName) {

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

    // =========================================================
    // DISPLAY COMPLETE INVENTORY
    // =========================================================

    public void displayInventory() {

        inventory.displayInventory();
    }
}