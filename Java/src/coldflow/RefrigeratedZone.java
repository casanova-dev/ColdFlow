package coldflow;

public class RefrigeratedZone extends StorageZone {

    protected final double minimumTemperature;
    protected final double maximumTemperature;

    public RefrigeratedZone(
            String zoneName,
            double targetTemperature,
            double minimumTemperature,
            double maximumTemperature) {

        super(zoneName, targetTemperature);

        this.minimumTemperature = minimumTemperature;
        this.maximumTemperature = maximumTemperature;
    }

    // Common refrigerated-zone behavior
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

    @Override
    public void runCycle() {

        super.runCycle();

        if (isTemperatureSafe()) {

            System.out.println(
                    zoneName
                            + " Status: TEMPERATURE SAFE"
            );

        } else {

            System.out.println(
                    zoneName
                            + " Status: TEMPERATURE OUT OF RANGE"
            );
        }
    }
}