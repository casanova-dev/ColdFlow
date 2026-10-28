# ColdFlow CO3 and CO4 Module

This project adds a modular C++17 implementation of the warehouse inventory logic for the CO3 and CO4 requirements.

## Modules

- CO3: Queue and priority queue for warehouse alert and batch dispatch
- CO4: Linear and binary search, sorting by expiry date and quantity

## Folder layout

```text
cpp/
├── CO3_CO4/
│   ├── InventoryRecord.h
│   ├── InventoryRecord.cpp
│   ├── QueueAlert.h
│   ├── QueueAlert.cpp
│   ├── FEFOPriorityQueue.h
│   ├── FEFOPriorityQueue.cpp
│   ├── SearchInventory.h
│   ├── SearchInventory.cpp
│   ├── SortInventory.h
│   ├── SortInventory.cpp
│   ├── ColdFlow_CO3_CO4.cpp
│   └── README.md
└── ... existing project files remain untouched
```

## Compilation

```bash
g++ -std=c++17 -Wall -Wextra -pedantic cpp/CO3_CO4/InventoryRecord.cpp cpp/CO3_CO4/QueueAlert.cpp cpp/CO3_CO4/FEFOPriorityQueue.cpp cpp/CO3_CO4/SearchInventory.cpp cpp/CO3_CO4/SortInventory.cpp cpp/CO3_CO4/ColdFlow_CO3_CO4.cpp -o coldflow_co3_co4
```

## Execution

```bash
./coldflow_co3_co4
```

## Sample test cases

### 1. Empty queue
- Expected: `No alerts pending.`

### 2. Duplicate batch IDs
- Example: `B-101` used for two product batches
- Expected: queue still stores both records and dispatch chooses by expiry date, not unique ID alone.

### 3. Missing Product ID in search
- Search for `P999`
- Expected: `not found` message

### 4. Equal expiry dates
- Two records with same date should be ordered deterministically by batch ID or product ID.

### 5. Zero stock
- Product quantity is `0`
- Expected: dispatch is still valid and low-stock alert should be generated if threshold is reached.

### 6. Multiple batches of same product
- Example: `P101` with multiple batch IDs
- Expected: FEFO dispatch uses earliest expiry date first, then updates quantity correctly.

## Expected output highlights

```text
Pending alerts:
  [1] A1 - Temperature spike detected in Zone A.
  [2] A2 - Cold-chain door left open.

FEFO dispatch queue (earliest expiry first):
  [1] Batch B-130 | Product P130 | Expiry 2026-07-30 | Qty 0
  [2] Batch B-101 | Product P101 | Expiry 2026-08-22 | Qty 50
  ...

Linear search found P101 at index ...
Binary search found P101 ...

Low stock alert: Product P150 quantity is now 6.
```

## Time complexity

- Queue enqueue/dequeue: O(1)
- Priority queue push/pop: O(log n)
- Linear search: O(n)
- Binary search: O(log n) after sorting
- Sorting by expiry date/quantity: O(n log n)

## CO3 and CO4 mapping

- CO3:
  - FIFO queue for alerts
  - priority queue for FEFO dispatch
  - inventory update and low-stock alert generation

- CO4:
  - linear search by product ID
  - binary search on product-sorted data
  - sorting by expiry date and quantity

## Team contribution

- Samyak Bhalerao: queue alert management and linear search
- Zaki Haque: FEFO priority queue and binary search
- Samarth Tayde: inventory dispatch updates and sorting by expiry date
- Sarthak Korde: edge-case testing and sorting by quantity

## Notes

The repository already contained older C++ work for a previous assignment, so the new CO3/CO4 implementation was added as a separate module to preserve compatibility and avoid breaking existing working code.
