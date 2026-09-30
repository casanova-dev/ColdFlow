#include <iostream>
#include <string>
#include <iomanip>
using namespace std;

// ===============================================================
// Samyak - Final CO2 Integration and Demonstration
// ColdFlow CO2: Choose suitable type of linked list for applications
// ===============================================================
// This file integrates all three linked list solutions:
// - Samyak: Singly Linked List for Batch Management
// - Sarthak: Doubly Linked List for Inventory Management
// - Zaki: Circular Linked List for Warehouse Zone Monitoring
//
// Each structure is chosen based on the application need:
// 1. Singly linked list: sequential batch records, simple forward-only processing.
// 2. Doubly linked list: back-and-forth inventory browsing and updates.
// 3. Circular linked list: repeated monitoring of all warehouse zones.
//
// Big-O complexity summary:
// Singly Linked List:
//   Insert at beginning: O(1), end: O(n), search: O(n), delete: O(n)
// Doubly Linked List:
//   Insert beginning/end: O(1), search/delete: O(n), forward/backward traversal: O(n)
// Circular Linked List:
//   Insert/delete/search: O(n), circular traversal: O(n)
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
        while (head != nullptr) {
            Node* temp = head;
            head = head->next;
            delete temp;
        }
        tail = nullptr;
        size = 0;
    }

    bool existsByBatchId(int id) const {
        Node* current = head;
        while (current != nullptr) {
            if (current->data.batchId == id) return true;
            current = current->next;
        }
        return false;
    }

    void insertAtEnd(const Batch& value) {
        if (existsByBatchId(value.batchId)) {
            cout << "Duplicate batchId. Try another ID.\n";
            return;
        }
        Node* newNode = new Node(value);
        if (head == nullptr) {
            head = tail = newNode;
        } else {
            tail->next = newNode;
            tail = newNode;
        }
        size++;
    }

    void displayAll() const {
        if (head == nullptr) {
            cout << "Batch list is empty.\n";
            return;
        }
        Node* current = head;
        cout << "\nBatches:\n";
        while (current != nullptr) {
            cout << "BatchId=" << current->data.batchId
                 << ", Product=" << current->data.productName
                 << ", Qty=" << current->data.quantity
                 << ", Expiry=" << current->data.expiryDate
                 << ", Zone=" << current->data.storageZone << "\n";
            current = current->next;
        }
    }

    bool deleteByBatchId(int id) {
        if (head == nullptr) {
            cout << "Batch list is empty.\n";
            return false;
        }
        Node* previous = nullptr;
        Node* current = head;
        while (current != nullptr && current->data.batchId != id) {
            previous = current;
            current = current->next;
        }
        if (current == nullptr) {
            cout << "Batch not found.\n";
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
        cout << "Batch deleted.\n";
        return true;
    }

    int countNodes() const {
        return size;
    }
};

struct InventoryItem {
    string productId;
    string productName;
    int batchId;
    int quantity;
    string expiryDate;
    string storageZone;
};

class DoublyLinkedList {
private:
    struct Node {
        InventoryItem data;
        Node* prev;
        Node* next;
        Node(const InventoryItem& value) : data(value), prev(nullptr), next(nullptr) {}
    };
    Node* head;
    Node* tail;
    int size;

public:
    DoublyLinkedList() : head(nullptr), tail(nullptr), size(0) {}
    ~DoublyLinkedList() {
        Node* current = head;
        while (current != nullptr) {
            Node* nextNode = current->next;
            delete current;
            current = nextNode;
        }
        head = tail = nullptr;
        size = 0;
    }

    bool existsByBatchId(int id) const {
        Node* current = head;
        while (current != nullptr) {
            if (current->data.batchId == id) return true;
            current = current->next;
        }
        return false;
    }

    void insertAtEnd(const InventoryItem& value) {
        if (existsByBatchId(value.batchId)) {
            cout << "Duplicate batchId in inventory.\n";
            return;
        }
        Node* newNode = new Node(value);
        if (head == nullptr) {
            head = tail = newNode;
        } else {
            tail->next = newNode;
            newNode->prev = tail;
            tail = newNode;
        }
        size++;
    }

    void displayForward() const {
        if (head == nullptr) {
            cout << "Inventory list is empty.\n";
            return;
        }
        Node* current = head;
        cout << "\nInventory Forward:\n";
        while (current != nullptr) {
            cout << "Product=" << current->data.productName
                 << ", BatchId=" << current->data.batchId
                 << ", Qty=" << current->data.quantity << "\n";
            current = current->next;
        }
    }

    void displayBackward() const {
        if (tail == nullptr) {
            cout << "Inventory list is empty.\n";
            return;
        }
        Node* current = tail;
        cout << "\nInventory Backward:\n";
        while (current != nullptr) {
            cout << "Product=" << current->data.productName
                 << ", BatchId=" << current->data.batchId
                 << ", Qty=" << current->data.quantity << "\n";
            current = current->prev;
        }
    }

    int countNodes() const {
        return size;
    }
};

struct Zone {
    int zoneId;
    string zoneName;
    double minimumTemperature;
    double maximumTemperature;
    double currentTemperature;
    string zoneStatus;
};

