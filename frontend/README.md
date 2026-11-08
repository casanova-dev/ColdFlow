# ColdFlow frontend demo

A responsive, dependency-free browser demo for the ColdFlow warehouse project.
It is a standalone interface and does not connect to the Java desktop app or
PostgreSQL.

## Run locally

```bash
cd frontend
python -m http.server 8080
```

Then open http://localhost:8080.

## Included demo sections

- Overview dashboard with metrics derived from sample browser data
- CG implementation section with control registers and monitoring curve
- Java layer overview with Dashboard.java, InventoryManager.java, StorageZone.java, and CppBridge.java
- Batch management screen for the singly linked list task
- Inventory flow screen for the doubly linked list task
- Zone monitoring screen for the circular linked list task
- CO2 demonstration console for the integrated linked-list explanation

The app starts with seeded sample data and persists edits in browser
`localStorage`. **Reset demo** restores the sample state. No live Java, C++, or
database data is fetched by this frontend.
