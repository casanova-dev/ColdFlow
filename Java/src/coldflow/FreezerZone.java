package coldflow;

public class FreezerZone
        extends RefrigeratedZone
        implements TemperatureControl, InventoryAware {

    public FreezerZone() {

        super(
                "Freezer",
                -20.0,
                -25.0,
                -15.0
        );
    }

    // =========================================================
    // METHOD OVERRIDING
    // =========================================================

    @Override
    public void runCycle() {

        // Execute RefrigeratedZone behavior
        super.runCycle();

        // Freezer-specific temperature check
        checkTemperature();
    }

    // =========================================================
    // TemperatureControl INTERFACE
    // =========================================================

    @Override
    public void checkTemperature() {

        double currentTemperature =
                registers.getR1();

        if (currentTemperature < minimumTemperature) {

            System.out.println(
                    "Freezer Status: TOO COLD"
            );

        } else if (currentTemperature > maximumTemperature) {

            System.out.println(
                    "Freezer Status: TOO WARM"
            );

        } else {

            System.out.println(
                    "Freezer Status: TEMPERATURE SAFE"
            );
        }
    }

    @Override
    public boolean isTemperatureSafe() {

        double currentTemperature =
                registers.getR1();

        return currentTemperature >= minimumTemperature
                && currentTemperature <= maximumTemperature;
    }

    // =========================================================
    // InventoryAware INTERFACE
    // =========================================================

    @Override
    public void displayInventoryStatus() {

        System.out.println(
                "Freezer Inventory Status: "
                        + (hasInventory()
                        ? "Inventory Available"
                        : "No Inventory")
        );
    }

    @Override
    public boolean hasInventory() {

        // Inventory is managed by InventoryManager.
        // This method implements the InventoryAware
        // interface responsibility for the zone.

        return true;
    }
}