class CircularLinkedList {
private:
    struct Node {
        Zone data;
        Node* next;
        Node(const Zone& value) : data(value), next(nullptr) {}
    };
    Node* head;
    int size;

public:
    CircularLinkedList() : head(nullptr), size(0) {}
    ~CircularLinkedList() {
        if (head == nullptr) return;
        Node* current = head;
        Node* first = head;
        do {
            Node* nextNode = current->next;
            delete current;
            current = nextNode;
        } while (current != first);
        head = nullptr;
        size = 0;
    }

    bool existsByZoneId(int id) const {
        if (head == nullptr) return false;
        Node* current = head;
        do {
            if (current->data.zoneId == id) return true;
            current = current->next;
        } while (current != head);
        return false;
    }

    void insertAtEnd(const Zone& value) {
        if (existsByZoneId(value.zoneId)) {
            cout << "Duplicate zoneId.\n";
            return;
        }
        Node* newNode = new Node(value);
        if (head == nullptr) {
            head = newNode;
            newNode->next = head;
        } else {
            Node* current = head;
            while (current->next != head) { current = current->next; }
            current->next = newNode;
            newNode->next = head;
        }
        size++;
    }

    void monitorZones() const {
        if (head == nullptr) {
            cout << "No zones to monitor.\n";
            return;
        }
        Node* current = head;
        do {
            if (current->data.currentTemperature >= current->data.minimumTemperature &&
                current->data.currentTemperature <= current->data.maximumTemperature) {
                current->data.zoneStatus = "NORMAL";
            } else {
                current->data.zoneStatus = "ALERT";
            }
            cout << "Zone " << current->data.zoneId << " -> " << current->data.zoneStatus << "\n";
            current = current->next;
        } while (current != head);
    }

    int countNodes() const {
        return size;
    }
};

void showComparison() {
    cout << "\n=== Linked List Comparison ===\n";
    cout << "1. Singly Linked List -> Best for sequential batch processing.\n";
    cout << "   Traversal: only forward. Good for insertion/deletion in one direction.\n";
    cout << "   Complexity: Insert front O(1), Insert end O(n), Search O(n), Delete O(n).\n\n";

    cout << "2. Doubly Linked List -> Best for inventory management.\n";
    cout << "   Traversal: forward and backward. Useful for browse and reverse operations.\n";
    cout << "   Complexity: Insert front/end O(1), Search O(n), Delete O(n), Traversal O(n).\n\n";

    cout << "3. Circular Linked List -> Best for repeated zone monitoring.\n";
    cout << "   Traversal: circular loop, no end condition. Good for round-robin monitoring.\n";
    cout << "   Complexity: Insert O(n), Search O(n), Delete O(n), Circular traversal O(n).\n";
}

int main() {
    cout << "\n========================================================\n";
    cout << "ColdFlow CO2 - Integrated Linked List Demonstration\n";
    cout << "========================================================\n";

    SinglyLinkedList batches;
    DoublyLinkedList inventory;
    CircularLinkedList zones;

    // Sample data for demonstration
    Batch batch1 = {101, "P01", "MilkPack", 50, "2026-10-15", "Zone-A"};
    Batch batch2 = {102, "P02", "Cheese", 30, "2026-11-02", "Zone-B"};
    batches.insertAtEnd(batch1);
    batches.insertAtEnd(batch2);

    InventoryItem item1 = {"P01", "MilkPack", 101, 50, "2026-10-15", "Zone-A"};
    InventoryItem item2 = {"P02", "Cheese", 102, 30, "2026-11-02", "Zone-B"};
    inventory.insertAtEnd(item1);
    inventory.insertAtEnd(item2);

    Zone zone1 = {1, "ColdRoomA", 2.0, 8.0, 6.5, "NORMAL"};
    Zone zone2 = {2, "ColdRoomB", 1.5, 7.0, 9.8, "ALERT"};
    zones.insertAtEnd(zone1);
    zones.insertAtEnd(zone2);

    int choice;
    while (true) {
        cout << "\n=== CO2 Main Menu ===\n";
        cout << "1. Batch Management\n";
        cout << "2. Inventory Management\n";
        cout << "3. Zone Monitoring\n";
        cout << "4. Linked List Comparison\n";
        cout << "5. Exit\n";
        cout << "Enter choice: ";
        cin >> choice;

        if (choice == 5) {
            cout << "Exiting ColdFlow CO2 integration...\n";
            break;
        }

        switch (choice) {
            case 1:
                batches.displayAll();
                cout << "Total batch nodes: " << batches.countNodes() << "\n";
                break;
            case 2:
                inventory.displayForward();
                inventory.displayBackward();
                cout << "Total inventory nodes: " << inventory.countNodes() << "\n";
                break;
            case 3:
                zones.monitorZones();
                cout << "Total zone nodes: " << zones.countNodes() << "\n";
                break;
            case 4:
                showComparison();
                break;
            default:
                cout << "Invalid choice. Please choose between 1 and 5.\n";
                break;
        }
    }

    return 0;
}

/*
Sample Output:
========================================================
ColdFlow CO2 - Integrated Linked List Demonstration
========================================================
=== CO2 Main Menu ===
1. Batch Management
2. Inventory Management
3. Zone Monitoring
4. Linked List Comparison
5. Exit
Enter choice: 3
Zone 1 -> NORMAL
Zone 2 -> ALERT

=== Linked List Comparison ===
1. Singly Linked List -> Best for sequential batch processing.
2. Doubly Linked List -> Best for inventory management.
3. Circular Linked List -> Best for repeated zone monitoring.
*/
" ,
