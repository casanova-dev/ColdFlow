package coldflow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.io.IOException;
import java.nio.file.Paths;

public class Dashboard extends JFrame {

    private final FreezerZone freezer;
    private final ChillerZone chiller;
    private final AmbientZone ambient;
    private final InventoryManager inventory;

    private JLabel freezerCurrent;
    private JLabel chillerCurrent;
    private JLabel ambientCurrent;

    private JLabel freezerStatus;
    private JLabel chillerStatus;
    private JLabel ambientStatus;

    private JLabel freezerCOA;
    private JLabel chillerCOA;
    private JLabel ambientCOA;

    private TemperatureGraphPanel graphPanel;

    private int simulationCycle = 0;

    public Dashboard(
            FreezerZone freezer,
            ChillerZone chiller,
            AmbientZone ambient,
            InventoryManager inventory) {

        this.freezer = freezer;
        this.chiller = chiller;
        this.ambient = ambient;
        this.inventory = inventory;

        configureLightLookAndFeel();

        setTitle("COLDFlow | Smart Cold-Chain Warehouse");
        setSize(1180, 850);
        setMinimumSize(new Dimension(1000, 740));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        buildDashboard();
        startMonitoring();
    }

    // Simple, clean light theme
    private static final Color BG = new Color(248, 250, 252);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color CYAN = new Color(37, 99, 235);

