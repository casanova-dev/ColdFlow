package coldflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SensorAnomalyDetector {

    // Configurable operational limits
    private final double minimumTemperature;
    private final double maximumTemperature;
    private final double maximumHumidity;

    // Z-score threshold for statistical outliers
    private final double zScoreThreshold;

    public SensorAnomalyDetector(
            double minimumTemperature,
            double maximumTemperature,
            double maximumHumidity,
            double zScoreThreshold) {

        if (!Double.isFinite(minimumTemperature)
                || !Double.isFinite(maximumTemperature)
                || !Double.isFinite(maximumHumidity)
                || !Double.isFinite(zScoreThreshold)) {
            throw new IllegalArgumentException(
                    "Configuration values must be finite.");
        }

        if (minimumTemperature >= maximumTemperature) {
            throw new IllegalArgumentException(
                    "Minimum temperature must be below maximum.");
        }

        if (maximumHumidity < 0 || maximumHumidity > 100) {
            throw new IllegalArgumentException(
                    "Maximum humidity must be between 0 and 100.");
        }

        if (zScoreThreshold <= 0) {
            throw new IllegalArgumentException(
                    "Z-score threshold must be positive.");
        }

        this.minimumTemperature = minimumTemperature;
        this.maximumTemperature = maximumTemperature;
        this.maximumHumidity = maximumHumidity;
        this.zScoreThreshold = zScoreThreshold;
    }

    public List<Anomaly> detect(List<SensorDatasetReading> readings) {

        if (readings == null) {
            throw new IllegalArgumentException(
                    "Readings list cannot be null.");
        }

        Map<String, List<SensorDatasetReading>> sensorGroups =
                new TreeMap<>();

        for (SensorDatasetReading reading : readings) {

            if (reading == null) {
                continue;
            }

            sensorGroups.computeIfAbsent(
                    reading.getSensorId(),
                    key -> new ArrayList<>()
            ).add(reading);
        }

        List<Anomaly> anomalies = new ArrayList<>();

        for (Map.Entry<String, List<SensorDatasetReading>> entry
                : sensorGroups.entrySet()) {

            List<SensorDatasetReading> sensorReadings =
                    entry.getValue();

            // Calculate statistics using available temperature values.
            List<Double> temperatures = new ArrayList<>();

            for (SensorDatasetReading reading : sensorReadings) {

                if (reading.hasTemperature()) {
                    temperatures.add(reading.getTemperature());
                }
            }

            double mean = calculateMean(temperatures);
            double standardDeviation =
                    calculateStandardDeviation(temperatures, mean);

            for (SensorDatasetReading reading : sensorReadings) {

                // Missing values are not interpreted as zero.
                if (!reading.hasTemperature()) {

                    anomalies.add(new Anomaly(
                            reading,
                            "TEMPERATURE",
                            null,
                            "Missing temperature measurement",
                            "WARNING",
                            "DATA_QUALITY"
                    ));

                } else {

                    double temperature = reading.getTemperature();

                    if (temperature < minimumTemperature) {

                        anomalies.add(new Anomaly(
                                reading,
                                "TEMPERATURE",
                                temperature,
                                "Below configured minimum: "
                                        + minimumTemperature + " °C",
                                "WARNING",
                                "RULE_BASED"
                        ));

                    } else if (temperature > maximumTemperature) {

                        anomalies.add(new Anomaly(
                                reading,
                                "TEMPERATURE",
                                temperature,
                                "Above configured maximum: "
                                        + maximumTemperature + " °C",
                                "CRITICAL",
                                "RULE_BASED"
                        ));
                    }

                    // Statistical detection is separate from operational limits.
                    if (temperatures.size() >= 2
                            && standardDeviation > 0) {

                        double zScore = Math.abs(
                                (temperature - mean) / standardDeviation);

                        if (zScore >= zScoreThreshold) {

                            anomalies.add(new Anomaly(
                                    reading,
                                    "TEMPERATURE",
                                    temperature,
                                    String.format(
                                            java.util.Locale.ROOT,
                                            "Statistical outlier: Z-score %.2f",
                                            zScore),
                                    "REVIEW",
                                    "STATISTICAL"
                            ));
                        }
                    }
                }

                if (!reading.hasHumidity()) {

                    anomalies.add(new Anomaly(
                            reading,
                            "HUMIDITY",
                            null,
                            "Missing humidity measurement",
                            "WARNING",
                            "DATA_QUALITY"
                    ));

                } else if (reading.getHumidity() > maximumHumidity) {

                    anomalies.add(new Anomaly(
                            reading,
                            "HUMIDITY",
                            reading.getHumidity(),
                            "Above configured maximum: "
                                    + maximumHumidity + " %",
                            "WARNING",
                            "RULE_BASED"
                    ));
                }
            }
        }

        return anomalies;
    }

    private double calculateMean(List<Double> values) {

        if (values.isEmpty()) {
            return Double.NaN;
        }

        double sum = 0;

        for (double value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    private double calculateStandardDeviation(
            List<Double> values,
            double mean) {

        if (values.size() < 2 || !Double.isFinite(mean)) {
            return 0;
        }

        double sumSquaredDifferences = 0;

        for (double value : values) {

            double difference = value - mean;

            sumSquaredDifferences += difference * difference;
        }

        // Population standard deviation
        return Math.sqrt(sumSquaredDifferences / values.size());
    }

    public static class Anomaly {

        private final SensorDatasetReading reading;
        private final String measurement;
        private final Double value;
        private final String reason;
        private final String severity;
        private final String detectionType;

        public Anomaly(
                SensorDatasetReading reading,
                String measurement,
                Double value,
                String reason,
                String severity,
                String detectionType) {

            this.reading = reading;
            this.measurement = measurement;
            this.value = value;
            this.reason = reason;
            this.severity = severity;
            this.detectionType = detectionType;
        }

        public SensorDatasetReading getReading() {
            return reading;
        }

        public String getSensorId() {
            return reading.getSensorId();
        }

        public String getMeasurement() {
            return measurement;
        }

        public Double getValue() {
            return value;
        }

        public String getReason() {
            return reason;
        }

        public String getSeverity() {
            return severity;
        }

        public String getDetectionType() {
            return detectionType;
        }

        @Override
        public String toString() {

            return getSensorId()
                    + " | "
                    + reading.getTimestamp()
                    + " | "
                    + measurement
                    + " | "
                    + (value == null ? "Missing" : value)
                    + " | "
                    + severity
                    + " | "
                    + reason;
        }
    }
}