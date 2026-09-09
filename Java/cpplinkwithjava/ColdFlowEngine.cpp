#include <iostream>
#include <iomanip>
#include <fstream>

using namespace std;

class ColdFlowEngine {

public:

    static double calculateError(
            double currentTemperature,
            double targetTemperature) {

        return currentTemperature - targetTemperature;
    }

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
             << currentTemperature << " C\n";

        cout << "Target Temperature  : "
             << targetTemperature << " C\n";

        cout << "Temperature Error   : "
             << error << " C\n";

        cout << "Cooling Required    : "
             << (cooling ? "YES" : "NO")
             << "\n";
    }
};

int main() {

    cout << "=====================================\n";
    cout << "       COLDFlow C++ Engine\n";
    cout << "=====================================\n\n";

    // Open input file
 ifstream inputFile("input.txt");

    if (!inputFile) {

        cout << "Error: Could not open input.txt\n";

        return 1;
    }

    double currentTemperature;
    double targetTemperature;

    // Read temperatures from file
    inputFile >> currentTemperature;
    inputFile >> targetTemperature;

    inputFile.close();

    // Process the received data
 double error =
        ColdFlowEngine::calculateError(
                currentTemperature,
                targetTemperature
        );

bool cooling =
        ColdFlowEngine::shouldCool(
                currentTemperature,
                targetTemperature
        );

ofstream outputFile("output.txt");

if (!outputFile) {

    cout << "Error: Could not create output.txt\n";

    return 1;
}

outputFile << fixed << setprecision(2);

outputFile << error << "\n";
outputFile << (cooling ? "ON" : "OFF") << "\n";

outputFile.close();

ColdFlowEngine::process(
        currentTemperature,
        targetTemperature
);

return 0;
}

