package coldflow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.*;
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

        configureDarkLookAndFeel();

        setTitle("COLDFlow | Smart Cold-Chain Command Center");
        setSize(1280, 900);
        setMinimumSize(new Dimension(1050, 760));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(10, 16, 30));

        buildDashboard();
        startMonitoring();
    }

    private static final Color BG = new Color(10, 16, 30);
    private static final Color PANEL_BG = new Color(15, 24, 43);
    private static final Color CARD_BG = new Color(18, 29, 51);
    private static final Color TEXT = new Color(225, 237, 250);
    private static final Color MUTED = new Color(164, 184, 211);
    private static final Color CYAN = new Color(105, 210, 255);

    private void configureDarkLookAndFeel() {
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("Panel.background", PANEL_BG);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.background", new Color(25, 48, 76));
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", new Color(10, 18, 32));
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextArea.background", new Color(10, 18, 32));
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("ComboBox.background", new Color(25, 48, 76));
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("Table.background", CARD_BG);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", new Color(0, 105, 160));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.gridColor", new Color(42, 67, 99));
        UIManager.put("TableHeader.background", new Color(22, 42, 67));
        UIManager.put("TableHeader.foreground", CYAN);
    }

    private void applyDarkTheme(Component component) {
        if (component instanceof JFrame) {
            component.setBackground(BG);
        } else if (component instanceof JPanel) {
            component.setBackground(PANEL_BG);
        } else if (component instanceof JLabel) {
            component.setForeground(TEXT);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setBackground(new Color(25, 48, 76));
            button.setForeground(TEXT);
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(48, 87, 125)),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        } else if (component instanceof JTextArea) {
            component.setBackground(new Color(10, 18, 32));
            component.setForeground(TEXT);
            ((JTextArea) component).setCaretColor(CYAN);
        } else if (component instanceof JTextField) {
            component.setBackground(new Color(10, 18, 32));
            component.setForeground(TEXT);
            ((JTextField) component).setCaretColor(CYAN);
        } else if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setBackground(CARD_BG);
            table.setForeground(TEXT);
            table.setGridColor(new Color(42, 67, 99));
            table.setSelectionBackground(new Color(0, 105, 160));
            table.setSelectionForeground(Color.WHITE);
            table.setFillsViewportHeight(true);
            table.setRowHeight(30);
            table.setShowVerticalLines(false);
            table.setIntercellSpacing(new Dimension(0, 1));
            table.getTableHeader().setBackground(new Color(22, 42, 67));
            table.getTableHeader().setForeground(CYAN);
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        } else if (component instanceof JScrollPane) {
            component.setBackground(PANEL_BG);
            ((JScrollPane) component).getViewport().setBackground(CARD_BG);
            ((JScrollPane) component).setBorder(
                    BorderFactory.createLineBorder(new Color(42, 67, 99)));
        } else if (component instanceof JViewport) {
            component.setBackground(CARD_BG);
        } else if (component instanceof JComboBox) {
            component.setBackground(new Color(25, 48, 76));
            component.setForeground(TEXT);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyDarkTheme(child);
            }
        }
    }

    // =========================================================
    // BUILD DASHBOARD
    // =========================================================

    private void buildDashboard() {

        setLayout(new BorderLayout(14, 14));

        // =====================================================
        // HEADER
        // =====================================================

        JPanel headerPanel =
                new JPanel(new BorderLayout(12, 8));
        headerPanel.setBackground(new Color(15, 24, 43));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 68, 105)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel title =
                new JLabel(
                        "COLDFlow  /  COLD-CHAIN COMMAND CENTER",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font("SansSerif", Font.BOLD, 23)
        );

        title.setForeground(new Color(105, 210, 255));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        headerPanel.add(
                title,
                BorderLayout.CENTER
        );

        JButton inventoryButton =
                new JButton("Inventory");

        inventoryButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        inventoryButton.setBackground(new Color(0, 151, 210));
        inventoryButton.setForeground(Color.WHITE);
        inventoryButton.setFocusPainted(false);
        inventoryButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(66, 196, 255), 1),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)));

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
                new JPanel(new BorderLayout(14, 14));
        mainPanel.setBackground(new Color(10, 16, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 14, 14, 14));

        // =====================================================
        // ZONE CARDS
        // =====================================================

        JPanel zonePanel =
                new JPanel(
                        new GridLayout(1, 3, 14, 14)
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

        graphPanel.setBackground(new Color(15, 24, 43));
        graphPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(35, 68, 105)),
                " LIVE TEMPERATURE TELEMETRY  /  CG VISUALIZATION ",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13),
                new Color(105, 210, 255)));

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

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(18, 29, 51));
        Color zoneAccent = zoneType.equals("Freezer")
                ? new Color(64, 170, 255)
                : zoneType.equals("Chiller")
                    ? new Color(53, 220, 190)
                    : new Color(255, 174, 74);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(42, 67, 99), 1),
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

        target.setForeground(new Color(164, 184, 211));
        current.setForeground(new Color(235, 246, 255));
        status.setForeground(new Color(91, 222, 168));
        JPanel information = new JPanel(new GridLayout(3, 1, 4, 4));
        information.setBackground(new Color(18, 29, 51));

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

        panel.setBackground(new Color(18, 29, 51));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(42, 67, 99)),
                " COA CONTROL REGISTERS ", TitledBorder.LEADING,
                TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12),
                new Color(105, 210, 255)));

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

        panel.setBackground(new Color(18, 29, 51));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(42, 67, 99)),
                " INVENTORY SNAPSHOT ", TitledBorder.LEADING,
                TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12),
                new Color(105, 210, 255)));

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
            g2.setColor(new Color(15, 24, 43));
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

            g2.setColor(new Color(105, 210, 255));
            g2.drawString("LIVE TEMPERATURE MONITORING", left, 43);

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

            g2.setColor(new Color(151, 174, 203));
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

                g2.setColor(new Color(38, 58, 83));
                g2.drawLine(
                        left,
                        y,
                        width - right,
                        y
                );

                g2.setColor(new Color(164, 184, 211));
                g2.drawString(
                        temperature + " °C",
                        25,
                        y + 5
                );
            }

            // =================================================
            // AXES
            // =================================================

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(164, 184, 211));

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

            g2.setColor(new Color(164, 184, 211));
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