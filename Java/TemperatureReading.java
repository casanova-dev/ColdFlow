package coldflow;

public class TemperatureReading {

    private final double temperature;
    private final double humidity;
    private final long timestamp;

    public TemperatureReading(double temperature, double humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = System.currentTimeMillis();
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format(
            "Temperature: %.2f°C | Humidity: %.2f%%",
            temperature,
            humidity
        );
    }
}