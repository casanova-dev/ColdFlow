# ColdFlow CO2 Linked List Modules

This folder contains the linked list assignments for the CO2 requirement of the ColdFlow project.

## Files

- `CO2_SinglyLinkedList_Batch.cpp`  
  Samyak: Batch Management using Singly Linked List

- `CO2_DoublyLinkedList_Inventory.cpp`  
  Sarthak: Inventory Management using Doubly Linked List

- `CO2_CircularLinkedList_Zone.cpp`  
  Zaki: Warehouse Zone Monitoring using Circular Linked List

- `CO2_Integration.cpp`  
  Samarth: Final menu-driven integration and comparison of all linked list types

## Compilation

Each file can be compiled independently:

```bash
cd cpp
g++ CO2_SinglyLinkedList_Batch.cpp -o batch
./batch
```

```bash
g++ CO2_DoublyLinkedList_Inventory.cpp -o inventory
./inventory
```

```bash
g++ CO2_CircularLinkedList_Zone.cpp -o zone
./zone
```

```bash
g++ CO2_Integration.cpp -o co2_integration
./co2_integration
```

## Why these linked list types?

- Singly Linked List: suitable for sequential batch records processed mostly in forward order.
- Doubly Linked List: suitable when forward and backward traversal are useful in inventory management.
- Circular Linked List: suitable for repeated monitoring of all warehouse zones in a round-robin pattern.

## Notes

- No `std::list` is used.
- Manual dynamic memory is used with proper cleanup.
- Code is simple, menu-driven and viva-friendly.
