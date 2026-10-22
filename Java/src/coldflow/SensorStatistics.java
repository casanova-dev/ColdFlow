package coldflow;

import java.util.List;

public class SensorStatistics {

private final String sensorId;

private final long totalRecords;
private final long temperatureCount;
private final long humidityCount;

private final double minTemperature;
private final double maxTemperature;
private final double averageTemperature;

private final double minHumidity;
private final double maxHumidity;
private final double averageHumidity;

public SensorStatistics(
        String sensorId,
        List<SensorDatasetReading> readings) {

    this.sensorId = sensorId;

    long tempCount = 0;
    long humidCount = 0;

    double tempSum = 0;
    double humidSum = 0;

    double minTemp = Double.POSITIVE_INFINITY;
    double maxTemp = Double.NEGATIVE_INFINITY;

    double minHumid = Double.POSITIVE_INFINITY;
    double maxHumid = Double.NEGATIVE_INFINITY;

    for (SensorDatasetReading reading : readings) {

        if (!reading.getSensorId().equals(sensorId)) {
            continue;
        }

        Double temperature = reading.getTemperature();
        Double humidity = reading.getHumidity();

        if (temperature != null) {
            tempSum += temperature;
            tempCount++;

            minTemp = Math.min(minTemp, temperature);
            maxTemp = Math.max(maxTemp, temperature);
        }

        if (humidity != null) {
            humidSum += humidity;
            humidCount++;

            minHumid = Math.min(minHumid, humidity);
            maxHumid = Math.max(maxHumid, humidity);
        }
    }

    this.totalRecords = readings.stream()
            .filter(r -> r.getSensorId().equals(sensorId))
            .count();

    this.temperatureCount = tempCount;
    this.humidityCount = humidCount;

    this.minTemperature = tempCount > 0 ? minTemp : Double.NaN;
    this.maxTemperature = tempCount > 0 ? maxTemp : Double.NaN;
    this.averageTemperature =
            tempCount > 0 ? tempSum / tempCount : Double.NaN;

    this.minHumidity = humidCount > 0 ? minHumid : Double.NaN;
    this.maxHumidity = humidCount > 0 ? maxHumid : Double.NaN;
    this.averageHumidity =
            humidCount > 0 ? humidSum / humidCount : Double.NaN;
}

public String getSensorId() {
    return sensorId;
}

public long getTotalRecords() {
    return totalRecords;
}

public long getTemperatureCount() {
    return temperatureCount;
}

public long getHumidityCount() {
    return humidityCount;
}

public double getMinTemperature() {
    return minTemperature;
}

public double getMaxTemperature() {
    return maxTemperature;
}

public double getAverageTemperature() {
    return averageTemperature;
}

public double getMinHumidity() {
    return minHumidity;
}

public double getMaxHumidity() {
    return maxHumidity;
}

public double getAverageHumidity() {
    return averageHumidity;
}

@Override
public String toString() {

    return "\nSensor: " + sensorId
            + "\nTotal records: " + totalRecords
            + "\nValid temperature readings: " + temperatureCount
            + "\nValid humidity readings: " + humidityCount
            + String.format(
                    "\nTemperature: min=%.2f°C, max=%.2f°C, average=%.2f°C",
                    minTemperature,
                    maxTemperature,
                    averageTemperature)
            + String.format(
                    "\nHumidity: min=%.2f%%, max=%.2f%%, average=%.2f%%",
                    minHumidity,
                    maxHumidity,
                    averageHumidity)
            + "\n----------------------------------------";
}


}
