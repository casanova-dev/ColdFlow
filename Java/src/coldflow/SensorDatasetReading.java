package coldflow;

import java.time.LocalDateTime;

public class SensorDatasetReading {
private final String sensorId;
private final LocalDateTime timestamp;
private final Double temperature;
private final Double humidity;

public SensorDatasetReading(
        String sensorId,
        LocalDateTime timestamp,
        Double temperature,
        Double humidity) {

    this.sensorId = sensorId;
    this.timestamp = timestamp;
    this.temperature = temperature;
    this.humidity = humidity;
}

public String getSensorId() {
    return sensorId;
}

public LocalDateTime getTimestamp() {
    return timestamp;
}

public Double getTemperature() {
    return temperature;
}

public Double getHumidity() {
    return humidity;
}

public boolean hasTemperature() {
    return temperature != null;
}

public boolean hasHumidity() {
    return humidity != null;
}

@Override
public String toString() {
    return "Sensor: " + sensorId
            + " | Time: " + timestamp
            + " | Temperature: "
            + (temperature == null ? "Missing" : temperature + " °C")
            + " | Humidity: "
            + (humidity == null ? "Missing" : humidity + " %");
}


}
