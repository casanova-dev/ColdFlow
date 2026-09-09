#include <iostream>
#include <fstream>
#include <cstdlib>
#include <cmath>

// ===================================================================
// ColdFlow - C++ Processing Engine with Array-Based Sensor Processing
// ===================================================================
//
// REQUIREMENT: Programming/C++ CO1 - Arrays
//
// This module demonstrates:
// - Fixed-size C++ arrays for sensor data storage
// - Array traversal and processing
// - Temperature calculation and control logic
// - Integration with Java via file I/O bridge
//
// ===================================================================

const int MAX_SENSORS = 10;  // Fixed-size array for sensor readings

// Temperature sensor data structure
struct SensorReading {
    double temperature;
    double humidity;
    int status;  // 0=OFF, 1=COOLING, 2=ALARM
};

// =================================================================
// ARRAY-BASED SENSOR BUFFER
// =================================================================
// Array Purpose: Store historical sensor readings for trend analysis
// Why Array: Fixed collection of recent measurements; enables bounded
//            memory usage and predictable array processing
// Array Size: MAX_SENSORS = 10 (circular buffer simulation)
// =================================================================

SensorReading sensorArray[MAX_SENSORS];
int sensorIndex = 0;
int sensorCount = 0;

// =================================================================
// FUNCTION: addSensorReading
// Purpose: Add a new sensor reading to the array
// Parameters: temperature (current), humidity, status
// Array Operation: Direct indexing with circular wraparound
// =================================================================
void addSensorReading(double temp, double humidity, int status) {
    sensorArray[sensorIndex].temperature = temp;
    sensorArray[sensorIndex].humidity = humidity;
    sensorArray[sensorIndex].status = status;
    
    sensorIndex = (sensorIndex + 1) % MAX_SENSORS;
    
    if (sensorCount < MAX_SENSORS) {
        sensorCount++;
    }
}

// =================================================================
// FUNCTION: getAverageTemperature
// Purpose: Calculate average temperature from array
// Array Operation: Traversal of all elements in array
// Returns: Average of stored readings
// =================================================================
double getAverageTemperature() {
    if (sensorCount == 0) {
        return 0.0;
    }
    
    double sum = 0.0;
    
    // ARRAY TRAVERSAL: Loop through all sensor readings
    for (int i = 0; i < sensorCount; i++) {
        sum += sensorArray[i].temperature;
    }
    
    return sum / sensorCount;
}

// =================================================================
// FUNCTION: detectTrend
// Purpose: Detect temperature trend from array data
// Array Operation: Compare adjacent elements to find trend
// Returns: 1 (rising), -1 (falling), 0 (stable)
// =================================================================
int detectTrend() {
    if (sensorCount < 2) {
        return 0;  // Insufficient data
    }
    
    double oldest = sensorArray[(sensorIndex - sensorCount + MAX_SENSORS) % MAX_SENSORS].temperature;
    double newest = sensorArray[(sensorIndex - 1 + MAX_SENSORS) % MAX_SENSORS].temperature;
    
    double diff = newest - oldest;
    
    if (diff > 0.5) {
        return 1;  // Temperature rising
    } else if (diff < -0.5) {
        return -1;  // Temperature falling
    } else {
        return 0;   // Stable
    }
}

// =================================================================
// MAIN PROCESSING LOGIC
// =================================================================
// Input: current temperature, target temperature (from Java/CppBridge)
// Processing:
//   1. Load input from cpp/input.txt
//   2. Process through sensor array
//   3. Calculate temperature error
//   4. Determine cooling status
//   5. Write output to cpp/output.txt
// =================================================================

int main() {
    
    // =================================================================
    // READ INPUT FROM JAVA
    // =================================================================
    
    double currentTemperature = 0.0;
    double targetTemperature = 0.0;
    
    std::ifstream inputFile("input.txt");
    
    if (!inputFile.is_open()) {
        std::cerr << "Error: Cannot open input.txt" << std::endl;
        return 1;
    }
    
    inputFile >> currentTemperature >> targetTemperature;
    inputFile.close();
    
    std::cout << "C++ Engine: Read temperatures" << std::endl;
    std::cout << "  Current: " << currentTemperature << "°C" << std::endl;
    std::cout << "  Target:  " << targetTemperature << "°C" << std::endl;
    
    // =================================================================
    // PROCESS THROUGH ARRAY
    // =================================================================
    
    // Add the current reading to sensor array
    int status = 0;
    addSensorReading(currentTemperature, 65.0, status);
    
    // Calculate average from array
    double averageTemp = getAverageTemperature();
    
    // Detect trend from array history
    int trend = detectTrend();
    
    std::cout << "Array Processing:" << std::endl;
    std::cout << "  Readings stored: " << sensorCount << "/" << MAX_SENSORS << std::endl;
    std::cout << "  Average temp:    " << averageTemp << "°C" << std::endl;
    std::cout << "  Trend:           " << (trend > 0 ? "RISING" : trend < 0 ? "FALLING" : "STABLE") << std::endl;
    
    // =================================================================
    // CALCULATE TEMPERATURE ERROR
    // =================================================================
    
    double temperatureError = currentTemperature - targetTemperature;
    
    std::cout << "Error Calculation:" << std::endl;
    std::cout << "  Temperature Error: " << temperatureError << "°C" << std::endl;
    
    // =================================================================
    // DETERMINE COOLING STATUS
    // =================================================================
    
    std::string coolingStatus = "OFF";
    
    // Hysteresis control: avoid chattering
    const double ON_THRESHOLD = 2.0;    // Turn ON when error > 2°C
    const double OFF_THRESHOLD = 0.5;   // Turn OFF when error < 0.5°C
    
    if (temperatureError > ON_THRESHOLD) {
        coolingStatus = "ON";
    } else if (temperatureError <= OFF_THRESHOLD) {
        coolingStatus = "OFF";
    } else {
        coolingStatus = "IDLE";
    }
    
    // If trend is falling, less aggressive cooling needed
    if (trend < 0 && temperatureError > 0) {
        coolingStatus = "IDLE";
    }
    
    std::cout << "Cooling Decision: " << coolingStatus << std::endl;
    
    // =================================================================
    // WRITE OUTPUT FOR JAVA
    // =================================================================
    
    std::ofstream outputFile("output.txt");
    
    if (!outputFile.is_open()) {
        std::cerr << "Error: Cannot open output.txt" << std::endl;
        return 1;
    }
    
    // Format: temperature error on line 1, cooling status on line 2
    outputFile << temperatureError << std::endl;
    outputFile << coolingStatus << std::endl;
    
    outputFile.close();
    
    std::cout << "C++ Engine: Output written to output.txt" << std::endl;
    std::cout << "C++ Engine: Cycle complete (exit code 0)" << std::endl;
    
    return 0;  // Success
}
