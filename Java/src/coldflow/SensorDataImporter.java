package coldflow;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class SensorDataImporter {


private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd.MM.uuuu");

private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm:ss");

public static class ImportResult {

    private final List<SensorDatasetReading> readings;
    private final int missingMeasurements;
    private final int skippedHeaders;
    private final int invalidRows;
    private final int filesProcessed;

    public ImportResult(
            List<SensorDatasetReading> readings,
            int missingMeasurements,
            int skippedHeaders,
            int invalidRows,
            int filesProcessed) {

        this.readings = readings;
        this.missingMeasurements = missingMeasurements;
        this.skippedHeaders = skippedHeaders;
        this.invalidRows = invalidRows;
        this.filesProcessed = filesProcessed;
    }

    public List<SensorDatasetReading> getReadings() {
        return readings;
    }

    public int getMissingMeasurements() {
        return missingMeasurements;
    }

    public int getSkippedHeaders() {
        return skippedHeaders;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }
}

public ImportResult importDirectory(Path directory)
        throws IOException {

    if (!Files.isDirectory(directory)) {
        throw new IOException(
                "Dataset folder not found: "
                        + directory.toAbsolutePath());
    }

    List<Path> csvFiles = new ArrayList<>();

    try (Stream<Path> paths = Files.list(directory)) {
        paths.filter(Files::isRegularFile)
                .filter(path -> path.getFileName()
                        .toString()
                        .toLowerCase(Locale.ROOT)
                        .endsWith(".csv"))
                .sorted()
                .forEach(csvFiles::add);
    }

    if (csvFiles.isEmpty()) {
        throw new IOException(
                "No CSV files found in: "
                        + directory.toAbsolutePath());
    }

    List<SensorDatasetReading> readings = new ArrayList<>();

    int missingMeasurements = 0;
    int skippedHeaders = 0;
    int invalidRows = 0;
    int filesProcessed = 0;

    for (Path file : csvFiles) {

        String filename = file.getFileName().toString();
        String sensorId = extractSensorId(filename);

        if (sensorId == null) {
            System.err.println(
                    "Skipping unrecognized sensor file: " + filename);
            continue;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split(";", -1);

                // Skip the original header and any repeated headers.
                if (fields.length > 0
                        && fields[0].trim()
                                .equalsIgnoreCase("Date")) {
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

                    Double temperature =
                            parseMeasurement(fields[2]);

                    Double humidity =
                            parseMeasurement(fields[3]);

                    // Preserve missing values; never convert them to zero.
                    if (temperature == null || humidity == null) {
                        missingMeasurements++;
                    }

                    // Basic physical-domain validation.
                    if (temperature != null
                            && (temperature < -100
                            || temperature > 100)) {
                        invalidRows++;
                        continue;
                    }

                    if (humidity != null
                            && (humidity < 0 || humidity > 100)) {
                        invalidRows++;
                        continue;
                    }

                    LocalDateTime timestamp =
                            LocalDateTime.of(date, time);

                    readings.add(new SensorDatasetReading(
                            sensorId,
                            timestamp,
                            temperature,
                            humidity));

                } catch (DateTimeParseException
                        | NumberFormatException ex) {
                    invalidRows++;
                }
            }

            filesProcessed++;

        } catch (IOException ex) {
            System.err.println(
                    "Could not read " + filename
                            + ": " + ex.getMessage());
            throw ex;
        }
    }

    if (readings.isEmpty()) {
        throw new IOException(
                "No valid timestamped readings were imported.");
    }

    return new ImportResult(
            readings,
            missingMeasurements,
            skippedHeaders,
            invalidRows,
            filesProcessed);
}

private String extractSensorId(String filename) {

    String normalized = filename.toUpperCase(Locale.ROOT);

    if (!normalized.matches(
            "SENSOR\\d{2}(_PREPROCESSED)?\\.CSV")) {
        return null;
    }

    int suffixIndex = normalized.indexOf("_PREPROCESSED");

    if (suffixIndex >= 0) {
        return normalized.substring(0, suffixIndex);
    }

    return normalized.substring(0, normalized.length() - 4);
}

private Double parseMeasurement(String value) {

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

private void printImportReport(ImportResult result) {

    List<SensorDatasetReading> readings = result.getReadings();

    System.out.println();
    System.out.println("==========================================");
    System.out.println("       COLDFLOW DATASET IMPORT REPORT");
    System.out.println("==========================================");

    System.out.println(
            "Files processed             : "
                    + result.getFilesProcessed());

    System.out.println(
            "Valid timestamped records   : "
                    + readings.size());

    System.out.println(
            "Records with missing values : "
                    + result.getMissingMeasurements());

    System.out.println(
            "Repeated headers skipped    : "
                    + result.getSkippedHeaders());

    System.out.println(
            "Invalid rows skipped        : "
                    + result.getInvalidRows());

    System.out.println("------------------------------------------");
    System.out.println("STATISTICS FOR ALL SENSORS");
    System.out.println("------------------------------------------");

    // Group records by sensor so statistics can be calculated
    // without scanning the entire dataset nine separate times.
    Map<String, List<SensorDatasetReading>> sensorGroups =
            new TreeMap<>();

    for (SensorDatasetReading reading : readings) {
        sensorGroups.computeIfAbsent(
                reading.getSensorId(),
                key -> new ArrayList<>()).add(reading);
    }

    for (Map.Entry<String, List<SensorDatasetReading>> entry
            : sensorGroups.entrySet()) {

        SensorStatistics statistics = new SensorStatistics(
                entry.getKey(),
                entry.getValue());

        System.out.println(statistics);
    }

    System.out.println("==========================================");
}

public static void main(String[] args) {

    Path datasetDirectory =
            Paths.get("data", "preprocessed");

    SensorDataImporter importer = new SensorDataImporter();

    try {
        ImportResult result =
                importer.importDirectory(datasetDirectory);

        importer.printImportReport(result);

        System.out.println();
        System.out.println("FIRST FIVE IMPORTED RECORDS");
        System.out.println("------------------------------------------");

        List<SensorDatasetReading> readings = result.getReadings();

        for (int i = 0; i < Math.min(5, readings.size()); i++) {
            System.out.println(readings.get(i));
        }

    } catch (IOException ex) {
        System.err.println(
                "Dataset import failed: " + ex.getMessage());
    }
}


}
