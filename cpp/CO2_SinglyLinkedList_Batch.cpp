#include <iostream>
#include <string>
using namespace std;

// ===============================================================
// Samarth - Singly Linked List for Batch Management
// ColdFlow CO2: Choose suitable type of linked list for applications
// ===============================================================
// Why singly linked list?
// Batch records are processed mostly in a forward, sequential order.
// We add batches at the start/end and often delete/search by batchId.
// A singly linked list is simple, memory-efficient and suitable for
// such sequential cold-chain batch records.
// Time complexity:
//   Insert at beginning: O(1)
//   Insert at end: O(n)
//   Delete by batchId: O(n)
//   Search by batchId: O(n)
//   Display all: O(n)
// ===============================================================

struct Batch {
    int batchId;
    string productId;
    string productName;
    int quantity;
    string expiryDate;
    string storageZone;
};

class SinglyLinkedList {
private:
    struct Node {
        Batch data;
        Node* next;

        Node(const Batch& value) : data(value), next(nullptr) {}
    };

    Node* head;
    Node* tail;
    int size;

public:
    SinglyLinkedList() : head(nullptr), tail(nullptr), size(0) {}

    ~SinglyLinkedList() {
        Node* current = head;
        while (current != nullptr) {
            Node* nextNode = current->next;
            delete current;
            current = nextNode;
        }
        head = nullptr;
        tail = nullptr;
        size = 0;
    }

    bool existsByBatchId(int batchId) const {
        Node* current = head;
        while (current != nullptr) {
            if (current->data.batchId == batchId) return true;
            current = current->next;
        }
        return false;
    }

    void insertAtBeginning(const Batch& batch) {
        if (existsByBatchId(batch.batchId)) {
            cout << "Duplicate batchId found. Please use a unique ID.\n";
            return;
        }

        Node* newNode = new Node(batch);
        if (head == nullptr) {
            head = tail = newNode;
        } else {
            newNode->next = head;
            head = newNode;
        }
        size++;
        cout << "Batch inserted at beginning successfully.\n";
    }

    void insertAtEnd(const Batch& batch) {
        if (existsByBatchId(batch.batchId)) {
            cout << "Duplicate batchId found. Please use a unique ID.\n";
            return;
        }

        Node* newNode = new Node(batch);
        if (tail == nullptr) {
            head = tail = newNode;
        } else {
            tail->next = newNode;
            tail = newNode;
        }
        size++;
        cout << "Batch inserted at end successfully.\n";
    }

    bool deleteByBatchId(int batchId) {
        if (head == nullptr) {
            cout << "List is empty. Nothing to delete.\n";
            return false;
        }

        Node* current = head;
        Node* previous = nullptr;

        while (current != nullptr && current->data.batchId != batchId) {
            previous = current;
            current = current->next;
        }

        if (current == nullptr) {
            cout << "Batch with ID " << batchId << " not found.\n";
            return false;
        }

        if (previous == nullptr) {
            head = head->next;
            if (head == nullptr) tail = nullptr;
        } else {
            previous->next = current->next;
            if (tail == current) tail = previous;
        }

        delete current;
        size--;
        cout << "Batch " << batchId << " deleted successfully.\n";
        return true;
    }

    Batch* searchByBatchId(int batchId) {
        Node* current = head;
        while (current != nullptr) {
            if (current->data.batchId == batchId) {
                return &(current->data);
            }
            current = current->next;
        }
        return nullptr;
    }

    bool updateQuantity(int batchId, int newQuantity) {
        Node* current = head;
        while (current != nullptr) {
            if (current->data.batchId == batchId) {
                current->data.quantity = newQuantity;
                cout << "Quantity updated successfully.\n";
                return true;
            }
            current = current->next;
        }
        cout << "Batch with ID " << batchId << " not found.\n";
        return false;
    }