    private void openSensorMonitoring() {

    try {
        SensorDataImporter importer = new SensorDataImporter();

        SensorDataImporter.ImportResult result =
                importer.importDirectory(
                        Paths.get("data", "preprocessed"));

        SensorMonitoringWindow window =
                new SensorMonitoringWindow(result.getReadings());

        window.setVisible(true);

    } catch (IOException ex) {

        JOptionPane.showMessageDialog(
                this,
                "Unable to load the sensor dataset:\n" + ex.getMessage(),
                "Dataset Import Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
    private void configureLightLookAndFeel() {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.background", new Color(243, 244, 246));
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(219, 234, 254));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.gridColor", new Color(229, 231, 235));
        UIManager.put("TableHeader.background", new Color(243, 244, 246));
        UIManager.put("TableHeader.foreground", TEXT);
    }

    // Applies simple light styling to the inventory window and its controls.
    private void applyDarkTheme(Component component) {
        if (component instanceof JFrame || component instanceof JPanel) {
            component.setBackground(Color.WHITE);
        } else if (component instanceof JLabel) {
            component.setForeground(TEXT);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setBackground(new Color(243, 244, 246));
            button.setForeground(TEXT);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(209, 213, 219)),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        } else if (component instanceof JTextArea) {
            component.setBackground(Color.WHITE);
            component.setForeground(TEXT);
            ((JTextArea) component).setCaretColor(CYAN);
        } else if (component instanceof JTextField) {
            component.setBackground(Color.WHITE);
            component.setForeground(TEXT);
            ((JTextField) component).setCaretColor(CYAN);
        } else if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setBackground(Color.WHITE);
            table.setForeground(TEXT);
            table.setGridColor(new Color(229, 231, 235));
            table.setSelectionBackground(new Color(219, 234, 254));
            table.setSelectionForeground(TEXT);
            table.setFillsViewportHeight(true);
            table.setRowHeight(28);
            table.setShowVerticalLines(false);
            table.setIntercellSpacing(new Dimension(0, 1));
            table.getTableHeader().setBackground(new Color(243, 244, 246));
            table.getTableHeader().setForeground(TEXT);
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        } else if (component instanceof JScrollPane) {
            component.setBackground(Color.WHITE);
            ((JScrollPane) component).getViewport().setBackground(Color.WHITE);
            ((JScrollPane) component).setBorder(
                    BorderFactory.createLineBorder(new Color(229, 231, 235)));
        } else if (component instanceof JViewport) {
            component.setBackground(Color.WHITE);
        } else if (component instanceof JComboBox) {
            component.setBackground(Color.WHITE);
            component.setForeground(TEXT);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyDarkTheme(child);
            }
        }
    }

    // BUILD DASHBOARD
    // =========================================================

    private void buildDashboard() {
        setLayout(new BorderLayout(12, 12));

        JPanel headerPanel = new JPanel(new BorderLayout(12, 8));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel title = new JLabel("COLDFlow | Smart Cold-Chain Warehouse");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(37, 99, 235));
        headerPanel.add(title, BorderLayout.CENTER);

        JButton inventoryButton = new JButton("Inventory");
        styleDashboardButton(inventoryButton, true);
        inventoryButton.addActionListener(e -> showInventoryWindow());

        JButton sensorButton = new JButton("Sensor Monitoring");
        styleDashboardButton(sensorButton, false);
        sensorButton.addActionListener(e -> openSensorMonitoring());

        JButton exitButton = new JButton("Exit");
        styleDashboardButton(exitButton, false);
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this, "Are you sure you want to exit COLDFlow?",
                    "Exit COLDFlow", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(inventoryButton);
        buttonPanel.add(sensorButton);
        buttonPanel.add(exitButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 16, 16, 16));

        JPanel zonePanel = new JPanel(new GridLayout(1, 3, 12, 12));
        zonePanel.setBackground(BG);
        zonePanel.add(createZoneCard(
                freezer.getZoneName(), freezer.getTargetTemperature(), "Freezer"));
        zonePanel.add(createZoneCard(
                chiller.getZoneName(), chiller.getTargetTemperature(), "Chiller"));
        zonePanel.add(createZoneCard(
                ambient.getZoneName(), ambient.getTargetTemperature(), "Ambient"));
        mainPanel.add(zonePanel, BorderLayout.NORTH);

        graphPanel = new TemperatureGraphPanel();
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                " Temperature Over Time ",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(37, 99, 235)));
        mainPanel.add(graphPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        bottomPanel.setBackground(BG);
        bottomPanel.add(createCOAPanel());
        bottomPanel.add(createInventorySummaryPanel());
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void styleDashboardButton(JButton button, boolean primary) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(primary ? new Color(37, 99, 235) : new Color(243, 244, 246));
        button.setForeground(primary ? Color.WHITE : TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary
                        ? new Color(37, 99, 235) : new Color(209, 213, 219)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    // =========================================================
    // ZONE CARD
    // =========================================================

    private JPanel createZoneCard(
            String zoneName,
            double targetTemperature,
            String zoneType) {

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        Color zoneAccent = zoneType.equals("Freezer")
                ? new Color(64, 170, 255)
                : zoneType.equals("Chiller")
                    ? new Color(53, 220, 190)
                    : new Color(255, 174, 74);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(8, 8, 8, 8),
                        "  " + zoneName.toUpperCase() + "  ",
                        TitledBorder.LEADING, TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 14), zoneAccent)));

        JLabel target =
                new JLabel(
                        String.format(
                                "Target: %.1f °C",
                                targetTemperature
                        ),
                        SwingConstants.CENTER
                );

        target.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        JLabel current =
                new JLabel(
                        "Current: -- °C",
                        SwingConstants.CENTER
                );

        current.setFont(
                new Font("Monospaced", Font.BOLD, 27)
        );

        JLabel status =
                new JLabel(
                        "STATUS: STARTING",
                        SwingConstants.CENTER
                );

        status.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        if (zoneType.equals("Freezer")) {

            freezerCurrent = current;
            freezerStatus = status;

        } else if (zoneType.equals("Chiller")) {

            chillerCurrent = current;
            chillerStatus = status;

        } else {

            ambientCurrent = current;
            ambientStatus = status;
        }

        target.setForeground(new Color(107, 114, 128));
        current.setForeground(new Color(31, 41, 55));
        status.setForeground(new Color(91, 222, 168));
        JPanel information = new JPanel(new GridLayout(3, 1, 4, 4));
        information.setBackground(Color.WHITE);

        information.add(target);
        information.add(current);
        information.add(status);

        panel.add(
                information,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // COA PANEL
    // =========================================================

    private JPanel createCOAPanel() {

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                4,
                                1,
                                3,
                                3
                        )
                );

        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                " COA Control Registers ", TitledBorder.LEADING,
                TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12),
                new Color(37, 99, 235)));

        freezerCOA = new JLabel();
        chillerCOA = new JLabel();
        ambientCOA = new JLabel();

        JLabel explanation =
                new JLabel(
                        "R0 = Target | R1 = Current | R2 = Error | R3 = Control | R4 = Energy"
                );

        panel.add(explanation);
        panel.add(freezerCOA);
        panel.add(chillerCOA);
        panel.add(ambientCOA);

        return panel;
    }

    // =========================================================
    // INVENTORY SUMMARY
    // =========================================================

    private JPanel createInventorySummaryPanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                " Inventory Summary ", TitledBorder.LEADING,
                TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12),
                new Color(37, 99, 235)));

        JTextArea text =
                new JTextArea();

        text.setEditable(false);

        text.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        12
                )
        );

        updateInventorySummary(text);

        panel.add(
                new JScrollPane(text),
                BorderLayout.CENTER
        );

        return panel;
    }

    private void updateInventorySummary(
            JTextArea text) {

        text.setText("");

        text.append(
                "Total Batches: "
                        + inventory.getBatchCount()
                        + "\n\n"
        );

        text.append(
                "Earliest Expiring Batch:\n"
        );

        Batch next =
                inventory.getNextBatch();

        if (next != null) {

            text.append(
                    next.toString()
            );

        } else {

            text.append(
                    "No batches available."
            );
        }
    }

    // =========================================================
    // INVENTORY WINDOW
    // =========================================================

    private void showInventoryWindow() {

        JFrame inventoryFrame =
                new JFrame(
                        "COLDFlow - Inventory Management"
                );

        inventoryFrame.setSize(
                920,
                580
        );

        inventoryFrame.setLocationRelativeTo(this);

        inventoryFrame.setLayout(
                new BorderLayout(10, 10)
        );
        inventoryFrame.getContentPane().setBackground(Color.WHITE);

        // =====================================================
        // TITLE
        // =====================================================

        JLabel title =
                new JLabel(
                        "Inventory Management",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );

        title.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        5,
                        10
                )
        );

        inventoryFrame.add(
                title,
                BorderLayout.NORTH
        );

        // =====================================================
        // TABLE
        // =====================================================

        String[] columnNames = {
                "Batch ID",
                "Product",
                "Quantity",
                "Expiry Date",
                "Zone"
        };

        DefaultTableModel tableModel =
                new DefaultTableModel(
                        columnNames,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        JTable table =
                new JTable(tableModel);

        table.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );

        table.setRowHeight(28);

        table.getTableHeader().setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        populateInventoryTable(tableModel);

        JScrollPane scrollPane =
                new JScrollPane(table);

        inventoryFrame.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // =====================================================
        // BUTTON PANEL
        // =====================================================

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                8,
                                8
                        )
                );

        JButton addButton =
                new JButton("Add Batch");

        JButton searchButton =
                new JButton("Search Batch");

        JButton updateButton =
                new JButton("Update Quantity");

        JButton deleteButton =
                new JButton("Delete Batch");

        JButton dispatchButton =
                new JButton("Dispatch Next Batch (FEFO)");

        JButton refreshButton =
                new JButton("Refresh");

        JButton closeButton =
                new JButton("Close");

        // =====================================================
        // ADD
        // =====================================================

        addButton.addActionListener(
                e -> {

                    showAddBatchDialog();

                    refreshInventoryTable(
                            tableModel
                    );
                }
        );

        // =====================================================
        // SEARCH
        // =====================================================

        searchButton.addActionListener(
                e -> showSearchDialog()
        );

        // =====================================================
        // UPDATE
        // =====================================================

        updateButton.addActionListener(
                e -> {

                    showUpdateQuantityDialog();

                    refreshInventoryTable(
                            tableModel
                    );
                }
        );

        // =====================================================
        // DELETE
        // =====================================================

        deleteButton.addActionListener(
                e -> {

                    showDeleteBatchDialog();

                    refreshInventoryTable(
                            tableModel
                    );
                }
        );

        // =====================================================
        // FEFO DISPATCH
        // =====================================================

        dispatchButton.addActionListener(
                e -> {

                    dispatchNextBatch();

                    refreshInventoryTable(
                            tableModel
                    );
                }
        );

        // =====================================================
        // REFRESH
        // =====================================================

        refreshButton.addActionListener(
                e ->
                        refreshInventoryTable(
                                tableModel
                        )
        );

        // =====================================================
        // CLOSE
        // =====================================================

        closeButton.addActionListener(
                e ->
                        inventoryFrame.dispose()
        );

        buttonPanel.add(addButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(dispatchButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        inventoryFrame.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        applyDarkTheme(inventoryFrame);
        inventoryFrame.setVisible(true);
    }

    // =========================================================
    // ADD BATCH
    // =========================================================

    private void showAddBatchDialog() {

        JTextField batchIdField =
                new JTextField();

        JTextField productField =
                new JTextField();

        JTextField quantityField =
                new JTextField();

        JTextField expiryField =
                new JTextField();

        JComboBox<String> zoneBox =
                new JComboBox<>(
                        new String[]{
                                "Freezer",
                                "Chiller",
                                "Ambient"
                        }
                );

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                5,
                                2,
                                8,
                                8
                        )
                );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        panel.add(
                new JLabel("Batch ID:")
        );

        panel.add(batchIdField);

        panel.add(
                new JLabel("Product:")
        );

        panel.add(productField);

        panel.add(
                new JLabel("Quantity:")
        );

        panel.add(quantityField);

        panel.add(
                new JLabel("Expiry Date (YYYY-MM-DD):")
        );

        panel.add(expiryField);

        panel.add(
                new JLabel("Zone:")
        );

        panel.add(zoneBox);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Add New Batch",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String batchId =
                batchIdField.getText().trim();

        String product =
                productField.getText().trim();

        String quantityText =
                quantityField.getText().trim();

        String expiryText =
                expiryField.getText().trim();

        String zone =
                (String) zoneBox.getSelectedItem();

        if (
                batchId.isEmpty()
                        || product.isEmpty()
                        || quantityText.isEmpty()
                        || expiryText.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill all fields.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Prevent duplicate Batch IDs
        if (inventory.searchBatch(batchId) != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Batch ID already exists.",
                    "Duplicate Batch ID",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        try {

            int quantity =
                    Integer.parseInt(
                            quantityText
                    );

            if (quantity <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Quantity must be greater than zero.",
                        "Invalid Quantity",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            java.time.LocalDate expiryDate =
                    java.time.LocalDate.parse(
                            expiryText
                    );

            Batch batch =
                    new Batch(
                            batchId,
                            product,
                            quantity,
                            expiryDate,
                            zone
                    );

            inventory.addBatch(batch);

            JOptionPane.showMessageDialog(
                    this,
                    "Batch added successfully!\n\n"
                            + batch,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Quantity must be a valid number.",
                    "Invalid Quantity",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid expiry date.\n"
                            + "Use format: YYYY-MM-DD",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void showSearchDialog() {

        String search =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Batch ID or Product Name:"
                );

        if (
                search == null
                        || search.trim().isEmpty()) {

            return;
        }

        search =
                search.trim();

        Batch exactBatch =
                inventory.searchBatch(search);

        if (exactBatch != null) {

            showBatchDetails(
                    exactBatch
            );

            return;
        }

        List<Batch> matches =
                inventory.searchProduct(search);

        if (matches.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No matching batch found.",
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        StringBuilder result =
                new StringBuilder();

        result.append(
                "Matching Batches:\n\n"
        );

        for (Batch batch : matches) {

            result.append(
                    batch
            );

            result.append("\n\n");
        }

        JTextArea area =
                new JTextArea(
                        result.toString()
                );

        area.setEditable(false);

        area.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        13
                )
        );

        JScrollPane scroll =
                new JScrollPane(area);

        scroll.setPreferredSize(
                new Dimension(
                        750,
                        300
                )
        );

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Search Result",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showBatchDetails(
            Batch batch) {

        JOptionPane.showMessageDialog(
                this,
                batch.toString(),
                "Batch Found",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // UPDATE QUANTITY
    // =========================================================

    private void showUpdateQuantityDialog() {

        String batchId =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Batch ID:"
                );

        if (
                batchId == null
                        || batchId.trim().isEmpty()) {

            return;
        }

        batchId =
                batchId.trim();

        Batch batch =
                inventory.searchBatch(batchId);

        if (batch == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Batch not found.",
                    "Update Quantity",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String quantityText =
                JOptionPane.showInputDialog(
                        this,
                        "Current quantity: "
                                + batch.getQuantity()
                                + "\n\nEnter new quantity:"
                );

        if (
                quantityText == null
                        || quantityText.trim().isEmpty()) {

            return;
        }

        try {

            int newQuantity =
                    Integer.parseInt(
                            quantityText.trim()
                    );

            if (newQuantity < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "Quantity cannot be negative.",
                        "Invalid Quantity",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            boolean success =
                    inventory.updateBatchQuantity(
                            batchId,
                            newQuantity
                    );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Quantity updated successfully.\n\n"
                                + "Batch ID: "
                                + batchId
                                + "\nNew Quantity: "
                                + newQuantity,
                        "Update Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to update quantity.",
                        "Update Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid integer.",
                    "Invalid Quantity",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    private void showDeleteBatchDialog() {

        String batchId =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Batch ID to delete:"
                );

        if (
                batchId == null
                        || batchId.trim().isEmpty()) {

            return;
        }

        batchId =
                batchId.trim();

        Batch batch =
                inventory.searchBatch(batchId);

        if (batch == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Batch not found.",
                    "Delete Batch",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        int confirmation =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete this batch?\n\n"
                                + batch,
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                confirmation
                        != JOptionPane.YES_OPTION) {

            return;
        }

        Batch deleted =
                inventory.deleteBatch(
                        batchId
                );

        if (deleted != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Batch deleted successfully.",
                    "Delete Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to delete batch.",
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // FEFO DISPATCH
    // =========================================================

    private void dispatchNextBatch() {

        if (inventory.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Inventory is empty.",
                    "FEFO Dispatch",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        Batch next =
                inventory.getNextBatch();

        if (next == null) {

            return;
        }

        int confirmation =
                JOptionPane.showConfirmDialog(
                        this,
                        "FEFO selected the following batch:\n\n"
                                + next
                                + "\n\n"
                                + "Dispatch this batch?",
                        "FEFO Dispatch",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                confirmation
                        != JOptionPane.YES_OPTION) {

            return;
        }

        Batch dispatched =
                inventory.dispatchNextBatch();

        if (dispatched != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Batch dispatched successfully:\n\n"
                            + dispatched
                            + "\n\n"
                            + "FEFO rule applied.",
                    "FEFO Dispatch",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to dispatch batch.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================================================
    // REFRESH TABLE
    // =========================================================

    private void refreshInventoryTable(
            DefaultTableModel tableModel) {

        tableModel.setRowCount(0);

        populateInventoryTable(
                tableModel
        );
    }

    // =========================================================
    // POPULATE TABLE
    // =========================================================

    private void populateInventoryTable(
            DefaultTableModel tableModel) {

        for (Batch batch :
                inventory.getAllBatches()) {

            tableModel.addRow(
                    new Object[]{
                            batch.getBatchId(),
                            batch.getProductName(),
                            batch.getQuantity(),
                            batch.getExpiryDate(),
                            batch.getZoneName()
                    }
            );
        }
    }

    // =========================================================
    // LIVE MONITORING
    // =========================================================

    private void startMonitoring() {

        Timer timer =
                new Timer(
                        1000,
                        e -> updateSystem()
                );

        timer.start();
    }

    private void updateSystem() {

        simulationCycle++;

        freezer.runCycle();
        chiller.runCycle();
        ambient.runCycle();

        updateZoneDisplay(
                freezer,
                freezerCurrent,
                freezerStatus
        );

        updateZoneDisplay(
                chiller,
                chillerCurrent,
                chillerStatus
        );

        updateZoneDisplay(
                ambient,
                ambientCurrent,
                ambientStatus
        );

        updateCOADisplay();

        graphPanel.repaint();
    }

    // =========================================================
    // ZONE DISPLAY
    // =========================================================

    private void updateZoneDisplay(
            StorageZone zone,
            JLabel currentLabel,
            JLabel statusLabel) {

        if (
                zone.getTelemetryBuffer()
                        .size() == 0) {

            return;
        }

        TemperatureReading reading =
                (TemperatureReading)
                        zone.getTelemetryBuffer()
                                .get(
                                        zone.getTelemetryBuffer()
                                                .size() - 1
                                );

        double temperature =
                reading.getTemperature();

        currentLabel.setText(
                String.format(
                        "Current: %.2f °C",
                        temperature
                )
        );

        double error =
                Math.abs(
                        temperature
                                - zone.getTargetTemperature()
                );

        if (error <= 2.0) {

            statusLabel.setText("STATUS: NORMAL");
            statusLabel.setForeground(new Color(73, 225, 157));

        } else if (error <= 5.0) {

            statusLabel.setText("STATUS: WARNING");
            statusLabel.setForeground(new Color(255, 184, 77));

        } else {

            statusLabel.setText("STATUS: CRITICAL");
            statusLabel.setForeground(new Color(255, 94, 112));
        }
    }

    // =========================================================
    // COA DISPLAY
    // =========================================================

    private void updateCOADisplay() {

        RegisterBank freezerReg =
                freezer.getRegisters();

        RegisterBank chillerReg =
                chiller.getRegisters();

        RegisterBank ambientReg =
                ambient.getRegisters();

        freezerCOA.setText(
                formatCOA(
                        "FREEZER",
                        freezerReg
                )
        );

        chillerCOA.setText(
                formatCOA(
                        "CHILLER",
                        chillerReg
                )
        );

        ambientCOA.setText(
                formatCOA(
                        "AMBIENT",
                        ambientReg
                )
        );
    }

    private String formatCOA(
            String zone,
            RegisterBank reg) {

        return String.format(
                "%s | R0: %.2f | R1: %.2f | R2: %.2f | R3: %d | R4: %.2f kWh | Compressor: %s",
                zone,
                reg.getR0(),
                reg.getR1(),
                reg.getR2(),
                reg.getR3(),
                reg.getR4(),
                reg.isCompressorOn()
                        ? "ON"
                        : "OFF"
        );
    }

    // =========================================================
    // TEMPERATURE GRAPH
    // =========================================================

    private class TemperatureGraphPanel
            extends JPanel {

        @Override
        protected void paintComponent(
                Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width =
                    getWidth();

            int height =
                    getHeight();

            // Extra space on left keeps
            // axis title and values tidy.
            int left = 100;
            int right = 30;
            int top = 68;
            int bottom = 70;

            int graphWidth =
                    width - left - right;

            int graphHeight =
                    height - top - bottom;

            // Scale the chart to the actual temperatures currently in the buffers.
            double minTemp = Double.POSITIVE_INFINITY;
            double maxTemp = Double.NEGATIVE_INFINITY;

            CircularBuffer[] temperatureBuffers = {
                    freezer.getTelemetryBuffer(),
                    chiller.getTelemetryBuffer(),
                    ambient.getTelemetryBuffer()
            };

            for (CircularBuffer buffer : temperatureBuffers) {
                if (buffer == null) {
                    continue;
                }
                for (int i = 0; i < buffer.size(); i++) {
                    TemperatureReading reading =
                            (TemperatureReading) buffer.get(i);
                    double value = reading.getTemperature();
                    minTemp = Math.min(minTemp, value);
                    maxTemp = Math.max(maxTemp, value);
                }
            }

            if (!Double.isFinite(minTemp) || !Double.isFinite(maxTemp)) {
                minTemp = -25.0;
                maxTemp = 30.0;
            } else {
                double span = maxTemp - minTemp;
                double margin = Math.max(2.0, span * 0.10);
                minTemp -= margin;
                maxTemp += margin;
                if (maxTemp - minTemp < 10.0) {
                    double extra = (10.0 - (maxTemp - minTemp)) / 2.0;
                    minTemp -= extra;
                    maxTemp += extra;
                }
            }

            // =================================================
            // GRAPH TITLE
            // =================================================

            g2.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            18
                    )
            );

            g2.setColor(new Color(31, 41, 55));
            g2.drawString("Temperature Monitoring", left, 43);

            // =================================================
            // GRID AND Y VALUES
            // =================================================

            g2.setFont(
                    new Font(
                            "Arial",
                            Font.PLAIN,
                            11
                    )
            );

            g2.setColor(new Color(107, 114, 128));
            int gridSteps = 6;
            for (int step = 0; step <= gridSteps; step++) {
                double temperature = maxTemp
                        - (step * (maxTemp - minTemp) / gridSteps);
                int y = top + (int) (step * graphHeight / (double) gridSteps);

                g2.setColor(new Color(229, 231, 235));
                g2.drawLine(left, y, width - right, y);

                g2.setColor(new Color(107, 114, 128));
                g2.drawString(String.format("%.1f °C", temperature), 25, y + 5);
            }

            // =================================================
            // AXES
            // =================================================

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(107, 114, 128));

            g2.drawLine(
                    left,
                    top,
                    left,
                    height - bottom
            );

            g2.drawLine(
                    left,
                    height - bottom,
                    width - right,
                    height - bottom
            );

            // =================================================
            // Y AXIS TITLE
            // =================================================

            g2.rotate(
                    -Math.PI / 2
            );

            g2.drawString(
                    "Temperature (°C)",
                    -(height / 2 + 60),
                    20
            );

            g2.rotate(
                    Math.PI / 2
            );

            // =================================================
            // X AXIS TITLE
            // =================================================

            g2.drawString(
                    "Simulation Cycle",
                    left
                            + graphWidth / 2
                            - 50,
                    height - 20
            );

            // =================================================
            // X AXIS VALUES
            // =================================================

            int bufferSize =
                    Math.min(
                            20,
                            Math.max(
                                    freezer.getTelemetryBuffer().size(),
                                    Math.max(
                                            chiller.getTelemetryBuffer().size(),
                                            ambient.getTelemetryBuffer().size()
                                    )
                            )
                    );

            if (bufferSize > 1) {

                for (
                        int i = 0;
                        i < bufferSize;
                        i++) {

                    int x =
                            left
                                    + (i * graphWidth)
                                    / (bufferSize - 1);

                    int cycleNumber =
                            simulationCycle
                                    - bufferSize
                                    + 1
                                    + i;

                    g2.drawLine(
                            x,
                            height - bottom,
                            x,
                            height - bottom + 5
                    );

                    if (
                            i % 5 == 0
                                    ||
                            i == bufferSize - 1) {

                        g2.drawString(
                                String.valueOf(
                                        cycleNumber
                                ),
                                x - 7,
                                height - bottom + 20
                        );
                    }
                }
            }

            // =================================================
            // DRAW TEMPERATURE LINES
            // =================================================

            drawTemperatureLine(
                    g2,
                    freezer.getTelemetryBuffer(),
                    left,
                    top,
                    graphWidth,
                    graphHeight,
                    minTemp,
                    maxTemp
            );

            drawTemperatureLine(
                    g2,
                    chiller.getTelemetryBuffer(),
                    left,
                    top,
                    graphWidth,
                    graphHeight,
                    minTemp,
                    maxTemp
            );

            drawTemperatureLine(
                    g2,
                    ambient.getTelemetryBuffer(),
                    left,
                    top,
                    graphWidth,
                    graphHeight,
                    minTemp,
                    maxTemp
            );

            // =================================================
            // LEGEND
            // =================================================

            int legendY =
                    height - 45;

            g2.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            12
                    )
            );

            // Freezer
            g2.setColor(
                    Color.BLUE
            );

            g2.drawLine(
                    left,
                    legendY - 4,
                    left + 25,
                    legendY - 4
            );

            g2.setColor(
                    Color.BLACK
            );

            g2.drawString(
                    "Freezer",
                    left + 30,
                    legendY
            );

            // Chiller
            int chillerX =
                    left + 110;

            g2.setColor(
                    Color.GREEN.darker()
            );

            g2.drawLine(
                    chillerX,
                    legendY - 4,
                    chillerX + 25,
                    legendY - 4
            );

            g2.setColor(
                    Color.BLACK
            );

            g2.drawString(
                    "Chiller",
                    chillerX + 30,
                    legendY
            );

            // Ambient
            int ambientX =
                    left + 210;

            g2.setColor(
                    Color.RED
            );

            g2.drawLine(
                    ambientX,
                    legendY - 4,
                    ambientX + 25,
                    legendY - 4
            );

            g2.setColor(
                    Color.BLACK
            );

            g2.drawString(
                    "Ambient",
                    ambientX + 30,
                    legendY
            );

            g2.setColor(new Color(107, 114, 128));
            g2.dispose();
        }

        // =====================================================
        // DRAW ONE TEMPERATURE LINE
        // =====================================================

        private void drawTemperatureLine(
                Graphics2D g2,
                CircularBuffer buffer,
                int left,
                int top,
                int graphWidth,
                int graphHeight,
                double minTemp,
                double maxTemp) {

            if (
                    buffer == null
                            ||
                    buffer.size() < 2) {

                return;
            }

            int size =
                    buffer.size();

            if (
                    buffer ==
                            freezer.getTelemetryBuffer()) {

                g2.setColor(
                        Color.BLUE
                );

            } else if (
                    buffer ==
                            chiller.getTelemetryBuffer()) {

                g2.setColor(
                        Color.GREEN.darker()
                );

            } else {

                g2.setColor(
                        Color.RED
                );
            }

            g2.setStroke(
                    new BasicStroke(2.5f)
            );

            for (
                    int i = 1;
                    i < size;
                    i++) {

                TemperatureReading r1 =
                        (TemperatureReading)
                                buffer.get(i - 1);

                TemperatureReading r2 =
                        (TemperatureReading)
                                buffer.get(i);

                double temp1 =
                        r1.getTemperature();

                double temp2 =
                        r2.getTemperature();

                int x1 =
                        left
                                + ((i - 1) * graphWidth)
                                / (size - 1);

                int x2 =
                        left
                                + (i * graphWidth)
                                / (size - 1);

                int y1 =
                        top
                                + (int) (
                                (maxTemp - temp1)
                                        / (maxTemp - minTemp)
                                        * graphHeight
                        );

                int y2 =
                        top
                                + (int) (
                                (maxTemp - temp2)
                                        / (maxTemp - minTemp)
                                        * graphHeight
                        );

                g2.drawLine(
                        x1,
                        y1,
                        x2,
                        y2
                );

                g2.fillOval(
                        x2 - 3,
                        y2 - 3,
                        6,
                        6
                );
            }

            g2.setColor(
                    Color.BLACK
            );
        }
    }
}