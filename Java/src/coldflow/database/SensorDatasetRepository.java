package coldflow.database;

import coldflow.SensorDatasetReading;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SensorDatasetRepository {

    private static final String INSERT_SQL =
            "INSERT INTO sensor_readings " +
            "(sensor_id, recorded_at, temperature, humidity) " +
            "VALUES (?, ?, ?, ?) " +
            "ON CONFLICT (sensor_id, recorded_at) DO NOTHING";

    private static final String SELECT_ALL =
            "SELECT sensor_id, recorded_at, temperature, humidity " +
            "FROM sensor_readings " +
            "ORDER BY sensor_id ASC, recorded_at ASC";

    /**
     * Inserts sensor readings in batches.
     * Used by SensorDatasetBulkImporter.
     */
    public int insertBatch(Connection connection,
                           List<SensorDatasetReading> readings)
            throws SQLException {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Database connection cannot be null.");
        }

        if (readings == null || readings.isEmpty()) {
            return 0;
        }

        int inserted = 0;

        try (PreparedStatement statement =
                     connection.prepareStatement(INSERT_SQL)) {

            for (SensorDatasetReading reading : readings) {

                if (reading == null
                        || reading.getSensorId() == null
                        || reading.getTimestamp() == null) {
                    throw new SQLException(
                            "Sensor reading is missing its sensor ID or timestamp.");
                }

                statement.setString(1, reading.getSensorId());

                statement.setTimestamp(
                        2, Timestamp.valueOf(reading.getTimestamp()));

                if (reading.getTemperature() == null) {
                    statement.setNull(3, Types.DOUBLE);
                } else {
                    statement.setDouble(3, reading.getTemperature());
                }

                if (reading.getHumidity() == null) {
                    statement.setNull(4, Types.DOUBLE);
                } else {
                    statement.setDouble(4, reading.getHumidity());
                }

                statement.addBatch();
            }

            int[] results = statement.executeBatch();

            for (int result : results) {
                if (result > 0) {
                    inserted += result;
                } else if (result == PreparedStatement.SUCCESS_NO_INFO) {
                    inserted++;
                }
            }
        }

        return inserted;
    }

    /**
     * Retrieves all historical sensor readings from Supabase.
     * Used by Sensor Monitoring.
     */
    public List<SensorDatasetReading> getAllReadings()
            throws SQLException {

        List<SensorDatasetReading> readings =
                new ArrayList<>(1_420_000);

        try (Connection connection = DatabaseManager.getConnection()) {

            if (connection == null || !connection.isValid(10)) {
                throw new SQLException(
                        "Could not establish a valid Supabase connection.");
            }

            boolean originalAutoCommit = connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     SELECT_ALL,
                                     ResultSet.TYPE_FORWARD_ONLY,
                                     ResultSet.CONCUR_READ_ONLY)) {

                    statement.setFetchSize(5000);
                    statement.setQueryTimeout(180);

                    try (ResultSet resultSet = statement.executeQuery()) {

                        while (resultSet.next()) {

                            String sensorId =
                                    resultSet.getString("sensor_id");

                            Timestamp recordedAt =
                                    resultSet.getTimestamp("recorded_at");

                            if (sensorId == null || recordedAt == null) {
                                continue;
                            }

                            double temperatureValue =
                                    resultSet.getDouble("temperature");

                            Double temperature =
                                    resultSet.wasNull()
                                            ? null : temperatureValue;

                            double humidityValue =
                                    resultSet.getDouble("humidity");

                            Double humidity =
                                    resultSet.wasNull()
                                            ? null : humidityValue;

                            readings.add(new SensorDatasetReading(
                                    sensorId,
                                    recordedAt.toLocalDateTime(),
                                    temperature,
                                    humidity));
                        }
                    }
                }

                connection.commit();

            } catch (SQLException | RuntimeException ex) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    ex.addSuppressed(rollbackException);
                }

                throw ex;

            } finally {

                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                    // Connection is closing.
                }
            }
        }

        return readings;
    }
}