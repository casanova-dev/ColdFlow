package coldflow;

public class AmbientZone extends StorageZone {

    private final double minimumTemperature;
    private final double maximumTemperature;

    public AmbientZone() {

        super("Ambient", 25.0);

        minimumTemperature = 20.0;
        maximumTemperature = 30.0;
    }

    // METHOD OVERRIDING
    @Override
    public void runCycle() {

        // Execute parent StorageZone cycle
        super.runCycle();

        // Ambient-specific temperature validation
        double currentTemperature =
                registers.getR1();

        if (currentTemperature < minimumTemperature) {

            System.out.println(
                    "Ambient Status: TOO COLD"
            );

        } else if (currentTemperature > maximumTemperature) {

            System.out.println(
                    "Ambient Status: TOO WARM"
            );

        } else {

            System.out.println(
                    "Ambient Status: TEMPERATURE SAFE"
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