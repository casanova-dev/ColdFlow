#include <iostream>
#include <string>
using namespace std;

// ===============================================================
// Zaki - Circular Linked List for Warehouse Zone Monitoring
// ColdFlow CO2: Choose suitable type of linked list for applications
// ===============================================================
// Why circular linked list?
// Warehouse zone monitoring repeatedly visits each zone in a loop.
// A circular linked list allows easy round-robin traversal without
// ending the list after the last node, which is perfect for monitoring.
// Time complexity:
//   Insert: O(n) if searching by position, or O(1) for end insertion
//   Delete by zoneId: O(n)
//   Search by zoneId: O(n)
//   Update temperature: O(n)
//   Circular traversal: O(n)
// ===============================================================

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
        if (head == nullptr) {
            return;
        }

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

    bool existsByZoneId(int zoneId) const {
        if (head == nullptr) return false;

        Node* current = head;
        do {
            if (current->data.zoneId == zoneId) return true;
            current = current->next;
        } while (current != head);
        return false;
    }

    void insertAtEnd(const Zone& zone) {
        if (existsByZoneId(zone.zoneId)) {
            cout << "Duplicate zoneId. Zone already exists.\n";
            return;
        }

        Node* newNode = new Node(zone);

        if (head == nullptr) {
            head = newNode;
            newNode->next = head;
        } else {
            Node* current = head;
            while (current->next != head) {
                current = current->next;
            }
            current->next = newNode;
            newNode->next = head;
        }

        size++;
        cout << "Zone inserted successfully.\n";
    }

    bool deleteByZoneId(int zoneId) {
        if (head == nullptr) {
            cout << "No zones to delete.\n";
            return false;
        }

        Node* current = head;
        Node* previous = nullptr;

        do {
            if (current->data.zoneId == zoneId) {
                if (size == 1) {
                    delete current;
                    head = nullptr;
                    size = 0;
                    cout << "Only zone deleted. List is empty.\n";
                    return true;
                }

                if (previous == nullptr) {
                    Node* last = head;
                    while (last->next != head) {
                        last = last->next;
                    }
                    head = head->next;
                    last->next = head;
                } else {
                    previous->next = current->next;
                }

                delete current;
                size--;
                cout << "Zone " << zoneId << " deleted successfully.\n";
                return true;
            }

            previous = current;
            current = current->next;
        } while (current != head);

        cout << "Zone " << zoneId << " not found.\n";
        return false;
    }

    Zone* searchByZoneId(int zoneId) {
        if (head == nullptr) return nullptr;

        Node* current = head;
        do {
            if (current->data.zoneId == zoneId) {
                return &(current->data);
            }
            current = current->next;
        } while (current != head);

        return nullptr;
    }

    bool updateTemperature(int zoneId, double newTemperature) {
        Node* current = head;
        if (current == nullptr) {
            cout << "Zone list is empty.\n";
            return false;
        }

        do {
            if (current->data.zoneId == zoneId) {
                current->data.currentTemperature = newTemperature;
                if (newTemperature >= current->data.minimumTemperature &&
                    newTemperature <= current->data.maximumTemperature) {
                    current->data.zoneStatus = "NORMAL";
                } else {
                    current->data.zoneStatus = "ALERT";
                }
                cout << "Temperature updated successfully.\n";
                return true;
            }
            current = current->next;
        } while (current != head);

        cout << "Zone not found.\n";
        return false;
    }

    void displayAll() const {
        if (head == nullptr) {
            cout << "No zones in the warehouse.\n";
            return;
        }

        cout << "\nWarehouse Zone Status:\n";
        Node* current = head;
        do {
            cout << "ZoneId=" << current->data.zoneId
                 << ", ZoneName=" << current->data.zoneName
                 << ", Temp=" << current->data.currentTemperature
                 << ", Status=" << current->data.zoneStatus << "\n";
            current = current->next;
        } while (current != head);
    }

    void monitorZones() {
        if (head == nullptr) {
            cout << "No zones available for monitoring.\n";
            return;
        }

        cout << "\nMonitoring all zones in circular order...\n";
        Node* current = head;
        do {
            if (current->data.currentTemperature >= current->data.minimumTemperature &&
                current->data.currentTemperature <= current->data.maximumTemperature) {
                current->data.zoneStatus = "NORMAL";
            } else {
                current->data.zoneStatus = "ALERT";
            }

            cout << "Zone " << current->data.zoneId << " (" << current->data.zoneName
                 << ") -> " << current->data.zoneStatus << "\n";
            current = current->next;
        } while (current != head);
    }

    int countNodes() const {
        return size;
    }

    void printMenu() const {
        cout << "\n=== Zone Monitoring (Circular Linked List) ===\n";
        cout << "1. Insert zone\n";
        cout << "2. Delete zone by zoneId\n";
        cout << "3. Search zone by zoneId\n";
        cout << "4. Update temperature\n";
        cout << "5. Display all zones\n";
        cout << "6. Monitor all zones\n";
        cout << "7. Count nodes\n";
        cout << "8. Exit\n";
        cout << "Enter choice: ";
    }
};

