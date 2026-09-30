# ColdFlow frontend

A responsive, dependency-free frontend for the four CO2 linked-list tasks.

## Run locally

From the repository root, serve the `frontend` directory with any static server:

```bash
cd frontend
python -m http.server 8080
```

Open http://localhost:8080.

The interface runs in local demonstration mode and persists changes in browser `localStorage`. It includes:

- Samyak: batch insertion, filtering, validation, and deletion.
- Sarthak: inventory insertion, forward/backward traversal, and quantity updates.
- Zaki: circular zone monitoring with NORMAL/ALERT status.
- Samarth: integrated CO2 demonstration console.
