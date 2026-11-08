package coldflow;

import java.time.LocalDate;

public final class FEFOHeapTest {

    private FEFOHeapTest() {
    }

    public static void main(String[] args) {
        FEFOHeap inventory = new FEFOHeap();

        Batch latest = new Batch(
                "A", "Milk", 5, LocalDate.of(2030, 10, 1), "Chiller");
        Batch earliest = new Batch(
                "B", "Yogurt", 8, LocalDate.of(2030, 1, 1), "Chiller");
        Batch middle = new Batch(
                "C", "Cheese", 10, LocalDate.of(2030, 5, 1), "Chiller");

        inventory.addBatch(latest);
        inventory.addBatch(earliest);
        inventory.addBatch(middle);
        inventory.addBatch(new Batch(
                "B", "Duplicate", 1, LocalDate.of(2029, 1, 1), "Chiller"));

        check(inventory.size() == 3, "Duplicate batch IDs must be ignored");
        check(inventory.peekNextBatch() == earliest,
                "Peek must return the earliest-expiring batch");

        check(inventory.removeByBatchId("B") == earliest,
                "Removal by ID must return the requested batch");
        check(inventory.findByBatchId("B") == null,
                "Removed batches must not remain in the index");
        check(inventory.findByBatchId("A") == latest
                        && inventory.findByBatchId("C") == middle,
                "Heap index must remain consistent after removal");

        check(inventory.updateQuantity("C", 12),
                "Existing batch quantity should update");
        check(middle.getQuantity() == 12,
                "Quantity update should be reflected on the batch");
        check(!inventory.updateQuantity("C", -1),
                "Negative quantities should be rejected");

        check(inventory.dispatchNextBatch() == middle,
                "Dispatch must return the next earliest-expiring batch");
        check(inventory.dispatchNextBatch() == latest,
                "Remaining batches must dispatch in expiry order");
        check(inventory.dispatchNextBatch() == null && inventory.isEmpty(),
                "Dispatching an empty heap should return null");

        System.out.println("FEFOHeap smoke test passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
