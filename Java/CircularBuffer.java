package coldflow;

public class CircularBuffer {

    private final TemperatureReading[] buffer;
    private int writeIndex;
    private int size;

    public CircularBuffer(int capacity) {
        buffer = new TemperatureReading[capacity];
        writeIndex = 0;
        size = 0;
    }

    // Add a new reading to the buffer
    public void add(TemperatureReading reading) {

        buffer[writeIndex] = reading;

        writeIndex = (writeIndex + 1) % buffer.length;

        if (size < buffer.length) {
            size++;
        }
    }

    // Get the number of readings currently stored
    public int size() {
        return size;
    }

    // Get the buffer capacity
    public int capacity() {
        return buffer.length;
    }

    // Get a reading by its logical position
    public TemperatureReading get(int index) {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "Invalid buffer index: " + index
            );
        }

        int startIndex = (writeIndex - size + buffer.length)
                        % buffer.length;

        int actualIndex = (startIndex + index)
                        % buffer.length;

        return buffer[actualIndex];
    }

    // Display all stored readings
    public void display() {

        System.out.println("\n--- Telemetry Buffer ---");

        for (int i = 0; i < size; i++) {
            System.out.println(
                (i + 1) + ". " + get(i)
            );
        }

        System.out.println("------------------------");
    }
}