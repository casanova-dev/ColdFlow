package coldflow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class AnomalyAnalysisWindow extends JFrame {

    private final List<SensorAnomalyDetector.Anomaly> anomalies;

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JComboBox<String> sensorFilter;
    private final JComboBox<String> typeFilter;
    private final JComboBox<String> severityFilter;

    private final JLabel resultCount;

    public AnomalyAnalysisWindow(
            List<SensorAnomalyDetector.Anomaly> anomalies) {

        this.anomalies = anomalies;

        setTitle("COLDFlow | Sensor Anomaly Analysis");
        setSize(1100, 650);
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        Color background = new Color(10, 16, 30);
        Color panelColor = new Color(15, 24, 43);
        Color cyan = new Color(105, 210, 255);

        getContentPane().setBackground(background);

        JLabel heading = new JLabel(
                "SENSOR ANOMALY ANALYSIS",
                SwingConstants.CENTER);

        heading.setForeground(cyan);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

        add(heading, BorderLayout.NORTH);

        sensorFilter = new JComboBox<>();
        sensorFilter.addItem("All Sensors");

        anomalies.stream()
                .map(SensorAnomalyDetector.Anomaly::getSensorId)
                .distinct()
                .sorted()
                .forEach(sensorFilter::addItem);

        typeFilter = new JComboBox<>(new String[]{
                "All Types",
                "RULE_BASED",
                "STATISTICAL",
                "DATA_QUALITY"
        });

        severityFilter = new JComboBox<>(new String[]{
                "All Severities",
                "CRITICAL",
                "WARNING",
                "REVIEW"
        });

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10));

        filters.setBackground(panelColor);
        filters.add(new JLabel("Sensor:"));
        filters.add(sensorFilter);
        filters.add(new JLabel("Detection:"));
        filters.add(typeFilter);
        filters.add(new JLabel("Severity:"));
        filters.add(severityFilter);

        JButton applyButton = new JButton("Apply Filters");
        JButton exportButton = new JButton("Export CSV");

        filters.add(applyButton);
        filters.add(exportButton);

        String[] columns = {
                "Sensor",
                "Timestamp",
                "Measurement",
                "Value",
                "Severity",
                "Detection Type",
                "Reason"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(155);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(115);
        table.getColumnModel().getColumn(6).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(background);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        center.add(filters, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);

        resultCount = new JLabel();
        resultCount.setForeground(cyan);
        resultCount.setBorder(
                BorderFactory.createEmptyBorder(8, 12, 8, 12));

        center.add(resultCount, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        applyButton.addActionListener(e -> applyFilters());
        exportButton.addActionListener(e -> exportFilteredResults());

        applyFilters();
    }

    private void applyFilters() {

        tableModel.setRowCount(0);

        String selectedSensor =
                (String) sensorFilter.getSelectedItem();

        String selectedType =
                (String) typeFilter.getSelectedItem();

        String selectedSeverity =
                (String) severityFilter.getSelectedItem();

        int count = 0;

        for (SensorAnomalyDetector.Anomaly anomaly : anomalies) {

            if (!"All Sensors".equals(selectedSensor)
                    && !selectedSensor.equals(anomaly.getSensorId())) {
                continue;
            }

            if (!"All Types".equals(selectedType)
                    && !selectedType.equals(anomaly.getDetectionType())) {
                continue;
            }

            if (!"All Severities".equals(selectedSeverity)
                    && !selectedSeverity.equals(anomaly.getSeverity())) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    anomaly.getSensorId(),
                    anomaly.getReading().getTimestamp(),
                    anomaly.getMeasurement(),
                    anomaly.getValue() == null
                            ? "Missing"
                            : String.format(
                                    Locale.ROOT,
                                    "%.2f",
                                    anomaly.getValue()),
                    anomaly.getSeverity(),
                    anomaly.getDetectionType(),
                    anomaly.getReason()
            });

            count++;
        }

        resultCount.setText(
                String.format(
                        Locale.ROOT,
                        "Displayed %d of %d detected events",
                        count,
                        anomalies.size()));
    }

    private void exportFilteredResults() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Anomaly Report");

        chooser.setSelectedFile(
                new java.io.File("COLDFlow_Anomaly_Report.csv"));

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String selectedSensor =
                (String) sensorFilter.getSelectedItem();

        String selectedType =
                (String) typeFilter.getSelectedItem();

        String selectedSeverity =
                (String) severityFilter.getSelectedItem();

        Path output = chooser.getSelectedFile().toPath();

        try (BufferedWriter writer = Files.newBufferedWriter(
                output, StandardCharsets.UTF_8)) {

            writer.write(
                    "Sensor;Timestamp;Measurement;Value;Severity;"
                            + "Detection Type;Reason");
            writer.newLine();

            for (SensorAnomalyDetector.Anomaly anomaly : anomalies) {

                if (!"All Sensors".equals(selectedSensor)
                        && !selectedSensor.equals(anomaly.getSensorId())) {
                    continue;
                }

                if (!"All Types".equals(selectedType)
                        && !selectedType.equals(
                                anomaly.getDetectionType())) {
                    continue;
                }

                if (!"All Severities".equals(selectedSeverity)
                        && !selectedSeverity.equals(
                                anomaly.getSeverity())) {
                    continue;
                }

                writer.write(anomaly.getSensorId() + ";");
                writer.write(
                        anomaly.getReading().getTimestamp() + ";");
                writer.write(anomaly.getMeasurement() + ";");
                writer.write(
                        anomaly.getValue() == null
                                ? "Missing"
                                : anomaly.getValue().toString());
                writer.write(";");
                writer.write(anomaly.getSeverity() + ";");
                writer.write(anomaly.getDetectionType() + ";");
                writer.write(anomaly.getReason());
                writer.newLine();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Anomaly report exported successfully.",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Export failed:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}