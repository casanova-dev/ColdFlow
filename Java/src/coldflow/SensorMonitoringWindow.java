package coldflow;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SensorMonitoringWindow extends JFrame {

private static final Color BG = new Color(10, 16, 30);
private static final Color PANEL = new Color(15, 24, 43);
private static final Color CARD = new Color(18, 29, 51);
private static final Color TEXT = new Color(225, 237, 250);
private static final Color MUTED = new Color(164, 184, 211);
private static final Color CYAN = new Color(105, 210, 255);

private static final DateTimeFormatter DISPLAY_TIME =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

private final GenericDataStore<SensorDatasetReading> readingStore;

private final JComboBox<String> sensorSelector;
private final JLabel totalLabel;
private final JLabel temperatureLabel;
private final JLabel humidityLabel;
private final JLabel missingLabel;
private final JLabel rangeLabel;

private final DefaultTableModel tableModel;
private final JTable readingsTable;
private final SensorChartPanel chartPanel;

public SensorMonitoringWindow(List<SensorDatasetReading> readings) {

    if (readings == null || readings.isEmpty()) {
        throw new IllegalArgumentException(
                "No sensor readings are available.");
    }

    this.readingStore = new GenericDataStore<>();
    this.readingStore.addAll(readings);

    setTitle("COLDFlow | Historical Sensor Monitoring");
    setSize(1180, 800);
    setMinimumSize(new Dimension(900, 650));
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    getContentPane().setBackground(BG);
    setLayout(new BorderLayout(12, 12));

    JPanel header = createHeader();

    sensorSelector = new JComboBox<>(
            readingStore.getAll().stream()
                    .map(SensorDatasetReading::getSensorId)
                    .distinct()
                    .sorted()
                    .toArray(String[]::new));

    totalLabel = createMetricLabel();
    temperatureLabel = createMetricLabel();
    humidityLabel = createMetricLabel();
    missingLabel = createMetricLabel();
    rangeLabel = createMetricLabel();

    JPanel metricPanel = createMetricPanel();

    tableModel = new DefaultTableModel(
            new String[]{
                    "Timestamp",
                    "Temperature (°C)",
                    "Humidity (%)"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    readingsTable = new JTable(tableModel);
    styleTable();

    chartPanel = new SensorChartPanel();

    JPanel center = new JPanel(new GridLayout(2, 1, 10, 10));
    center.setBackground(BG);
    center.add(chartPanel);

    JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.setBackground(PANEL);
    tablePanel.setBorder(createPanelBorder("HISTORICAL READINGS"));
    tablePanel.add(new JScrollPane(readingsTable), BorderLayout.CENTER);

    center.add(tablePanel);

    JButton exportButton = createButton("Export Sensor CSV");
    exportButton.addActionListener(this::exportSensorData);

    JButton anomalyButton = createButton("Anomaly Analysis");
    anomalyButton.addActionListener(e -> openAnomalyAnalysis());

    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    footer.setBackground(BG);
    footer.add(anomalyButton);
    footer.add(exportButton);

    JPanel north = new JPanel(new BorderLayout(10, 10));
    north.setBackground(BG);
    north.add(header, BorderLayout.NORTH);

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
    controls.setBackground(PANEL);
    controls.setBorder(createPanelBorder("SENSOR SELECTION"));

    controls.add(createLabel("Select sensor:"));
    controls.add(sensorSelector);

    JButton refreshButton = createButton("Refresh");
    refreshButton.addActionListener(e -> refreshSelectedSensor());
    controls.add(refreshButton);

    north.add(controls, BorderLayout.SOUTH);

    JPanel content = new JPanel(new BorderLayout(10, 10));
    content.setBackground(BG);
    content.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    content.add(metricPanel, BorderLayout.NORTH);
    content.add(center, BorderLayout.CENTER);
    content.add(footer, BorderLayout.SOUTH);

    add(north, BorderLayout.NORTH);
    add(content, BorderLayout.CENTER);

    sensorSelector.addActionListener(e -> refreshSelectedSensor());

    refreshSelectedSensor();
}

private JPanel createHeader() {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(PANEL);
    panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

    JLabel title = new JLabel("HISTORICAL SENSOR MONITORING");
    title.setForeground(CYAN);
    title.setFont(new Font("SansSerif", Font.BOLD, 22));

    JLabel subtitle = new JLabel(
            "Imported environmental measurements | Historical dataset");
    subtitle.setForeground(MUTED);
    subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

    JPanel textPanel = new JPanel(new GridLayout(2, 1, 3, 3));
    textPanel.setBackground(PANEL);
    textPanel.add(title);
    textPanel.add(subtitle);

    panel.add(textPanel, BorderLayout.CENTER);
    return panel;
}

private JPanel createMetricPanel() {

    JPanel panel = new JPanel(new GridLayout(1, 5, 8, 8));
    panel.setBackground(BG);

    panel.add(createMetricCard("TOTAL RECORDS", totalLabel));
    panel.add(createMetricCard("TEMPERATURE", temperatureLabel));
    panel.add(createMetricCard("HUMIDITY", humidityLabel));
    panel.add(createMetricCard("MISSING", missingLabel));
    panel.add(createMetricCard("DATE RANGE", rangeLabel));

    return panel;
}

private JPanel createMetricCard(String title, JLabel value) {

    JPanel card = new JPanel(new BorderLayout(4, 8));
    card.setBackground(CARD);
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(42, 67, 99)),
            BorderFactory.createEmptyBorder(12, 10, 12, 10)));

    JLabel heading = createLabel(title);
    heading.setFont(new Font("SansSerif", Font.BOLD, 11));

    card.add(heading, BorderLayout.NORTH);
    card.add(value, BorderLayout.CENTER);

    return card;
}

