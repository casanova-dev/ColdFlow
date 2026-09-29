package coldflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FEFOHeap {

    /*
     * =========================================================
     * CO2 OPTIMIZED DATA STRUCTURE
     * =========================================================
     *
     * ArrayList:
     * Stores Batch objects in Min-Heap form.
     *
     * HashMap:
     * Maps Batch ID -> Heap Index.
     *
     * This avoids repeatedly performing linear searches
     * through the entire heap.
     *
     * Batch ID search:
     * Old implementation  : O(n)
     * New implementation  : O(1) average
     *
     * Remove by Batch ID:
     * Old implementation  : O(n)
     * New implementation  : O(log n)
     *
     * =========================================================
     */

    private final List<Batch> heap;

    private final Map<String, Integer> indexMap;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public FEFOHeap() {

        heap = new ArrayList<>();

        indexMap = new HashMap<>();
    }

    // =========================================================
    // ADD BATCH
    // Time Complexity: O(log n)
    // Space Complexity: O(n)
    // =========================================================

    public void addBatch(Batch batch) {

        if (batch == null ||
                batch.getBatchId() == null) {

            return;
        }

        String batchId =
                batch.getBatchId();

        // Prevent duplicate Batch IDs
        if (indexMap.containsKey(batchId)) {

            return;
        }

        heap.add(batch);

        int index =
                heap.size() - 1;

        indexMap.put(
                batchId,
                index
        );

        siftUp(index);
    }

    // =========================================================
    // PEEK - EARLIEST EXPIRING BATCH
    // Time Complexity: O(1)
    // =========================================================

    public Batch peekNextBatch() {

        if (heap.isEmpty()) {

            return null;
        }

        return heap.get(0);
    }

    // =========================================================
    // DISPATCH EARLIEST EXPIRING BATCH
    // Time Complexity: O(log n)
    // =========================================================

    public Batch dispatchNextBatch() {

        if (heap.isEmpty()) {

            return null;
        }

        return removeAt(0);
    }

    // =========================================================
    // SIZE
    // Time Complexity: O(1)
    // =========================================================

    public int size() {

        return heap.size();
    }

    // =========================================================
    // EMPTY CHECK
    // Time Complexity: O(1)
    // =========================================================

    public boolean isEmpty() {

        return heap.isEmpty();
    }

    // =========================================================
    // SNAPSHOT
    // Time Complexity: O(n)
    // Space Complexity: O(n)
    // =========================================================

    public List<Batch> getSnapshot() {

        return new ArrayList<>(heap);
    }

    // =========================================================
    // SEARCH BY BATCH ID
    // Time Complexity: O(1) AVERAGE
    // =========================================================

    public Batch findByBatchId(
            String batchId) {

        if (batchId == null) {

            return null;
        }

        Integer index =
                indexMap.get(batchId);

        if (index == null) {

            return null;
        }

        return heap.get(index);
    }

    // =========================================================
    // SEARCH BY PRODUCT NAME
    // Time Complexity: O(n)
    //
    // Product search uses partial matching, therefore
    // every batch may need to be checked.
    // =========================================================

    public List<Batch> findByProductName(
            String productName) {

        List<Batch> result =
                new ArrayList<>();

        if (productName == null) {

            return result;
        }

        String search =
                productName.toLowerCase();

        for (Batch batch : heap) {

            if (batch.getProductName()
                    .toLowerCase()
                    .contains(search)) {

                result.add(batch);
            }
        }

        return result;
    }

    // =========================================================
    // UPDATE QUANTITY
    //
    // Batch expiry does not change when quantity changes.
    // Therefore heap order does not need to be rebuilt.
    //
    // Time Complexity: O(1) average
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
    //
    // Time Complexity: O(log n)
    //
    // HashMap finds the position in O(1) average,
    // then the heap is repaired in O(log n).
    // =========================================================

    public Batch removeByBatchId(
            String batchId) {

        if (batchId == null) {

            return null;
        }

        Integer index =
                indexMap.get(batchId);

        if (index == null) {

            return null;
        }

        return removeAt(index);
    }

    // =========================================================
    // REMOVE AT HEAP INDEX
    // Time Complexity: O(log n)
    // =========================================================

    private Batch removeAt(int index) {

        int lastIndex =
                heap.size() - 1;

        Batch removed =
                heap.get(index);

        indexMap.remove(
                removed.getBatchId()
        );

        // Removing the last element
        if (index == lastIndex) {

            heap.remove(lastIndex);

            return removed;
        }

        Batch lastBatch =
                heap.remove(lastIndex);

        heap.set(
                index,
                lastBatch
        );

        indexMap.put(
                lastBatch.getBatchId(),
                index
        );

        repairHeap(index);

        return removed;
    }

    // =========================================================
    // REPAIR HEAP
    // =========================================================

    private void repairHeap(int index) {

        if (index > 0) {

            int parent =
                    (index - 1) / 2;

            if (heap.get(index)
                    .compareTo(heap.get(parent)) < 0) {

                siftUp(index);

                return;
            }
        }

        siftDown(index);
    }

    // =========================================================
    // SIFT UP
    // Time Complexity: O(log n)
    // =========================================================

    private void siftUp(int index) {

        while (index > 0) {

            int parent =
                    (index - 1) / 2;

            if (heap.get(index)
                    .compareTo(heap.get(parent)) >= 0) {

                break;
            }

            swap(
                    index,
                    parent
            );

            index = parent;
        }
    }

    // =========================================================
    // SIFT DOWN
    // Time Complexity: O(log n)
    // =========================================================

    private void siftDown(int index) {

        int size =
                heap.size();

        while (true) {

            int left =
                    2 * index + 1;

            int right =
                    2 * index + 2;

            int smallest =
                    index;

            if (left < size &&
                    heap.get(left)
                            .compareTo(
                                    heap.get(smallest)
                            ) < 0) {

                smallest = left;
            }

            if (right < size &&
                    heap.get(right)
                            .compareTo(
                                    heap.get(smallest)
                            ) < 0) {

                smallest = right;
            }

            if (smallest == index) {

                break;
            }

            swap(
                    index,
                    smallest
            );

            index = smallest;
        }
    }

    // =========================================================
    // SWAP TWO HEAP ELEMENTS
    // =========================================================

    private void swap(
            int first,
            int second) {

        Batch firstBatch =
                heap.get(first);

        Batch secondBatch =
                heap.get(second);

        heap.set(
                first,
                secondBatch
        );

        heap.set(
                second,
                firstBatch
        );

        indexMap.put(
                secondBatch.getBatchId(),
                first
        );

        indexMap.put(
                firstBatch.getBatchId(),
                second
        );
    }

    // =========================================================
    // DISPLAY INVENTORY IN FEFO ORDER
    //
    // Does not create another PriorityQueue.
    //
    // We use a copy of the references and sort it for display.
    // The original heap remains unchanged.
    //
    // Time Complexity: O(n log n)
    // Extra Space: O(n)
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

        List<Batch> displayList =
                new ArrayList<>(heap);

        displayList.sort(
                Batch::compareTo
        );

        int position = 1;

        for (Batch batch :
                displayList) {

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
