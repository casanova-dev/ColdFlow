package coldflow.database;

import coldflow.TemperatureReading;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class TemperatureRepository {

    /**
     * Saves a temperature reading to Supabase
     * and updates the corresponding storage zone.
     */
    public void saveReading(
            String zoneName,
            TemperatureReading reading) throws SQLException {

        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Zone name must not be blank."
            );
        }

        if (reading == null) {
            throw new IllegalArgumentException(
                    "Temperature reading must not be null."
            );
        }

        if (!Double.isFinite(reading.getTemperature())
                || !Double.isFinite(reading.getHumidity())) {

            throw new IllegalArgumentException(
                    "Temperature and humidity must be finite numbers."
            );
        }

        final String findZoneSql =
                "SELECT zone_id, target_temperature "
                + "FROM storage_zones "
                + "WHERE LOWER(zone_name) = LOWER(?)";

        final String insertReadingSql =
                "INSERT INTO temperature_readings "
                + "(zone_id, temperature, humidity, recorded_at) "
                + "VALUES (?, ?, ?, ?)";

        final String updateZoneSql =
                "UPDATE storage_zones "
                + "SET current_temperature = ?, status = ? "
                + "WHERE zone_id = ?";

        try (Connection connection =
                     DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                long zoneId;
                double targetTemperature;

                // Find the storage zone
                try (PreparedStatement findZone =
                             connection.prepareStatement(findZoneSql)) {

                    findZone.setString(1, zoneName.trim());

                    try (ResultSet result =
                                 findZone.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException(
                                    "Storage zone not found: " + zoneName
                            );
                        }

                        zoneId = result.getLong("zone_id");

                        targetTemperature =
                                result.getDouble("target_temperature");
                    }
                }

                // Calculate temperature status
                double temperature = reading.getTemperature();

                double error =
                        Math.abs(temperature - targetTemperature);

                String status;

                if (error <= 2.0) {
                    status = "NORMAL";
                } else if (error <= 5.0) {
                    status = "WARNING";
                } else {
                    status = "CRITICAL";
                }

                // Insert historical temperature reading
                try (PreparedStatement insert =
                             connection.prepareStatement(insertReadingSql)) {

                    insert.setLong(1, zoneId);

                    insert.setDouble(
                            2,
                            reading.getTemperature()
                    );

                    insert.setDouble(
                            3,
                            reading.getHumidity()
                    );

                    insert.setTimestamp(
                            4,
                            Timestamp.from(
                                    Instant.ofEpochMilli(
                                            reading.getTimestamp()
                                    )
                            )
                    );

                    insert.executeUpdate();
                }

                // Update latest storage zone information
                try (PreparedStatement update =
                             connection.prepareStatement(updateZoneSql)) {

                    update.setDouble(
                            1,
                            reading.getTemperature()
                    );

                    update.setString(2, status);

                    update.setLong(3, zoneId);

                    if (update.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to update storage zone: " + zoneName
                        );
                    }
                }

                // Commit both operations
                connection.commit();

            } catch (SQLException | RuntimeException ex) {

                connection.rollback();

                throw ex;
            }
        }
    }
}