private JLabel createMetricLabel() {

    JLabel label = new JLabel("--", SwingConstants.CENTER);
    label.setForeground(CYAN);
    label.setFont(new Font("SansSerif", Font.BOLD, 16));
    return label;
}

private JLabel createLabel(String text) {

    JLabel label = new JLabel(text);
    label.setForeground(MUTED);
    return label;
}

private JButton createButton(String text) {

    JButton button = new JButton(text);
    button.setBackground(new Color(25, 48, 76));
    button.setForeground(TEXT);
    button.setFocusPainted(false);
    return button;
}

private TitledBorder createPanelBorder(String title) {

    return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(42, 67, 99)),
            title,
            TitledBorder.LEADING,
            TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12),
            CYAN);
}

private void styleTable() {

    readingsTable.setBackground(CARD);
    readingsTable.setForeground(TEXT);
    readingsTable.setGridColor(new Color(42, 67, 99));
    readingsTable.setSelectionBackground(new Color(0, 105, 160));
    readingsTable.setSelectionForeground(Color.WHITE);
    readingsTable.setRowHeight(24);
    readingsTable.setFillsViewportHeight(true);

    readingsTable.getTableHeader().setBackground(new Color(22, 42, 67));
    readingsTable.getTableHeader().setForeground(CYAN);
}

private List<SensorDatasetReading> getSelectedReadings() {

    String sensorId = (String) sensorSelector.getSelectedItem();

    if (sensorId == null) {
        return new ArrayList<>();
    }

    return readingStore.getAll().stream()
            .filter(r -> sensorId.equals(r.getSensorId()))
            .sorted(Comparator.comparing(
                    SensorDatasetReading::getTimestamp))
            .collect(Collectors.toList());
}

