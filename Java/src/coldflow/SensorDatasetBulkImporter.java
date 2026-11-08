package coldflow;

import coldflow.database.DatabaseManager;
import coldflow.database.SensorDatasetRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * One-time, streaming importer for the historical sensor CSV files.
 *
 * Run this class manually once. It does not run when the dashboard opens.
 * Re-running it is safe because the database unique constraint skips
 * duplicate (sensor_id, recorded_at) records.
 */
public class SensorDatasetBulkImporter {

    private static final int BATCH_SIZE = 1000;
    private static final int PROGRESS_INTERVAL = 10000;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);

    private static final Pattern SENSOR_FILENAME =
            Pattern.compile("^(SENSOR\\d{2})(?:_PREPROCESSED)?\\.CSV$",
                    Pattern.CASE_INSENSITIVE);

    private final SensorDatasetRepository repository =
            new SensorDatasetRepository();

    private long validRows;
    private long insertedRows;
    private long missingMeasurements;
    private long skippedHeaders;
    private long invalidRows;
    private long duplicateRows;
    private int filesProcessed;

    public static void main(String[] args) {
        Path directory = args.length > 0
                ? Paths.get(args[0])
                : Paths.get("data", "preprocessed");

        SensorDatasetBulkImporter importer =
                new SensorDatasetBulkImporter();

        try {
            importer.importDirectory(directory);
        } catch (Exception ex) {
            System.err.println();
            System.err.println("HISTORICAL SENSOR IMPORT FAILED");
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void importDirectory(Path directory)
            throws IOException, SQLException {

        if (!Files.isDirectory(directory)) {
            throw new IOException(
                    "Dataset folder not found: "
                            + directory.toAbsolutePath());
        }

        List<Path> csvFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".csv"))
                    .sorted(Comparator.comparing(
                            path -> path.getFileName().toString()))
                    .forEach(csvFiles::add);
        }

        if (csvFiles.isEmpty()) {
            throw new IOException(
                    "No CSV files found in: "
                            + directory.toAbsolutePath());
        }

        System.out.println("==============================================");
        System.out.println("       COLDFLOW HISTORICAL DATA IMPORT");
        System.out.println("==============================================");
        System.out.println("Folder: " + directory.toAbsolutePath());
        System.out.println("CSV files found: " + csvFiles.size());
        System.out.println("Batch size: " + BATCH_SIZE);
        System.out.println("----------------------------------------------");

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null || !connection.isValid(10)) {
                throw new SQLException(
                        "Could not establish a valid Supabase connection.");
            }

            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                for (Path file : csvFiles) {
                    String sensorId = extractSensorId(
                            file.getFileName().toString());

                    if (sensorId == null) {
                        System.out.println(
                                "Skipping unrecognized file: "
                                        + file.getFileName());
                        continue;
                    }

                    importFile(connection, file, sensorId);
                    filesProcessed++;
                    connection.commit();

                    System.out.println(
                            "Completed " + file.getFileName()
                                    + " | valid rows so far: " + validRows
                                    + " | inserted so far: " + insertedRows);
                }

                connection.commit();

            } catch (IOException | SQLException | RuntimeException ex) {
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
                    // Connection is closing; preserve the original result.
                }
            }
        }

        duplicateRows = Math.max(0, validRows - insertedRows);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("             IMPORT SUMMARY");
        System.out.println("==============================================");
        System.out.println("Files processed          : " + filesProcessed);
        System.out.println("Valid CSV rows            : " + validRows);
        System.out.println("Inserted into Supabase    : " + insertedRows);
        System.out.println("Duplicates skipped        : " + duplicateRows);
        System.out.println("Rows with missing values  : " + missingMeasurements);
        System.out.println("Repeated headers skipped  : " + skippedHeaders);
        System.out.println("Invalid rows skipped      : " + invalidRows);
        System.out.println("----------------------------------------------");
        System.out.println("Import finished.");
        System.out.println("==============================================");
    }

    private void importFile(Connection connection,
                            Path file,
                            String sensorId)
            throws IOException, SQLException {

        List<SensorDatasetReading> batch =
                new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(";", -1);

                if (fields.length > 0
                        && fields[0].trim().equalsIgnoreCase("Date")) {
                    skippedHeaders++;
                    continue;
                }

                if (fields.length != 4) {
                    invalidRows++;
                    continue;
                }

                try {
                    LocalDate date = LocalDate.parse(
                            fields[0].trim(), DATE_FORMAT);
                    LocalTime time = LocalTime.parse(
                            fields[1].trim(), TIME_FORMAT);

                    Double temperature = parseMeasurement(fields[2]);
                    Double humidity = parseMeasurement(fields[3]);

                    if (temperature != null
                            && (temperature < -100 || temperature > 100)) {
                        invalidRows++;
                        continue;
                    }

                    if (humidity != null
                            && (humidity < 0 || humidity > 100)) {
                        invalidRows++;
                        continue;
                    }

                    if (temperature == null || humidity == null) {
                        missingMeasurements++;
                    }

                    batch.add(new SensorDatasetReading(
                            sensorId,
                            LocalDateTime.of(date, time),
                            temperature,
                            humidity));

                    validRows++;

                    if (batch.size() >= BATCH_SIZE) {
                        flushBatch(connection, batch);
                    }

                    if (validRows > 0
                            && validRows % PROGRESS_INTERVAL == 0) {
                        System.out.println(
                                "Progress: read " + validRows
                                        + " valid rows; inserted "
                                        + insertedRows + " rows...");
                    }

                } catch (DateTimeParseException
                         | NumberFormatException ex) {
                    invalidRows++;
                }
            }
        }

        if (!batch.isEmpty()) {
            flushBatch(connection, batch);
        }
    }

    private void flushBatch(Connection connection,
                            List<SensorDatasetReading> batch)
            throws SQLException {

        int attempted = batch.size();
        int inserted = repository.insertBatch(connection, batch);
        insertedRows += inserted;

        // Commit each batch to limit transaction size and make progress durable.
        connection.commit();
        batch.clear();

        if (inserted < attempted) {
            System.out.println(
                    "Batch saved: " + inserted + " new, "
                            + (attempted - inserted) + " duplicates skipped.");
        }
    }

    private static Double parseMeasurement(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        double number = Double.parseDouble(value.trim());

        if (!Double.isFinite(number)) {
            throw new NumberFormatException(
                    "Measurement must be a finite number.");
        }

        return number;
    }

    private static String extractSensorId(String filename) {
        Matcher matcher = SENSOR_FILENAME.matcher(
                filename.toUpperCase(Locale.ROOT));

        return matcher.matches()
                ? matcher.group(1).toUpperCase(Locale.ROOT)
                : null;
    }
}
