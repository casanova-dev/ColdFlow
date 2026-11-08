package coldflow;

import coldflow.database.TemperatureRepository;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class StorageZone {

    protected final String zoneName;
    protected final double targetTemperature;
    protected final RegisterBank registers;
    protected final CoolingALU coolingALU;
    protected final CircularBuffer telemetryBuffer;

    // One shared background worker keeps JDBC operations off the Swing UI thread
    // and preserves the order in which zone readings are submitted.
    private static final ExecutorService DATABASE_WRITER =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "ColdFlow-Temperature-DB");
                thread.setDaemon(true);
                return thread;
            });

    private static final TemperatureRepository TEMPERATURE_REPOSITORY =
            new TemperatureRepository();

    public StorageZone(String zoneName, double targetTemperature) {

        this.zoneName = zoneName;
        this.targetTemperature = targetTemperature;

        // COA register bank
        registers = new RegisterBank(targetTemperature);

        // Cooling controller
        coolingALU = new CoolingALU(registers);

        // Store latest 20 sensor readings
        telemetryBuffer = new CircularBuffer(20);
    }

    // Run one temperature-control cycle
    public void runCycle() {

        // Run the normal Java cooling simulation
        coolingALU.executeCycle();

        double temperature = registers.getR1();

        // Send Java temperature data to C++
        CppBridge.sendTemperature(temperature, targetTemperature);

        // Run the C++ processing engine
        boolean cppSuccess = CppBridge.runCppEngine();

        String cppError = "0.00";
        String cppCooling = "OFF";

        if (cppSuccess) {
            String[] result = CppBridge.readCppResult();
            cppError = result[0];
            cppCooling = result[1];
        }

        // Simulated humidity
        double humidity = 60.0 + Math.random() * 15.0;

        TemperatureReading reading =
                new TemperatureReading(temperature, humidity);

        telemetryBuffer.add(reading);

        // Persist each reading asynchronously so database latency does not
        // block the Swing dashboard's monitoring timer.
        saveReadingToDatabase(reading);

        System.out.println("\n[" + zoneName + "]");

        registers.displayRegisters();

        System.out.println(
                "Java Compressor: " +
                (registers.isCompressorOn() ? "ON" : "OFF")
        );

        System.out.println("C++ Temperature Error: " + cppError + "°C");
        System.out.println("C++ Cooling Decision: " + cppCooling);
        System.out.println("Latest Reading: " + reading);

        System.out.println(
                "Telemetry: " + telemetryBuffer.size() + "/" +
                telemetryBuffer.capacity()
        );
    }

    private void saveReadingToDatabase(TemperatureReading reading) {
        try {
            DATABASE_WRITER.submit(() -> {
                try {
                    TEMPERATURE_REPOSITORY.saveReading(zoneName, reading);
                } catch (SQLException ex) {
                    System.err.println(
                            "[Supabase] Failed to save temperature for " +
                            zoneName + ": " + ex.getMessage()
                    );
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ex) {
            System.err.println(
                    "[Supabase] Temperature save was rejected for " + zoneName +
                    ": " + ex.getMessage()
            );
        }
    }

    // Each storage zone must define its own temperature safety condition.
    public abstract boolean isTemperatureSafe();

    public String getZoneName() {
        return zoneName;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public RegisterBank getRegisters() {
        return registers;
    }

    public CircularBuffer getTelemetryBuffer() {
        return telemetryBuffer;
    }
}