private void refreshSelectedSensor() {

    List<SensorDatasetReading> selected = getSelectedReadings();

    if (selected.isEmpty()) {
        return;
    }

    long validTemperatures = selected.stream()
            .filter(SensorDatasetReading::hasTemperature)
            .count();

    long validHumidity = selected.stream()
            .filter(SensorDatasetReading::hasHumidity)
            .count();

    long recordsWithMissingData = selected.stream()
            .filter(r -> !r.hasTemperature() || !r.hasHumidity())
            .count();

    double minTemp = selected.stream()
            .filter(SensorDatasetReading::hasTemperature)
            .mapToDouble(r -> r.getTemperature())
            .min().orElse(Double.NaN);

    double maxTemp = selected.stream()
            .filter(SensorDatasetReading::hasTemperature)
            .mapToDouble(r -> r.getTemperature())
            .max().orElse(Double.NaN);

    double averageTemp = selected.stream()
            .filter(SensorDatasetReading::hasTemperature)
            .mapToDouble(r -> r.getTemperature())
            .average().orElse(Double.NaN);

    double averageHumidity = selected.stream()
            .filter(SensorDatasetReading::hasHumidity)
            .mapToDouble(r -> r.getHumidity())
            .average().orElse(Double.NaN);

    totalLabel.setText(String.format("%,d", selected.size()));

    temperatureLabel.setText(validTemperatures == 0
            ? "No data"
            : String.format(Locale.ROOT, "%.2f °C", averageTemp));

    humidityLabel.setText(validHumidity == 0
            ? "No data"
            : String.format(Locale.ROOT, "%.2f%%", averageHumidity));

    missingLabel.setText(String.format(
            "%,d (%.1f%%)",
            recordsWithMissingData,
            100.0 * recordsWithMissingData / selected.size()));

    rangeLabel.setText(
            selected.get(0).getTimestamp().toLocalDate()
                    + " to "
                    + selected.get(selected.size() - 1)
                            .getTimestamp().toLocalDate());

    tableModel.setRowCount(0);

    // Display the most recent 500 rows, not all 156,000+ rows.
    int start = Math.max(0, selected.size() - 500);

    for (int i = start; i < selected.size(); i++) {

        SensorDatasetReading reading = selected.get(i);

        tableModel.addRow(new Object[]{
                reading.getTimestamp().format(DISPLAY_TIME),
                reading.getTemperature() == null
                        ? "Missing"
                        : String.format(
                                Locale.ROOT,
                                "%.2f",
                                reading.getTemperature()),
                reading.getHumidity() == null
                        ? "Missing"
                        : String.format(
                                Locale.ROOT,
                                "%.2f",
                                reading.getHumidity())
        });
    }

    chartPanel.setReadings(selected);
    chartPanel.repaint();

    setTitle(String.format(
            Locale.ROOT,
            "COLDFlow | %s | Min %.2f°C | Max %.2f°C",
            sensorSelector.getSelectedItem(),
            minTemp,
            maxTemp));
}

