# ColdFlow C++ Processing Engine

## Overview

This is the **CO1 Programming / Data Structures** component for ColdFlow, demonstrating the requirement:  
**"Develop applications using arrays."**

The C++ engine processes temperature data using a **fixed-size C++ array** to store and analyze sensor readings for the cold-chain warehouse system.

---

## Array Implementation

### Array Declaration
```cpp
const int MAX_SENSORS = 10;

struct SensorReading {
    double temperature;
    double humidity;
    int status;
};

SensorReading sensorArray[MAX_SENSORS];  // Fixed-size array
```

### Array Purpose
- **Store sensor readings during one engine run** for trend analysis
- **Maintain bounded memory usage** with predictable behavior
- **Enable sensor data processing** (averaging, trend detection)
- **Simulate circular buffer** pattern for continuous monitoring

### Array Operations Implemented

#### 1. **Array Indexing & Insertion**
```cpp
void addSensorReading(double temp, double humidity, int status) {
    sensorArray[sensorIndex].temperature = temp;
    sensorArray[sensorIndex].humidity = humidity;
    sensorArray[sensorIndex].status = status;
    
    sensorIndex = (sensorIndex + 1) % MAX_SENSORS;  // Circular wraparound
    if (sensorCount < MAX_SENSORS) {
        sensorCount++;
    }
}
```

#### 2. **Array Traversal**
```cpp
double getAverageTemperature() {
    double sum = 0.0;
    
    // Loop through all sensor readings in array
    for (int i = 0; i < sensorCount; i++) {
        sum += sensorArray[i].temperature;
    }
    
    return sum / sensorCount;
}
```

#### 3. **Array Element Comparison**
```cpp
int detectTrend() {
    // Compare oldest vs newest array elements
    double oldest = sensorArray[(sensorIndex - sensorCount + MAX_SENSORS) % MAX_SENSORS].temperature;
    double newest = sensorArray[(sensorIndex - 1 + MAX_SENSORS) % MAX_SENSORS].temperature;
    
    double diff = newest - oldest;
    
    if (diff > 0.5)  return 1;   // Rising
    if (diff < -0.5) return -1;  // Falling
    return 0;                     // Stable
}
```

---

## Processing Logic

### Input Format
**File:** `cpp/input.txt`

```
<current_temperature>
<target_temperature>
```

**Example:**
```
-18.5
-20.0
```

### Processing Steps

1. **Read Input** – Load current and target temperatures from Java bridge
2. **Add to Array** – Store reading in sensor array (circular buffer)
3. **Calculate Average** – Traverse array to compute average temperature
4. **Detect Trend** – Compare oldest vs newest readings to identify trend
5. **Calculate Error** – Compute difference: `error = current - target`
6. **Determine Cooling Status**:
   - `ON`: Error > 2.0°C
   - `IDLE`: 0.5°C < Error ≤ 2.0°C
   - `OFF`: Error ≤ 0.5°C
   - If the detected trend is falling, a positive error is reduced to `IDLE`

The engine currently reads only one temperature pair and exits. Its sensor
array is therefore reset on each invocation, so trend detection reports
`STABLE` during normal Java bridge calls. The array can demonstrate trend
processing within a longer-running/multi-reading run, but it does not retain
history between separate runs. The thresholds above are decision bands; the
current implementation does not preserve cooling state between runs.

### Output Format
**File:** `cpp/output.txt`

```
<temperature_error>
<cooling_status>
```

**Example:**
```
1.50
IDLE
```

---

## Compilation

### On Windows (MinGW/MSVC)
```bash
cd cpp
g++ -o ColdFlowEngine.exe ColdFlowEngine.cpp
```

Or with MSVC:
```bash
cd cpp
cl ColdFlowEngine.cpp /Fe:ColdFlowEngine.exe
```

### On Linux/macOS
```bash
cd cpp
g++ -o ColdFlowEngine ColdFlowEngine.cpp
```

`CppBridge` already selects the executable name from the current operating
system (`ColdFlowEngine.exe` on Windows, `ColdFlowEngine` elsewhere).

---

## Execution

### Direct Execution (Test)
```powershell
Set-Location cpp
@("-18.5", "-20.0") | Set-Content input.txt
.\ColdFlowEngine.exe
Get-Content output.txt
```

### Integration with Java
Run Java from the repository root. `CppBridge`:
1. Writes temperature data to `cpp/input.txt`
2. Executes `ColdFlowEngine.exe` from the `cpp/` directory
3. Reads results from `cpp/output.txt`
4. Returns the result to the Java caller; `StorageZone` uses it during its temperature cycle

---

## Test Results

### Test Case 1: Warmer than target (moderate error)
**Input:**
```
-18.5
-20.0
```

**Expected Output:**
```
1.50
IDLE
```

**Explanation:**  
- Current: -18.5°C, Target: -20.0°C
- Error: 1.5°C (too warm)
- Action: Set cooling status to IDLE (the 1.5°C error is within the IDLE band)

### Test Case 2: At Target (No Cooling)
**Input:**
```
-20.1
-20.0
```

**Expected Output:**
```
-0.1
OFF
```

**Explanation:**  
- Current: -20.1°C, Target: -20.0°C
- Error: -0.1°C (slightly cold)
- Action: Keep compressor OFF

### Test Case 3: Warmer than target (moderate error)
**Input:**
```
-19.0
-20.0
```

**Expected Output:**
```
1.0
IDLE
```

**Explanation:**  
- Current: -19.0°C, Target: -20.0°C
- Error: 1.0°C (within the IDLE band)
- Trend: STABLE, because this invocation contains only one reading
- Action: IDLE

---

## Integration Architecture

```
Java StorageZone.runCycle()
        ↓
CppBridge.sendTemperature()
        ↓
cpp/input.txt (write)
        ↓
ColdFlowEngine (platform-specific executable)
        ↓
Array processing:
  - Add sensor reading
  - Calculate average
  - Detect trend
  - Compute error
  - Determine status
        ↓
cpp/output.txt (write)
        ↓
CppBridge.readCppResult()
        ↓
Java StorageZone displays results
        ↓
Dashboard updated
```

---

## File Structure
```
cpp/
  ├── ColdFlowEngine.cpp    (C++ source with array processing)
  ├── ColdFlowEngine.exe    (compiled executable, auto-generated)
  ├── input.txt             (Java → C++ data, auto-generated)
  ├── output.txt            (C++ → Java results, auto-generated)
  └── README.md             (this file)
```

---

## Key Features

✅ **Fixed-size C++ array** (`SensorReading[MAX_SENSORS]`)  
✅ **Array traversal** (getAverageTemperature)  
✅ **Array element processing** (detectTrend, comparisons)  
✅ **Circular buffer pattern** (wraparound indexing)  
✅ **Real-world application** (temperature sensor data)  
✅ **Meaningful data flow** (array → analysis → decision)  
✅ **Java/C++ integration** (file-based bridge)  

---

## CO1 Requirement Checklist

- [x] Implement actual C++ array
- [x] Array has meaningful ColdFlow purpose
- [x] Input/output array operations implemented
- [x] Traversal, processing, calculation demonstrated
- [x] Accepts values from CppBridge.java
- [x] Produces output compatible with Java
- [x] Includes array usage comments
- [x] Code compiles successfully
- [x] Integration tested with Java

---

## Notes

- The array simulates a **circular buffer** for continuous sensor monitoring
- **Threshold bands** classify cooling status for the current reading
- **Trend detection** improves cooling decisions
- C++ runs independently; Java handles orchestration
- Output files are human-readable for debugging
