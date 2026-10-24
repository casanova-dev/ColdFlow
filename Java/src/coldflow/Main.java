package coldflow;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        // =========================================
        // COLDFlow - Initialize Warehouse
        // =========================================

        System.out.println("==========================================");
        System.out.println("        COLDFlow Warehouse System");
        System.out.println("==========================================");

        // =========================================
        // Create warehouse zones
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

        LocalDate today = LocalDate.now();

        // =========================================
        // FREEZER INVENTORY
        // =========================================

        inventory.addBatch(new Batch(
                "B001",
                "Frozen Peas",
                50,
                today.plusDays(120),
                "Freezer"
        ));

        inventory.addBatch(new Batch(
                "B005",
                "Ice Cream",
                6,
                today.plusDays(27),
                "Freezer"
        ));

        inventory.addBatch(new Batch(
                "B006",
                "Frozen Chicken",
                20,
                today.plusDays(150),
                "Freezer"
        ));

        inventory.addBatch(new Batch(
                "B007",
                "Frozen Fish",
                18,
                today.plusDays(180),
                "Freezer"
        ));

        // =========================================
        // CHILLER INVENTORY
        // =========================================

        inventory.addBatch(new Batch(
                "B002",
                "Yogurt",
                30,
                today.plusDays(4),
                "Chiller"
        ));

        inventory.addBatch(new Batch(
                "B004",
                "Milk",
                40,
                today.minusDays(2),
                "Chiller"
        ));

        inventory.addBatch(new Batch(
                "B008",
                "Fresh Paneer",
                8,
                today.plusDays(2),
                "Chiller"
        ));

        inventory.addBatch(new Batch(
                "B009",
                "Butter",
                30,
                today.plusDays(75),
                "Chiller"
        ));

        inventory.addBatch(new Batch(
                "B010",
                "Fresh Cream",
                5,
                today,
                "Chiller"
        ));

        inventory.addBatch(new Batch(
                "B011",
                "Cheese",
                9,
                today.plusDays(45),
                "Chiller"
        ));

        inventory.addBatch(new Batch(
                "B012",
                "Orange Juice",
                0,
                today.plusDays(12),
                "Chiller"
        ));

        // =========================================
        // AMBIENT INVENTORY
        // =========================================

        inventory.addBatch(new Batch(
                "B003",
                "Bananas",
                20,
                today.plusDays(10),
                "Ambient"
        ));

        inventory.addBatch(new Batch(
                "B013",
                "Apples",
                50,
                today.plusDays(25),
                "Ambient"
        ));

        // =========================================
        // DISPLAY INITIAL INVENTORY
        // =========================================

        System.out.println("\n===== INITIAL INVENTORY =====");

        inventory.displayInventory();

        System.out.println(
                "\nTotal batches loaded: "
                        + inventory.getBatchCount()
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
}

