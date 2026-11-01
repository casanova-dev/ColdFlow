package coldflow.database;

import coldflow.Batch;

import java.time.LocalDate;

public class BatchRepositoryTest {

    public static void main(String[] args) {

        BatchRepository repository = new BatchRepository();

        // Add a sample batch
        Batch batch = new Batch(
            "CF001",
            "Milk",
            100,
            LocalDate.of(2026, 12, 15),
            "Chiller"
        );

        System.out.println("\nAdding batch...");

        boolean added = repository.addBatch(batch);

        System.out.println("Batch added: " + added);

        // Retrieve inventory
        System.out.println("\nRetrieving inventory...");

        for (Batch b : repository.getAllBatches()) {
            System.out.println(b);
        }

        // Retrieve FEFO batch
        System.out.println("\nNext FEFO batch:");

        Batch next = repository.getNextFEFOBatch();

        if (next != null) {
            System.out.println(next);
        }
    }
}