int main() {
    CircularLinkedList list;
    int choice;

    cout << "\n========================================================\n";
    cout << "ColdFlow - Warehouse Zone Monitoring using Circular Linked List\n";
    cout << "========================================================\n";

    while (true) {
        list.printMenu();
        cin >> choice;

        if (choice == 8) {
            cout << "Exiting Zone Monitoring...\n";
            break;
        }

        switch (choice) {
            case 1: {
                Zone zone;
                cout << "Enter zoneId: "; cin >> zone.zoneId;
                cout << "Enter zoneName: "; cin >> zone.zoneName;
                cout << "Enter minimumTemperature: "; cin >> zone.minimumTemperature;
                cout << "Enter maximumTemperature: "; cin >> zone.maximumTemperature;
                cout << "Enter currentTemperature: "; cin >> zone.currentTemperature;
                zone.zoneStatus = (zone.currentTemperature >= zone.minimumTemperature &&
                                   zone.currentTemperature <= zone.maximumTemperature)
                                      ? "NORMAL" : "ALERT";
                list.insertAtEnd(zone);
                break;
            }
            case 2: {
                int id;
                cout << "Enter zoneId to delete: "; cin >> id;
                list.deleteByZoneId(id);
                break;
            }
            case 3: {
                int id;
                cout << "Enter zoneId to search: "; cin >> id;
                Zone* result = list.searchByZoneId(id);
                if (result == nullptr) {
                    cout << "Zone not found.\n";
                } else {
                    cout << "Zone found: " << result->zoneName
                         << ", Current Temp=" << result->currentTemperature
                         << ", Status=" << result->zoneStatus << "\n";
                }
                break;
            }
            case 4: {
                int id;
                double temp;
                cout << "Enter zoneId: "; cin >> id;
                cout << "Enter new currentTemperature: "; cin >> temp;
                list.updateTemperature(id, temp);
                break;
            }
            case 5:
                list.displayAll();
                break;
            case 6:
                list.monitorZones();
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
ColdFlow - Warehouse Zone Monitoring using Circular Linked List
========================================================
=== Zone Monitoring (Circular Linked List) ===
1. Insert zone
2. Delete zone by zoneId
3. Search zone by zoneId
4. Update temperature
5. Display all zones
6. Monitor all zones
7. Count nodes
8. Exit
Enter choice: 1
Enter zoneId: 1
Enter zoneName: ColdRoomA
Enter minimumTemperature: 2
Enter maximumTemperature: 8
Enter currentTemperature: 6
Zone inserted successfully.

Monitoring all zones in circular order...
Zone 1 (ColdRoomA) -> NORMAL
*/
