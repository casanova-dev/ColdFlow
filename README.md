# ColdFlow

ColdFlow is a cold-chain warehouse coursework project that demonstrates Java
object-oriented programming and inventory structures, a C++ temperature
processing engine, and a browser-based dashboard demo.

## What is connected

- **Java desktop application** (`Java/src/coldflow`): models freezer, chiller,
  and ambient zones; manages inventory in a FEFO min-heap; and reads/writes
  inventory and sensor data through PostgreSQL repositories.
- **C++ engine** (`cpp/ColdFlowEngine.cpp`): reads one current/target temperature
  pair from `cpp/input.txt`, calculates the error and cooling status, and writes
  the two-line result to `cpp/output.txt`. Java's `StorageZone` calls it through
  `CppBridge` using those files.
- **Browser dashboard** (`frontend`): a standalone demonstration using seeded
  sample data and browser `localStorage`. It does not connect to the Java
  application or PostgreSQL.
- **Coursework demonstrations** (`coa`, `cpp/CO3_CO4`, and `graphics`): separate
  assembly, data-structure, and graphics exercises.

The browser dashboard and Java desktop app are separate interfaces; updates in
one are not synchronized to the other.

## Prerequisites

- Java 15 or newer (the Java source uses text blocks); Java 21 is recommended.
- The PostgreSQL JDBC driver is included at
  `Java/lib/postgresql-42.7.14.jar`.
- A PostgreSQL database configured with the tables expected by the repositories.
  At minimum, the batch repository expects `batches` and `storage_zones`.
- A C++ compiler such as MinGW `g++` to rebuild the engine. A Windows executable
  is already included at `cpp/ColdFlowEngine.exe`.
- Python 3 to serve the static frontend, if desired.

## Run the Java desktop application (PowerShell)

Set these environment variables to the PostgreSQL connection details before
launching. The connection uses SSL (`sslmode=require`).

```powershell
$env:COLDFLOW_DB_HOST = "<database-host>"
$env:COLDFLOW_DB_PORT = "<database-port>"
$env:COLDFLOW_DB_NAME = "<database-name>"
$env:COLDFLOW_DB_USER = "<database-user>"
$env:COLDFLOW_DB_PASSWORD = "<database-password>"
```

From the repository root, compile and launch:

```powershell
$buildDir = Join-Path $env:TEMP "coldflow-java-build"
New-Item -ItemType Directory -Force $buildDir | Out-Null
$javaSources = Get-ChildItem Java\src -Recurse -Filter *.java | ForEach-Object FullName
javac -cp "Java\lib\postgresql-42.7.14.jar" -d $buildDir $javaSources
java -cp "$buildDir;Java\lib\postgresql-42.7.14.jar" coldflow.Main
```

Run the application from the repository root: the Java-to-C++ bridge uses
repository-relative `cpp` paths. The desktop app loads inventory at startup, so
valid database settings and the expected schema are required.

## Run the browser demo

```powershell
python -m http.server 8080 --directory frontend
```

Open <http://localhost:8080>. This demo edits only its own seeded browser data;
use **Reset demo** to restore the initial sample state.

## Build and smoke-test the C++ engine

From the repository root:

```powershell
g++ -std=c++17 cpp\ColdFlowEngine.cpp -o cpp\ColdFlowEngine.exe
```

The Java application invokes this executable when a storage zone runs a
temperature cycle. To exercise the Java bridge independently, first compile
the Java sources as above, then run:

```powershell
java -cp "$buildDir;Java\lib\postgresql-42.7.14.jar" coldflow.CppBridge
```

That bridge writes `cpp/input.txt` and `cpp/output.txt`; these files are
generated test/runtime data.

## Run the FEFO smoke test

The dependency-free test checks heap insertion, earliest-expiry selection,
duplicate IDs, removal/index consistency, quantity updates, and empty
dispatch. From the repository root:

```powershell
$buildDir = Join-Path $env:TEMP "coldflow-java-test-build"
New-Item -ItemType Directory -Force $buildDir | Out-Null
$javaSources = Get-ChildItem Java\src -Recurse -Filter *.java | ForEach-Object FullName
javac -d $buildDir $javaSources Java\test\coldflow\FEFOHeapTest.java
java -cp $buildDir coldflow.FEFOHeapTest
```

The database-backed repository demo at
`Java/src/coldflow/database/BatchRepositoryTest.java` writes to the configured
database, so it is not part of this offline smoke test.