    void displayAll() const {
        if (head == nullptr) {
            cout << "No batches available in the list.\n";
            return;
        }

        cout << "\nBatch Inventory:\n";
        cout << "------------------------------------------------------------\n";
        cout << "BatchId  ProductId  ProductName         Qty  ExpiryDate   StorageZone\n";
        cout << "------------------------------------------------------------\n";

        Node* current = head;
        while (current != nullptr) {
            cout << current->data.batchId << "\t"
                 << current->data.productId << "\t"
                 << current->data.productName << "\t\t"
                 << current->data.quantity << "\t"
                 << current->data.expiryDate << "\t"
                 << current->data.storageZone << "\n";
            current = current->next;
        }
        cout << "------------------------------------------------------------\n";
    }

    int countNodes() const {
        return size;
    }

    void printMenu() const {
        cout << "\n=== Batch Management (Singly Linked List) ===\n";
        cout << "1. Insert batch at beginning\n";
        cout << "2. Insert batch at end\n";
        cout << "3. Delete batch by batchId\n";
        cout << "4. Search batch by batchId\n";
        cout << "5. Update quantity\n";
        cout << "6. Display all batches\n";
        cout << "7. Count nodes\n";
        cout << "8. Exit\n";
        cout << "Enter choice: ";
    }
};

int main() {
    SinglyLinkedList list;
    int choice;

    cout << "\n========================================================\n";
    cout << "ColdFlow - Batch Management using Singly Linked List\n";
    cout << "========================================================\n";

    while (true) {
        list.printMenu();
        cin >> choice;

        if (choice == 8) {
            cout << "Exiting Batch Management...\n";
            break;
        }

        switch (choice) {
            case 1: {
                Batch b;
                cout << "Enter batchId: "; cin >> b.batchId;
                cout << "Enter productId: "; cin >> b.productId;
                cout << "Enter productName: "; cin >> b.productName;
                cout << "Enter quantity: "; cin >> b.quantity;
                cout << "Enter expiryDate: "; cin >> b.expiryDate;
                cout << "Enter storageZone: "; cin >> b.storageZone;
                list.insertAtBeginning(b);
                break;
            }
            case 2: {
                Batch b;
                cout << "Enter batchId: "; cin >> b.batchId;
                cout << "Enter productId: "; cin >> b.productId;
                cout << "Enter productName: "; cin >> b.productName;
                cout << "Enter quantity: "; cin >> b.quantity;
                cout << "Enter expiryDate: "; cin >> b.expiryDate;
                cout << "Enter storageZone: "; cin >> b.storageZone;
                list.insertAtEnd(b);
                break;
            }
            case 3: {
                int id;
                cout << "Enter batchId to delete: "; cin >> id;
                list.deleteByBatchId(id);
                break;
            }
            case 4: {
                int id;
                cout << "Enter batchId to search: "; cin >> id;
                Batch* result = list.searchByBatchId(id);
                if (result == nullptr) {
                    cout << "Batch not found.\n";
                } else {
                    cout << "Batch found: ID=" << result->batchId
                         << ", Product=" << result->productName
                         << ", Quantity=" << result->quantity << "\n";
                }
                break;
            }
            case 5: {
                int id, qty;
                cout << "Enter batchId: "; cin >> id;
                cout << "Enter new quantity: "; cin >> qty;
                list.updateQuantity(id, qty);
                break;
            }
            case 6:
                list.displayAll();
                break;
            case 7:
                cout << "Total nodes: " << list.countNodes() << "\n";
                break;
            default:
                cout << "Invalid choice. Please select 1 to 8.\n";
                break;
        }
    }

    return 0;
}

/*
Sample Output:
========================================================
ColdFlow - Batch Management using Singly Linked List
========================================================
=== Batch Management (Singly Linked List) ===
1. Insert batch at beginning
2. Insert batch at end
3. Delete batch by batchId
4. Search batch by batchId
5. Update quantity
6. Display all batches
7. Count nodes
8. Exit
Enter choice: 1
Enter batchId: 101
Enter productId: P01
Enter productName: MilkPack
Enter quantity: 40
Enter expiryDate: 2026-10-01
Enter storageZone: Zone-A
Batch inserted at beginning successfully.

Batch Inventory:
------------------------------------------------------------
BatchId  ProductId  ProductName         Qty  ExpiryDate   StorageZone
------------------------------------------------------------
101      P01        MilkPack            40    2026-10-01  Zone-A
------------------------------------------------------------

Total nodes: 1
*/
