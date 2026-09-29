package coldflow;

public interface InventoryOperations {

    void addBatch(Batch batch);

    void removeBatch(String batchId);

    Batch getNextDispatchBatch();

    void displayInventory();

    int getInventoryCount();
}