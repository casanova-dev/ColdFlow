#include <iostream>
#include <iomanip>
#include <fstream>
#include <string>
#include <cstdlib>
#include <cmath>


using namespace std;

// ===================================================================
// COLDFlow - C++ Temperature Processing Engine
// ===================================================================
//
// Demonstrates:
// - C++ classes
// - Fixed-size arrays
// - Array traversal
// - Temperature error calculation
// - Temperature trend detection
// - Cooling control logic
// - Hysteresis
// - Java <-> C++ file I/O integration
//
// C++ CO1:
// Develop applications using arrays.
//
// ===================================================================


// ================================================================
// SENSOR ARRAY CONFIGURATION
// ================================================================

const int MAX_SENSORS = 10;


// ================================================================
// SENSOR READING STRUCTURE
// ================================================================

struct SensorReading {

    double temperature;
    double humidity;
    int status;
};


// ================================================================
// ARRAY-BASED SENSOR BUFFER
// ================================================================

SensorReading sensorArray[MAX_SENSORS];

int sensorIndex = 0;
int sensorCount = 0;


// ================================================================
// ADD SENSOR READING
// ================================================================
// Uses direct array indexing.
// Circular wraparound keeps the array size fixed.
// ================================================================

void addSensorReading(
        double temperature,
        double humidity,
        int status) {

    sensorArray[sensorIndex].temperature = temperature;
    sensorArray[sensorIndex].humidity = humidity;
    sensorArray[sensorIndex].status = status;

    sensorIndex =
            (sensorIndex + 1) % MAX_SENSORS;

    if (sensorCount < MAX_SENSORS) {
        sensorCount++;
    }
}


// ================================================================
// CALCULATE AVERAGE TEMPERATURE
// ================================================================
// Array traversal.
// Time Complexity: O(n)
// ================================================================

double getAverageTemperature() {

    if (sensorCount == 0) {
        return 0.0;
    }

    double sum = 0.0;

    for (int i = 0;
         i < sensorCount;
         i++) {

        sum += sensorArray[i].temperature;
    }

    return sum / sensorCount;
}


// ================================================================
// DETECT TEMPERATURE TREND
// ================================================================
// Returns:
//  1  -> Rising
// -1  -> Falling
//  0  -> Stable
// ================================================================

int detectTrend() {

    if (sensorCount < 2) {
        return 0;
    }

    int oldestIndex =
            (sensorIndex -
             sensorCount +
             MAX_SENSORS)
            % MAX_SENSORS;

    int newestIndex =
            (sensorIndex - 1 +
             MAX_SENSORS)
            % MAX_SENSORS;

    double oldest =
            sensorArray[oldestIndex].temperature;

    double newest =
            sensorArray[newestIndex].temperature;

    double difference =
            newest - oldest;

    if (difference > 0.5) {
        return 1;
    }

    if (difference < -0.5) {
        return -1;
    }

    return 0;
}


// ================================================================
// COLDFlow ENGINE CLASS
// ================================================================

class ColdFlowEngine {

public:

    // ============================================================
    // CALCULATE TEMPERATURE ERROR
    // ============================================================

    static double calculateError(
            double currentTemperature,
            double targetTemperature) {

        return currentTemperature -
               targetTemperature;
    }


    // ============================================================
    // BASIC COOLING DECISION
    // ============================================================

    static bool shouldCool(
            double currentTemperature,
            double targetTemperature) {

        double error =
                calculateError(
                        currentTemperature,
                        targetTemperature
                );

        return error > 1.5;
    }


    // ============================================================
    // COMPLETE PROCESSING
    // ============================================================

    static void process(
            double currentTemperature,
            double targetTemperature) {

        double error =
                calculateError(
                        currentTemperature,
                        targetTemperature
                );

        bool cooling =
                shouldCool(
                        currentTemperature,
                        targetTemperature
                );

        cout << fixed << setprecision(2);

        cout << "Current Temperature : "
             << currentTemperature
             << " C\n";

        cout << "Target Temperature  : "
             << targetTemperature
             << " C\n";

        cout << "Temperature Error   : "
             << error
             << " C\n";

        cout << "Cooling Required    : "
             << (cooling ? "YES" : "NO")
             << "\n";
    }
};


