package coldflow;

public class RegisterBank {

    // COA simulated registers
    private double R0; // Target Temperature
    private double R1; // Current Temperature
    private double R2; // Temperature Error
    private int R3;    // Control Status
    private double R4; // Energy Consumption

    public RegisterBank(double targetTemperature) {
        R0 = targetTemperature;
        R1 = targetTemperature;
        R2 = 0;
        R3 = 0;
        R4 = 0;
    }

    public double getR0() {
        return R0;
    }

    public void setR0(double value) {
        R0 = value;
    }

    public double getR1() {
        return R1;
    }

    public void setR1(double value) {
        R1 = value;
    }

    public double getR2() {
        return R2;
    }

    public void setR2(double value) {
        R2 = value;
    }

    public int getR3() {
        return R3;
    }

    public void setR3(int value) {
        R3 = value;
    }

    public double getR4() {
        return R4;
    }

    public void addEnergy(double energy) {
        R4 += energy;
    }

    // Bit 0 controls the compressor
    public boolean isCompressorOn() {
        return (R3 & 1) != 0;
    }

    public void setCompressor(boolean on) {
        if (on) {
            R3 |= 1;
        } else {
            R3 &= ~1;
        }
    }

    public void displayRegisters() {
        System.out.printf(
            "R0 Target: %.2f°C | " +
            "R1 Current: %.2f°C | " +
            "R2 Error: %.2f°C | " +
            "R3 Status: %d | " +
            "R4 Energy: %.2f kWh%n",
            R0, R1, R2, R3, R4
        );
    }
}