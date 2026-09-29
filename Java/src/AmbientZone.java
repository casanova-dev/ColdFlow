package coldflow;

public class AmbientZone extends StorageZone {

    private final double minimumTemperature;
    private final double maximumTemperature;

    public AmbientZone() {

        super("Ambient", 25.0);

        minimumTemperature = 20.0;
        maximumTemperature = 30.0;
    }

    public double getMinimumTemperature() {
        return minimumTemperature;
    }

    public double getMaximumTemperature() {
        return maximumTemperature;
    }
}