private void exportSensorData(ActionEvent event) {

    List<SensorDatasetReading> selected = getSelectedReadings();

    if (selected.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "No readings are available to export.",
                "Export",
                JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Export Sensor Readings");

    String sensorId = (String) sensorSelector.getSelectedItem();

    chooser.setSelectedFile(
            new java.io.File(sensorId + "_readings.csv"));

    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    Path output = chooser.getSelectedFile().toPath();

    try (BufferedWriter writer = Files.newBufferedWriter(
            output, StandardCharsets.UTF_8)) {

        writer.write("Sensor;Date;Time;Temperature;Humidity");
        writer.newLine();

        for (SensorDatasetReading reading : selected) {

            writer.write(reading.getSensorId());
            writer.write(';');
            writer.write(reading.getTimestamp().toLocalDate().toString());
            writer.write(';');
            writer.write(reading.getTimestamp().toLocalTime().toString());
            writer.write(';');

            if (reading.getTemperature() != null) {
                writer.write(reading.getTemperature().toString());
            }

            writer.write(';');

            if (reading.getHumidity() != null) {
                writer.write(reading.getHumidity().toString());
            }

            writer.newLine();
        }

        JOptionPane.showMessageDialog(
                this,
                "Exported " + selected.size()
                        + " readings to:\n" + output.toAbsolutePath(),
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE);

    } catch (IOException ex) {

        JOptionPane.showMessageDialog(
                this,
                "Could not export readings:\n" + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

private void openAnomalyAnalysis() {

    // Demonstration thresholds only; configure these for the actual
    // storage requirements before treating alerts as operational warnings.
    SensorAnomalyDetector detector = new SensorAnomalyDetector(
            2.0,   // Example minimum temperature (°C)
            8.0,   // Example maximum temperature (°C)
            90.0,  // Example maximum humidity (%)
            3.0    // Statistical Z-score threshold
    );

    // The dataset is large, so run detection off the Swing UI thread.
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    SwingWorker<List<SensorAnomalyDetector.Anomaly>, Void> worker =
            new SwingWorker<>() {

        @Override
        protected List<SensorAnomalyDetector.Anomaly> doInBackground() {
            return detector.detect(readingStore.getAll());
        }

        @Override
        protected void done() {
            setCursor(Cursor.getDefaultCursor());

            try {
                List<SensorAnomalyDetector.Anomaly> anomalies = get();

                AnomalyAnalysisWindow window =
                        new AnomalyAnalysisWindow(anomalies);

                window.setVisible(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        SensorMonitoringWindow.this,
                        "Anomaly analysis failed: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    worker.execute();
}

private class SensorChartPanel extends JPanel {

    private List<SensorDatasetReading> readings = new ArrayList<>();

    SensorChartPanel() {
        setBackground(PANEL);
        setBorder(createPanelBorder("HISTORICAL TEMPERATURE"));
    }

    void setReadings(List<SensorDatasetReading> readings) {
        this.readings = readings;
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();

        try {
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 65;
            int right = 25;
            int top = 35;
            int bottom = 45;

            int graphWidth = getWidth() - left - right;
            int graphHeight = getHeight() - top - bottom;

            if (graphWidth <= 0 || graphHeight <= 0) {
                return;
            }

            List<SensorDatasetReading> valid = readings.stream()
                    .filter(SensorDatasetReading::hasTemperature)
                    .collect(Collectors.toList());

            if (valid.size() < 2) {
                g.setColor(TEXT);
                g.drawString(
                        "Not enough temperature readings to plot.",
                        left,
                        top + 20);
                return;
            }

            // Sample at most 1,000 points to keep drawing responsive.
            int step = Math.max(1, valid.size() / 1000);

            List<SensorDatasetReading> points = new ArrayList<>();

            for (int i = 0; i < valid.size(); i += step) {
                points.add(valid.get(i));
            }

            if (points.get(points.size() - 1)
                    != valid.get(valid.size() - 1)) {
                points.add(valid.get(valid.size() - 1));
            }

            double min = points.stream()
                    .mapToDouble(r -> r.getTemperature())
                    .min().orElse(0);

            double max = points.stream()
                    .mapToDouble(r -> r.getTemperature())
                    .max().orElse(1);

            if (min == max) {
                min -= 1;
                max += 1;
            }

            // Add a little vertical padding.
            double padding = (max - min) * 0.08;
            min -= padding;
            max += padding;

            g.setFont(new Font("SansSerif", Font.PLAIN, 11));

            for (int i = 0; i <= 5; i++) {

                int y = top + i * graphHeight / 5;
                double value = max - i * (max - min) / 5.0;

                g.setColor(new Color(38, 58, 83));
                g.drawLine(left, y, left + graphWidth, y);

                g.setColor(MUTED);
                g.drawString(
                        String.format(Locale.ROOT, "%.1f°C", value),
                        8,
                        y + 4);
            }

            g.setColor(CYAN);
            g.setStroke(new BasicStroke(2f));

            for (int i = 1; i < points.size(); i++) {

                double previous = points.get(i - 1).getTemperature();
                double current = points.get(i).getTemperature();

                int x1 = left + (i - 1) * graphWidth
                        / (points.size() - 1);

                int x2 = left + i * graphWidth
                        / (points.size() - 1);

                int y1 = top + (int) (
                        (max - previous) / (max - min) * graphHeight);

                int y2 = top + (int) (
                        (max - current) / (max - min) * graphHeight);

                g.drawLine(x1, y1, x2, y2);
            }

            g.setColor(MUTED);
            g.drawString("Earlier readings", left, getHeight() - 12);
            g.drawString(
                    "Latest: " + points.get(points.size() - 1)
                            .getTimestamp().format(DISPLAY_TIME),
                    Math.max(left, getWidth() - 260),
                    getHeight() - 12);

        } finally {
            g.dispose();
        }
    }
}


}
