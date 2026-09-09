package coldflow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

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

        setTitle("COLDFlow - Smart Cold-Chain Warehouse");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildDashboard();
        startMonitoring();
    }

    // =========================================================
    // BUILD DASHBOARD
    // =========================================================

    private void buildDashboard() {

        setLayout(new BorderLayout(10, 10));

        // =====================================================
        // HEADER
        // =====================================================

        JPanel headerPanel =
                new JPanel(new BorderLayout());

        JLabel title =
                new JLabel(
                        "COLDFlow - Smart Cold-Chain Warehouse",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font("Arial", Font.BOLD, 26)
        );

        headerPanel.add(
                title,
                BorderLayout.CENTER
        );

        JButton inventoryButton =
                new JButton("Inventory");

        inventoryButton.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        inventoryButton.addActionListener(
                e -> showInventoryWindow()
        );

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                15,
                                8
                        )
                );

        buttonPanel.add(inventoryButton);

        headerPanel.add(
                buttonPanel,
                BorderLayout.EAST
        );

        add(
                headerPanel,
                BorderLayout.NORTH
        );

        // =====================================================
        // MAIN PANEL
        // =====================================================

        JPanel mainPanel =
                new JPanel(
                        new BorderLayout(10, 10)
                );

        // =====================================================
        // ZONE CARDS
        // =====================================================

        JPanel zonePanel =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                10,
                                10
                        )
                );

        zonePanel.add(
                createZoneCard(
                        freezer.getZoneName(),
                        freezer.getTargetTemperature(),
                        "Freezer"
                )
        );

        zonePanel.add(
                createZoneCard(
                        chiller.getZoneName(),
                        chiller.getTargetTemperature(),
                        "Chiller"
                )
        );

        zonePanel.add(
                createZoneCard(
                        ambient.getZoneName(),
                        ambient.getTargetTemperature(),
                        "Ambient"
                )
        );

        mainPanel.add(
                zonePanel,
                BorderLayout.NORTH
        );

        // =====================================================
        // GRAPH
        // =====================================================

        graphPanel =
                new TemperatureGraphPanel();

        graphPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Temperature Monitoring - CG Visualization"
                )
        );

        mainPanel.add(
                graphPanel,
                BorderLayout.CENTER
        );

        // =====================================================
        // BOTTOM PANEL
        // =====================================================

        JPanel bottomPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                10,
                                10
                        )
                );

        bottomPanel.add(createCOAPanel());
        bottomPanel.add(createInventorySummaryPanel());

        mainPanel.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        add(
                mainPanel,
                BorderLayout.CENTER
        );
    }

    // =========================================================
    // ZONE CARD
    // =========================================================

    private JPanel createZoneCard(
            String zoneName,
            double targetTemperature,
            String zoneType) {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        zoneName
                )
        );

        JLabel target =
                new JLabel(
                        String.format(
                                "Target: %.1f °C",
                                targetTemperature
                        ),
                        SwingConstants.CENTER
                );

        target.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        JLabel current =
                new JLabel(
                        "Current: -- °C",
                        SwingConstants.CENTER
                );

        current.setFont(
                new Font("Arial", Font.BOLD, 21)
        );

        JLabel status =
                new JLabel(
                        "STATUS: STARTING",
                        SwingConstants.CENTER
                );

        status.setFont(
                new Font("Arial", Font.BOLD, 14)
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

        JPanel information =
                new JPanel(
                        new GridLayout(3, 1)
                );

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

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "COA Control Registers"
                )
        );

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

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Inventory Management"
                )
        );

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
                1000,
                620
        );

        inventoryFrame.setLocationRelativeTo(this);

        inventoryFrame.setLayout(
                new BorderLayout(10, 10)
        );

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

            statusLabel.setText(
                    "STATUS: NORMAL"
            );

        } else if (error <= 5.0) {

            statusLabel.setText(
                    "STATUS: WARNING"
            );

        } else {

            statusLabel.setText(
                    "STATUS: CRITICAL"
            );
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

            Graphics2D g2 =
                    (Graphics2D) g;

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
            int top = 50;
            int bottom = 70;

            int graphWidth =
                    width - left - right;

            int graphHeight =
                    height - top - bottom;

            double minTemp =
                    -25.0;

            double maxTemp =
                    30.0;

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

            g2.drawString(
                    "Live Temperature Monitoring",
                    left,
                    25
            );

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

            for (
                    int temperature = -25;
                    temperature <= 30;
                    temperature += 5) {

                int y =
                        top
                                + (int) (
                                (maxTemp - temperature)
                                        / (maxTemp - minTemp)
                                        * graphHeight
                        );

                g2.drawLine(
                        left,
                        y,
                        width - right,
                        y
                );

                g2.drawString(
                        temperature + " °C",
                        25,
                        y + 5
                );
            }

            // =================================================
            // AXES
            // =================================================

            g2.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            12
                    )
            );

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

            g2.setColor(
                    Color.BLACK
            );
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