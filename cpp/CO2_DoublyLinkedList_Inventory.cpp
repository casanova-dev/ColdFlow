#include <iostream>
#include <string>
using namespace std;

// ===============================================================
// Sarthak - Doubly Linked List for Inventory Management
// ColdFlow CO2: Choose suitable type of linked list for applications
// ===============================================================
// Why doubly linked list?
// Inventory records often need forward and backward traversal while
// updating stock, searching by batchId, or deleting nodes.
// A doubly linked list supports both directions and is useful for
// warehouse inventory browsing and management.
// Time complexity:
//   Insert at beginning: O(1)
//   Insert at end: O(1)
//   Delete by batchId: O(n)
//   Search by batchId: O(n)
//   Forward traversal: O(n)
//   Backward traversal: O(n)
// ===============================================================

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

    bool existsByBatchId(int batchId) const {
        Node* current = head;
        while (current != nullptr) {
            if (current->data.batchId == batchId) return true;
            current = current->next;
        }
        return false;
    }

    void insertAtBeginning(const InventoryItem& item) {
        if (existsByBatchId(item.batchId)) {
            cout << "Duplicate batchId. Inventory item already exists.\n";
            return;
        }

        Node* newNode = new Node(item);
        if (head == nullptr) {
            head = tail = newNode;
        } else {
            newNode->next = head;
            head->prev = newNode;
            head = newNode;
        }
        size++;
        cout << "Inventory item inserted at beginning.\n";
    }

    void insertAtEnd(const InventoryItem& item) {
        if (existsByBatchId(item.batchId)) {
            cout << "Duplicate batchId. Inventory item already exists.\n";
            return;
        }

        Node* newNode = new Node(item);
        if (tail == nullptr) {
            head = tail = newNode;
        } else {
            tail->next = newNode;
            newNode->prev = tail;
            tail = newNode;
        }
        size++;
        cout << "Inventory item inserted at end.\n";
    }

    bool deleteByBatchId(int batchId) {
        if (head == nullptr) {
            cout << "Inventory list is empty. Nothing to delete.\n";
            return false;
        }

        Node* current = head;
        while (current != nullptr && current->data.batchId != batchId) {
            current = current->next;
        }

        if (current == nullptr) {
            cout << "Inventory item with batchId " << batchId << " not found.\n";
            return false;
        }

        if (current == head) {
            head = head->next;
            if (head != nullptr) head->prev = nullptr;
        } else if (current == tail) {
            tail = tail->prev;
            if (tail != nullptr) tail->next = nullptr;
        } else {
            current->prev->next = current->next;
            current->next->prev = current->prev;
        }

        delete current;
        size--;
        cout << "Inventory item with batchId " << batchId << " deleted.\n";
        return true;
    }

    InventoryItem* searchByBatchId(int batchId) {
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
        cout << "BatchId not found for quantity update.\n";
        return false;
    }

    void displayForward() const {
        if (head == nullptr) {
            cout << "Inventory list is empty.\n";
            return;
        }

        cout << "\nForward Traversal:\n";
        Node* current = head;
        while (current != nullptr) {
            cout << "ProductId=" << current->data.productId
                 << ", ProductName=" << current->data.productName
                 << ", BatchId=" << current->data.batchId
                 << ", Quantity=" << current->data.quantity
                 << ", Expiry=" << current->data.expiryDate
                 << ", Zone=" << current->data.storageZone << "\n";
            current = current->next;
        }
    }

    void displayBackward() const {
        if (tail == nullptr) {
            cout << "Inventory list is empty.\n";
            return;
        }

        cout << "\nBackward Traversal:\n";
        Node* current = tail;
        while (current != nullptr) {
            cout << "ProductId=" << current->data.productId
                 << ", ProductName=" << current->data.productName
                 << ", BatchId=" << current->data.batchId
                 << ", Quantity=" << current->data.quantity
                 << ", Expiry=" << current->data.expiryDate
                 << ", Zone=" << current->data.storageZone << "\n";
            current = current->prev;
        }
    }

    int countNodes() const {
        return size;
    }

    void printMenu() const {
        cout << "\n=== Inventory Management (Doubly Linked List) ===\n";
        cout << "1. Insert inventory at beginning\n";
        cout << "2. Insert inventory at end\n";
        cout << "3. Delete by batchId\n";
        cout << "4. Search by batchId\n";
        cout << "5. Update quantity\n";
        cout << "6. Display forward\n";
        cout << "7. Display backward\n";
        cout << "8. Count nodes\n";
        cout << "9. Exit\n";
        cout << "Enter choice: ";
    }
};

int main() {
    DoublyLinkedList list;
    int choice;

    cout << "\n========================================================\n";
    cout << "ColdFlow - Inventory Management using Doubly Linked List\n";
    cout << "========================================================\n";

    while (true) {
        list.printMenu();
        cin >> choice;

        if (choice == 9) {
            cout << "Exiting Inventory Management...\n";
            break;
        }

        switch (choice) {
            case 1: {
                InventoryItem item;
                cout << "Enter productId: "; cin >> item.productId;
                cout << "Enter productName: "; cin >> item.productName;
                cout << "Enter batchId: "; cin >> item.batchId;
                cout << "Enter quantity: "; cin >> item.quantity;
                cout << "Enter expiryDate: "; cin >> item.expiryDate;
                cout << "Enter storageZone: "; cin >> item.storageZone;
                list.insertAtBeginning(item);
                break;
            }
            case 2: {
                InventoryItem item;
                cout << "Enter productId: "; cin >> item.productId;
                cout << "Enter productName: "; cin >> item.productName;
                cout << "Enter batchId: "; cin >> item.batchId;
                cout << "Enter quantity: "; cin >> item.quantity;
                cout << "Enter expiryDate: "; cin >> item.expiryDate;
                cout << "Enter storageZone: "; cin >> item.storageZone;
                list.insertAtEnd(item);
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
                InventoryItem* result = list.searchByBatchId(id);
                if (result == nullptr) {
                    cout << "Inventory item not found.\n";
                } else {
                    cout << "Item found: Product=" << result->productName
                         << ", Quantity=" << result->quantity
                         << ", Zone=" << result->storageZone << "\n";
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
                list.displayForward();
                break;
            case 7:
                list.displayBackward();
                break;
            case 8:
                cout << "Total nodes: " << list.countNodes() << "\n";
                break;
            default:
                cout << "Invalid choice. Please select 1 to 9.\n";
                break;
        }
    }

    return 0;
}

/*
Sample Output:
========================================================
ColdFlow - Inventory Management using Doubly Linked List
========================================================
=== Inventory Management (Doubly Linked List) ===
1. Insert inventory at beginning
2. Insert inventory at end
3. Delete by batchId
4. Search by batchId
5. Update quantity
6. Display forward
7. Display backward
8. Count nodes
9. Exit
Enter choice: 1
Enter productId: P100
Enter productName: Cheese
Enter batchId: 41
Enter quantity: 20
Enter expiryDate: 2026-11-12
Enter storageZone: ColdRoom-2
Inventory item inserted at beginning.

Forward Traversal:
ProductId=P100, ProductName=Cheese, BatchId=41, Quantity=20, Expiry=2026-11-12, Zone=ColdRoom-2

Backward Traversal:
ProductId=P100, ProductName=Cheese, BatchId=41, Quantity=20, Expiry=2026-11-12, Zone=ColdRoom-2
*/
