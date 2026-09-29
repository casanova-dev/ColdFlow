package coldflow;

public class ChillerZone extends StorageZone {

    private final double minimumTemperature;
    private final double maximumTemperature;

    public ChillerZone() {

        super("Chiller", 4.0);

        minimumTemperature = 2.0;
        maximumTemperature = 8.0;
    }

    public double getMinimumTemperature() {
        return minimumTemperature;
    }

    public double getMaximumTemperature() {
        return maximumTemperature;
    }
}