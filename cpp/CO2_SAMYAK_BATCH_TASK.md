# CO2 Task: Batch Management (Singly Linked List)
**Developer:** Samyak  
**Data Structure:** Singly Linked List  
**File:** `CO2_SinglyLinkedList_Batch.cpp`  
**Concept:** FIFO batch processing with sequential access patterns

---

## 📋 Overview
This task implements batch management for cold-chain warehouses using a Singly Linked List. Batches are tracked with product information, quantities, expiry dates, and storage zones. The singly linked list is chosen because batch records are processed sequentially in a forward direction—insertion, deletion, and searching follow a linear pattern.

---

## ⚙️ Required Inputs

### 1. **Batch Addition**
- **Batch ID** (integer): Unique identifier for the batch
- **Product ID** (string): Product reference code (e.g., "P01", "P100")
- **Product Name** (string): Human-readable product name (e.g., "MilkPack", "Cheese")
- **Quantity** (integer): Number of units in the batch
- **Expiry Date** (string): Batch expiration date in format YYYY-MM-DD
- **Storage Zone** (string): Location/zone name where batch is stored (e.g., "Zone-A", "ColdRoom-2")

### 2. **Batch Deletion**
- **Batch ID** (integer): Unique identifier of the batch to remove

### 3. **Batch Search**
- **Batch ID** (integer): Unique identifier to locate the batch

### 4. **Batch Update**
- **Batch ID** (integer): Identify which batch to update
- **New Quantity** (integer): Updated quantity value

---

## ✅ Expected Outputs

### 1. **Insertion Result**
```
Batch inserted at beginning successfully.
```
- Batch successfully added to the linked list
- Validation: Duplicate Batch IDs are rejected
- Message if duplicate: "Duplicate batchId found. Please use a unique ID."

### 2. **Updated Batch List Display**
```
Batch Inventory:
------------------------------------------------------------
BatchId  ProductId  ProductName         Qty  ExpiryDate   StorageZone
------------------------------------------------------------
101      P01        MilkPack            40    2026-10-01  Zone-A
102      P02        YogurtJar           25    2026-09-28  Zone-B
------------------------------------------------------------
```
- Tabular format showing all batches in insertion order
- Columns: BatchId, ProductId, ProductName, Qty, ExpiryDate, StorageZone

### 3. **Search Result**
```
Batch found: ID=101, Product=MilkPack, Quantity=40
```
- Batch details if found
- Message if not found: "Batch not found."

### 4. **Update Result**
```
Quantity updated successfully.
```
- Confirmation of quantity change
- Message if batch not found: "Batch with ID [batchId] not found."

### 5. **Delete Result**
```
Batch 101 deleted successfully.
```
- Confirmation of deletion
- Message if list empty: "List is empty. Nothing to delete."
- Message if not found: "Batch with ID [batchId] not found."

### 6. **Validation Messages**
- **Duplicate Batch ID:** "Duplicate batchId found. Please use a unique ID."
- **Empty List:** "No batches available in the list."
- **Invalid Operation:** Error message with reason

### 7. **Node Count**
```
Total nodes: 3
```
- Current number of batches in the linked list

---

## 🔄 Operations & Time Complexity

| Operation | Time Complexity | Description |
|-----------|-----------------|-------------|
| Insert at beginning | O(1) | Direct head insertion after duplicate check |
| Insert at end | O(n) | Traversal needed to find tail |
| Delete by batchId | O(n) | Search + removal |
| Search by batchId | O(n) | Linear traversal |
| Update quantity | O(n) | Search then modify |
| Display all | O(n) | Traverse all nodes |

---

## 📊 Sample Execution

### Input Sequence:
```
1. Insert at beginning
   Enter batchId: 101
   Enter productId: P01
   Enter productName: MilkPack
   Enter quantity: 40
   Enter expiryDate: 2026-10-01
   Enter storageZone: Zone-A

2. Insert at end
   Enter batchId: 102
   Enter productId: P02
   Enter productName: Cheese
   Enter quantity: 20
   Enter expiryDate: 2026-11-12
   Enter storageZone: ColdRoom-2

3. Search by batchId: 101
4. Update quantity: 101, new qty: 50
5. Display all
6. Count nodes
```

### Output Display:
```
Batch Inventory:
------------------------------------------------------------
BatchId  ProductId  ProductName         Qty  ExpiryDate   StorageZone
------------------------------------------------------------
101      P01        MilkPack            50    2026-10-01  Zone-A
102      P02        Cheese              20    2026-11-12  ColdRoom-2
------------------------------------------------------------

Total nodes: 2
```

---

## 🎯 Key Features

✅ **Singly Linked List** - Linear data structure with forward-only traversal  
✅ **Duplicate Prevention** - Batch IDs are validated for uniqueness  
✅ **CRUD Operations** - Create, Read, Update, Delete batch records  
✅ **Sequential Processing** - Suitable for batch-by-batch warehouse operations  
✅ **Memory Efficient** - Only one pointer per node (compared to doubly linked)  
✅ **Display Capability** - Formatted output of all batch records  

---

## 📝 Notes

- Singly linked list is ideal when batches need forward-only processing
- Deletion requires O(n) time but ensures data integrity
- Expiry dates enable FIFO or FEFO dispatch strategies
- Zone tracking supports multi-location warehouse management
