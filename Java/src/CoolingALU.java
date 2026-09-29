package coldflow;

public class CoolingALU {

    private final RegisterBank registers;

    // Temperature deadband
    private final double deadband = 1.5;

    // Simulated cooling and warming rates
    private final double coolingRate = 0.8;
    private final double warmingRate = 0.25;

    public CoolingALU(RegisterBank registers) {
        this.registers = registers;
    }

    public void executeCycle() {

        double target = registers.getR0();
        double current = registers.getR1();

        // ALU calculates temperature error
        double error = current - target;

        registers.setR2(error);

        // Compressor control
        if (error > deadband) {
            registers.setCompressor(true);
        }
        else if (error <= 0) {
            registers.setCompressor(false);
        }

        // Simulate temperature change
        if (registers.isCompressorOn()) {

            current -= coolingRate;

            // Simulated energy consumption
            registers.addEnergy(0.05);

        } else {

            current += warmingRate;
        }

        registers.setR1(current);
    }
}