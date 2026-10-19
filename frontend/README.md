# ColdFlow frontend

A responsive, dependency-free frontend for the ColdFlow warehouse control system.

## Run locally

```bash
cd frontend
python -m http.server 8080
```

Then open http://localhost:8080.

## Included modules

- Overview dashboard with live warehouse metrics
- CG implementation section with control registers and monitoring curve
- Java layer overview with Dashboard.java, InventoryManager.java, StorageZone.java, and CppBridge.java
- Batch management screen for the singly linked list task
- Inventory flow screen for the doubly linked list task
- Zone monitoring screen for the circular linked list task
- CO2 demonstration console for the integrated linked-list explanation

The app persists updates in browser `localStorage` and is designed with a polished, enterprise-style dashboard layout.
