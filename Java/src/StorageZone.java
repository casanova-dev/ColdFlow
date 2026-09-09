package coldflow;

public class StorageZone {

    protected final String zoneName;
    protected final double targetTemperature;
    protected final RegisterBank registers;
    protected final CoolingALU coolingALU;
    protected final CircularBuffer telemetryBuffer;

    public StorageZone(
            String zoneName,
            double targetTemperature) {

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
    CppBridge.sendTemperature(
            temperature,
            targetTemperature
    );

    // Run the C++ processing engine
    boolean cppSuccess =
            CppBridge.runCppEngine();

    String cppError = "0.00";
    String cppCooling = "OFF";

    if (cppSuccess) {

        String[] result =
                CppBridge.readCppResult();

        cppError = result[0];
        cppCooling = result[1];
    }

    // Simulated humidity
    double humidity =
            60.0 + Math.random() * 15.0;

    TemperatureReading reading =
            new TemperatureReading(
                    temperature,
                    humidity
            );

    telemetryBuffer.add(reading);

    System.out.println("\n[" + zoneName + "]");

    registers.displayRegisters();

    System.out.println(
            "Java Compressor: " +
            (registers.isCompressorOn()
                    ? "ON"
                    : "OFF")
    );

    System.out.println(
            "C++ Temperature Error: "
                    + cppError + "°C"
    );

    System.out.println(
            "C++ Cooling Decision: "
                    + cppCooling
    );

    System.out.println(
            "Latest Reading: " +
            reading
    );

    System.out.println(
            "Telemetry: " +
            telemetryBuffer.size() +
            "/" +
            telemetryBuffer.capacity()
    );
}

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