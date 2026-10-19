package coldflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CppBridge {

    private static final String INPUT_FILE =
            "cpp/input.txt";

    private static final String OUTPUT_FILE =
            "cpp/output.txt";

    // C++ executable name because we will run it from the cpp folder
    // Use platform-appropriate name (Windows vs Unix)
    private static String getCppExecutableName() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "ColdFlowEngine.exe" : "ColdFlowEngine";
    }

    // Send Java data to C++
    public static void sendTemperature(
            double currentTemperature,
            double targetTemperature) {

        try (FileWriter writer =
                     new FileWriter(INPUT_FILE)) {

            writer.write(currentTemperature + "\n");
            writer.write(targetTemperature + "\n");

        } catch (IOException e) {

            System.out.println(
                    "Error writing input.txt: "
                            + e.getMessage()
            );
        }
    }

    // Start the C++ engine
    public static boolean runCppEngine() {

        try {

            File cppFolder =
                    new File("cpp").getAbsoluteFile();

            File cppExecutable =
                    new File(
                            cppFolder,
                            getCppExecutableName()
                    );

            System.out.println(
                    "C++ executable: "
                            + cppExecutable.getAbsolutePath()
            );

            if (!cppExecutable.exists()) {
                System.out.println("C++ executable not found: " + cppExecutable.getAbsolutePath());
                return false;
            }

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            cppExecutable.getAbsolutePath()
                    );

            processBuilder.directory(cppFolder);

            processBuilder.inheritIO();

            Process process =
                    processBuilder.start();

            int exitCode =
                    process.waitFor();

            return exitCode == 0;

        } catch (IOException e) {

            System.out.println(
                    "Error running C++ engine: "
                            + e.getMessage()
            );

            return false;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    "C++ process was interrupted."
            );

            return false;
        }
    }
    // Read the result produced by C++
    public static String[] readCppResult() {

        String[] result = {
                "0.00",
                "OFF"
        };

        File file =
                new File(OUTPUT_FILE);

        if (!file.exists()) {

            System.out.println(
                    "output.txt not found."
            );

            return result;
        }

        try (Scanner scanner =
                     new Scanner(file)) {

            if (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    result[0] = line;
                }
            }

            if (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    result[1] = line;
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Error reading output.txt: "
                            + e.getMessage()
            );
        }

        return result;
    }

    // Test Java → C++ → Java
    public static void main(String[] args) {

        System.out.println(
                "Testing Java -> C++ integration..."
        );

        // Send test temperatures
        sendTemperature(
                -18.5,
                -20.0
        );

        System.out.println(
                "Data sent to C++."
        );

        // Start C++ engine
        boolean success =
                runCppEngine();

        if (!success) {

            System.out.println(
                    "C++ engine failed to run."
            );

            return;
        }

        System.out.println(
                "C++ engine completed."
        );

        // Read C++ result
        String[] result =
                readCppResult();

        System.out.println(
                "C++ Error: " + result[0]
        );

        System.out.println(
                "C++ Cooling: " + result[1]
        );
    }
}
