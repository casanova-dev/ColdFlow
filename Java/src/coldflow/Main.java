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
        // A StorageZone reference can refer to
        // different child-class objects.

        StorageZone zone;

        zone = freezer;

        System.out.println(
                "\nRuntime Polymorphism: "
                        + zone.getZoneName()
        );

        zone = chiller;

        System.out.println(
                "Runtime Polymorphism: "
                        + zone.getZoneName()
        );

        zone = ambient;

        System.out.println(
                "Runtime Polymorphism: "
                        + zone.getZoneName()
        );

        // =========================================
        // HYBRID INHERITANCE DEMONSTRATION
        // =========================================
        // FreezerZone inherits through a class
        // hierarchy and implements multiple interfaces.

        TemperatureControl temperatureControl =
                freezer;

        InventoryAware inventoryAware =
                freezer;

        System.out.println(
                "\n===== HYBRID INHERITANCE DEMO ====="
        );

        System.out.println(
                "Temperature Control Interface: "
                        + temperatureControl.isTemperatureSafe()
        );

        inventoryAware.displayInventoryStatus();

        // =========================================
        // Create Inventory
        // =========================================

        InventoryManager inventory =
                new InventoryManager();

        // =========================================
        // Freezer inventory
        // =========================================

        inventory.addBatch(
                new Batch(
                        "B001",
                        "Frozen Peas",
                        50,
                        LocalDate.of(2026, 8, 28),
                        "Freezer"
                )
        );

        // =========================================
        // Chiller inventory
        // =========================================

        inventory.addBatch(
                new Batch(
                        "B002",
                        "Yogurt",
                        30,
                        LocalDate.of(2026, 8, 25),
                        "Chiller"
                )
        );

        // =========================================
        // Ambient inventory
        // =========================================

        inventory.addBatch(
                new Batch(
                        "B003",
                        "Bananas",
                        20,
                        LocalDate.of(2026, 9, 2),
                        "Ambient"
                )
        );

        // =========================================
        // Another Chiller batch
        // =========================================

        inventory.addBatch(
                new Batch(
                        "B004",
                        "Milk",
                        40,
                        LocalDate.of(2026, 8, 30),
                        "Chiller"
                )
        );

        // =========================================
        // Start Dashboard
        // =========================================

        javax.swing.SwingUtilities.invokeLater(() -> {

            Dashboard dashboard =
                    new Dashboard(
                            freezer,
                            chiller,
                            ambient,
                            inventory
                    );

            dashboard.setVisible(true);
        });

        System.out.println(
                "\nCOLDFlow dashboard started."
        );
    }
}