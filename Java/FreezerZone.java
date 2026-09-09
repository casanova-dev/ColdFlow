package coldflow;

public class FreezerZone extends StorageZone {

    private final double minimumTemperature;

    private final double maximumTemperature;

    public FreezerZone() {

        super("Freezer", -20.0);

        minimumTemperature = -25.0;

        maximumTemperature = -15.0;
    }

    public double getMinimumTemperature() {

        return minimumTemperature;
    }

    public double getMaximumTemperature() {

        return maximumTemperature;
    }
}