// ================================================================
// MAIN
// ================================================================

int main() {

    cout << "=====================================\n";
    cout << "       COLDFlow C++ Engine\n";
    cout << "=====================================\n\n";


    // ============================================================
    // READ INPUT FROM JAVA
    // ============================================================

    ifstream inputFile("input.txt");

    if (!inputFile.is_open()) {

        cerr << "Error: Cannot open input.txt"
             << endl;

        return 1;
    }

    double currentTemperature = 0.0;
    double targetTemperature = 0.0;

    inputFile >>
            currentTemperature >>
            targetTemperature;

    inputFile.close();


    cout << "C++ Engine: Read temperatures"
         << endl;

    cout << "  Current: "
         << currentTemperature
         << "°C"
         << endl;

    cout << "  Target:  "
         << targetTemperature
         << "°C"
         << endl;


    // ============================================================
    // ARRAY PROCESSING
    // ============================================================

    int status = 0;

    addSensorReading(
            currentTemperature,
            65.0,
            status
    );


    double averageTemperature =
            getAverageTemperature();


    int trend =
            detectTrend();


    cout << "\nArray Processing:"
         << endl;

    cout << "  Readings stored: "
         << sensorCount
         << "/"
         << MAX_SENSORS
         << endl;

    cout << "  Average temp:    "
         << averageTemperature
         << "°C"
         << endl;

    cout << "  Trend:           ";

    if (trend > 0) {

        cout << "RISING";

    } else if (trend < 0) {

        cout << "FALLING";

    } else {

        cout << "STABLE";
    }

    cout << endl;


    // ============================================================
    // TEMPERATURE ERROR
    // ============================================================

    double temperatureError =
            ColdFlowEngine::calculateError(
                    currentTemperature,
                    targetTemperature
            );


    cout << "\nError Calculation:"
         << endl;

    cout << "  Temperature Error: "
         << temperatureError
         << "°C"
         << endl;


    // ============================================================
    // COOLING CONTROL WITH HYSTERESIS
    // ============================================================

    const double ON_THRESHOLD = 2.0;

    const double OFF_THRESHOLD = 0.5;

    string coolingStatus = "OFF";


    if (temperatureError >
            ON_THRESHOLD) {

        coolingStatus = "ON";

    } else if (
            temperatureError <=
            OFF_THRESHOLD) {

        coolingStatus = "OFF";

    } else {

        coolingStatus = "IDLE";
    }


    // If temperature is already falling,
    // reduce cooling intensity.

    if (trend < 0 &&
        temperatureError > 0) {

        coolingStatus = "IDLE";
    }


    cout << "\nCooling Decision: "
         << coolingStatus
         << endl;


    // ============================================================
    // WRITE RESULT FOR JAVA
    // ============================================================
    //
    // IMPORTANT:
    // Java CppBridge expects:
    //
    // Line 1 -> Temperature Error
    // Line 2 -> Cooling Status
    //
    // ============================================================

    ofstream outputFile("output.txt");

    if (!outputFile.is_open()) {

        cerr << "Error: Cannot create output.txt"
             << endl;

        return 1;
    }


    outputFile
            << fixed
            << setprecision(2);


    outputFile
            << temperatureError
            << endl;


    outputFile
            << coolingStatus
            << endl;


    outputFile.close();


    // ============================================================
    // DISPLAY ENGINE PROCESSING
    // ============================================================

    ColdFlowEngine::process(
            currentTemperature,
            targetTemperature
    );


    cout << "\nC++ Engine: Output written "
         << "to output.txt"
         << endl;

    cout << "C++ Engine: Cycle complete "
         << "(exit code 0)"
         << endl;


    return 0;
}