package coldflow;

public class ChillerZone extends StorageZone {

    private final double minimumTemperature;
    private final double maximumTemperature;

    public ChillerZone() {

        super("Chiller", 4.0);

        minimumTemperature = 2.0;
        maximumTemperature = 8.0;
    }

    // METHOD OVERRIDING
    @Override
    public void runCycle() {

        // Execute parent StorageZone cycle
        super.runCycle();

        // Chiller-specific temperature validation
        double currentTemperature =
                registers.getR1();

        if (currentTemperature < minimumTemperature) {

            System.out.println(
                    "Chiller Status: TOO COLD"
            );

        } else if (currentTemperature > maximumTemperature) {

            System.out.println(
                    "Chiller Status: TOO WARM"
            );

        } else {

            System.out.println(
                    "Chiller Status: TEMPERATURE SAFE"
            );
        }
    }

    // ABSTRACTION
    @Override
    public boolean isTemperatureSafe() {

        double currentTemperature =
                registers.getR1();

        return currentTemperature >= minimumTemperature
                && currentTemperature <= maximumTemperature;
    }

    public double getMinimumTemperature() {
        return minimumTemperature;
    }

    public double getMaximumTemperature() {
        return maximumTemperature;
    }
}