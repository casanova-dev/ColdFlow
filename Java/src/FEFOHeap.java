package coldflow;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class FEFOHeap {

    // Java PriorityQueue works as a Min-Heap
    private final PriorityQueue<Batch> heap;

    public FEFOHeap() {
        heap = new PriorityQueue<>();
    }

    // =========================================================
    // ADD
    // =========================================================

    public void addBatch(Batch batch) {

        if (batch == null) {
            return;
        }

        heap.add(batch);
    }

    // =========================================================
    // PEEK - SEE EARLIEST EXPIRY
    // =========================================================

    public Batch peekNextBatch() {

        return heap.peek();
    }

    // =========================================================
    // DISPATCH - REMOVE EARLIEST EXPIRY
    // =========================================================

    public Batch dispatchNextBatch() {

        return heap.poll();
    }

    // =========================================================
    // SIZE
    // =========================================================

    public int size() {

        return heap.size();
    }

    // =========================================================
    // EMPTY CHECK
    // =========================================================

    public boolean isEmpty() {

        return heap.isEmpty();
    }

    // =========================================================
    // SNAPSHOT
    // =========================================================

    public List<Batch> getSnapshot() {

        return new ArrayList<>(heap);
    }

    // =========================================================
    // SEARCH BY BATCH ID
    // =========================================================

    public Batch findByBatchId(String batchId) {

        if (batchId == null) {
            return null;
        }

        for (Batch batch : heap) {

            if (batch.getBatchId()
                    .equalsIgnoreCase(batchId)) {

                return batch;
            }
        }

        return null;
    }

    // =========================================================
    // SEARCH BY PRODUCT NAME
    // =========================================================

    public List<Batch> findByProductName(
            String productName) {

        List<Batch> result =
                new ArrayList<>();

        if (productName == null) {
            return result;
        }

        for (Batch batch : heap) {

            if (batch.getProductName()
                    .toLowerCase()
                    .contains(
                            productName.toLowerCase()
                    )) {

                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // UPDATE QUANTITY
    // =========================================================

    public boolean updateQuantity(
        String batchId,
        int newQuantity) {

    if (batchId == null ||
            newQuantity < 0) {

        return false;
    }

    Batch batch =
            findByBatchId(batchId);

    if (batch == null) {
        return false;
    }

    int currentQuantity =
            batch.getQuantity();

    if (newQuantity < currentQuantity) {

        batch.reduceQuantity(
                currentQuantity - newQuantity
        );

    } else if (newQuantity > currentQuantity) {

        batch.increaseQuantity(
                newQuantity - currentQuantity
        );
    }

    return true;
}

    // =========================================================
    // REMOVE BY BATCH ID
    // =========================================================

    public Batch removeByBatchId(
            String batchId) {

        if (batchId == null) {
            return null;
        }

        Batch found =
                findByBatchId(batchId);

        if (found == null) {
            return null;
        }

        heap.remove(found);

        return found;
    }

    // =========================================================
    // DISPLAY INVENTORY IN FEFO ORDER
    // =========================================================

    public void displayInventory() {

        System.out.println(
                "\n========== FEFO INVENTORY =========="
        );

        if (heap.isEmpty()) {

            System.out.println(
                    "Inventory is empty."
            );

            System.out.println(
                    "===================================="
            );

            return;
        }

        // Temporary heap so actual inventory is not changed
        PriorityQueue<Batch> temporaryHeap =
                new PriorityQueue<>(heap);

        int position = 1;

        while (!temporaryHeap.isEmpty()) {

            Batch batch =
                    temporaryHeap.poll();

            System.out.println(
                    position + ". " + batch
            );

            position++;
        }

        System.out.println(
                "===================================="
        );
    }
}