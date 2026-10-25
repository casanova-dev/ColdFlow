package coldflow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {

    // CSV expiry date format: dd-MM-yyyy
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {

        // =========================================
        // COLDFlow - Initialize Warehouse
        // =========================================

        System.out.println("==========================================");
        System.out.println("        COLDFlow Warehouse System");
        System.out.println("==========================================");

        // =========================================
        // CREATE WAREHOUSE ZONES
        // =========================================

        FreezerZone freezer = new FreezerZone();
        ChillerZone chiller = new ChillerZone();
        AmbientZone ambient = new AmbientZone();

        // =========================================
        // RUNTIME POLYMORPHISM
        // =========================================

        StorageZone zone;

        zone = freezer;
        System.out.println("\nRuntime Polymorphism: " + zone.getZoneName());

        zone = chiller;
        System.out.println("Runtime Polymorphism: " + zone.getZoneName());

        zone = ambient;
        System.out.println("Runtime Polymorphism: " + zone.getZoneName());

        // =========================================
        // HYBRID INHERITANCE DEMONSTRATION
        // =========================================

        TemperatureControl temperatureControl = freezer;
        InventoryAware inventoryAware = freezer;

        System.out.println("\n===== HYBRID INHERITANCE DEMO =====");

        System.out.println(
                "Temperature Control Interface: "
                        + temperatureControl.isTemperatureSafe()
        );

        inventoryAware.displayInventoryStatus();

        // =========================================
        // CREATE INVENTORY
        // =========================================

        InventoryManager inventory = new InventoryManager();

        // =========================================
        // LOAD INVENTORY FROM CSV
        // =========================================

        String csvPath =
                "Java/data/inventory/COLDFlow_Inventory_Processed.csv";

        System.out.println("\n===== LOADING INVENTORY FROM CSV =====");

        loadInventoryFromCSV(inventory, csvPath);

        // =========================================
        // DISPLAY INITIAL INVENTORY
        // =========================================

        System.out.println("\n===== INITIAL INVENTORY =====");

        inventory.displayInventory();

        System.out.println(
                "\nTotal batches loaded: " + inventory.getBatchCount()
        );

        // =========================================
        // START DASHBOARD
        // =========================================

        javax.swing.SwingUtilities.invokeLater(() -> {

            Dashboard dashboard = new Dashboard(
                    freezer,
                    chiller,
                    ambient,
                    inventory
            );

            dashboard.setVisible(true);
        });

        System.out.println("\nCOLDFlow dashboard started.");
    }

    // =========================================
    // LOAD INVENTORY FROM CSV FILE
    // =========================================

    private static void loadInventoryFromCSV(
            InventoryManager inventory,
            String filePath
    ) {

        int loaded = 0;
        int skipped = 0;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(filePath))) {

            // Read and skip the CSV header
            String line = reader.readLine();

            if (line == null) {
                System.out.println("The CSV file is empty.");
                return;
            }

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] fields = line.split(",", -1);

                    // Expected CSV columns:
                    // 0 Product_ID
                    // 1 Product_Name
                    // 2 Category
                    // 3 Batch_ID
                    // 4 Stock_Quantity
                    // 5 Expiration_Date
                    // 6 Storage_Zone
                    // 7 Reorder_Level

                    // The current CSV contains 8 columns.
                    if (fields.length < 8) {
                        skipped++;

                        System.out.println(
                                "Skipped invalid CSV row: " + line
                        );

                        continue;
                    }

                    String productName = fields[1].trim();
                    String batchId = fields[3].trim();

                    int quantity =
                            Integer.parseInt(fields[4].trim());

                    // Parse expiry dates in dd-MM-yyyy format.
                    LocalDate expiryDate = LocalDate.parse(
                            fields[5].trim(),
                            DATE_FORMATTER
                    );

                    String storageZone = fields[6].trim();

                    // Create a batch from the CSV record.
                    Batch batch = new Batch(
                            batchId,
                            productName,
                            quantity,
                            expiryDate,
                            storageZone
                    );

                    // Add the batch to the inventory.
                    inventory.addBatch(batch);

                    loaded++;

                } catch (Exception e) {

                    skipped++;

                    System.out.println(
                            "Could not load CSV row: " + line
                    );

                    System.out.println(
                            "Reason: " + e.getMessage()
                    );
                }
            }

            // =========================================
            // CSV LOADING SUMMARY
            // =========================================

            System.out.println("\nCSV loading completed.");
            System.out.println("Successfully loaded: " + loaded);
            System.out.println("Skipped rows: " + skipped);

        } catch (IOException e) {

            System.out.println(
                    "\nERROR: Could not read the inventory CSV."
            );

            System.out.println("Expected file: " + filePath);
            System.out.println("Reason: " + e.getMessage());
        }
    }
}

