#include <cstdlib>
#include <iomanip>
#include <iostream>
#include <string>
#include <vector>

#include "FEFOPriorityQueue.h"
#include "InventoryRecord.h"
#include "QueueAlert.h"
#include "SearchInventory.h"
#include "SortInventory.h"

namespace {

void printInventory(const std::vector<coldflow::ProductRecord>& inventory) {
    if (inventory.empty()) {
        std::cout << "Inventory is empty.\n";
        return;
    }

    std::cout << "\nInventory Report\n";
    std::cout << "Product ID | Batch ID | Expiry Date | Qty\n";
    std::cout << "--------------------------------------\n";
    for (const auto& item : inventory) {
        std::cout << item.productId << " | " << item.batchId << " | "
                  << item.expiryDate << " | " << item.quantity << "\n";
    }
}

void addSampleData(std::vector<coldflow::ProductRecord>& inventory) {
    inventory = {
        {"P200", "B-200", "2026-12-02", 78},
        {"P105", "B-105", "2026-10-10", 12},
        {"P101", "B-101", "2026-08-22", 50},
        {"P180", "B-180", "2026-09-15", 25},
        {"P101", "B-102", "2026-08-24", 15},
        {"P130", "B-130", "2026-07-30", 0},
        {"P150", "B-150", "2026-11-01", 6},
        {"P200", "B-201", "2026-12-04", 8}
    };
}

void runDemo() {
    coldflow::AlertQueue alertQueue;
    coldflow::FEFOPriorityQueue fefoQueue;
    std::vector<coldflow::ProductRecord> inventory;
    std::vector<std::string> lowStockAlerts;

    addSampleData(inventory);

    alertQueue.enqueue("A1", "Temperature spike detected in Zone A.");
    alertQueue.enqueue("A2", "Cold-chain door left open.");

    fefoQueue.enqueue({"B-130", "P130", "2026-07-30", 0});
    fefoQueue.enqueue({"B-101", "P101", "2026-08-22", 50});
    fefoQueue.enqueue({"B-102", "P101", "2026-08-24", 15});
    fefoQueue.enqueue({"B-105", "P105", "2026-10-10", 12});
    fefoQueue.enqueue({"B-200", "P200", "2026-12-02", 78});
    fefoQueue.enqueue({"B-150", "P150", "2026-11-01", 6});

    std::cout << "====================================\n";
    std::cout << "ColdFlow CO3 & CO4 Demo\n";
    std::cout << "====================================\n\n";

    std::cout << "1) Queue demo\n";
    alertQueue.display();
    std::cout << "\nProcessing first alert: " << alertQueue.dequeue().message << "\n\n";

    std::cout << "2) FEFO dispatch demo\n";
    fefoQueue.display();
    std::cout << "\n";
    fefoQueue.dispatchNext(inventory, 10, lowStockAlerts);
    fefoQueue.dispatchNext(inventory, 10, lowStockAlerts);
    std::cout << "\n";
    printInventory(inventory);

    std::cout << "\n3) Linear search demo\n";
    const int linearIndex = coldflow::linearSearchByProductId(inventory, "P101");
    if (linearIndex >= 0) {
        std::cout << "Linear search found P101 at index " << linearIndex << "\n";
    } else {
        std::cout << "Linear search: P101 not found.\n";
    }

    std::cout << "\n4) Sorting and binary search demo\n";
    auto sortedById = inventory;
    coldflow::sortByProductId(sortedById);
    const int binaryIndex = coldflow::binarySearchByProductId(sortedById, "P101");
    if (binaryIndex >= 0) {
        std::cout << "Binary search found P101 at index " << binaryIndex << " in product-sorted inventory.\n";
    } else {
        std::cout << "Binary search: P101 not found in product-sorted inventory.\n";
    }

    auto sortedByDate = inventory;
    coldflow::sortByExpiryDate(sortedByDate);
    std::cout << "\nExpiry-sorted inventory:\n";
    for (const auto& item : sortedByDate) {
        std::cout << item.productId << " | " << item.expiryDate << " | " << item.quantity << "\n";
    }

    auto sortedByQty = inventory;
    coldflow::sortByQuantity(sortedByQty);
    std::cout << "\nQuantity-sorted inventory:\n";
    for (const auto& item : sortedByQty) {
        std::cout << item.productId << " | " << item.quantity << "\n";
    }

    std::cout << "\nLow stock alerts generated:\n";
    if (lowStockAlerts.empty()) {
        std::cout << "No low-stock alerts\n";
    } else {
        for (const auto& message : lowStockAlerts) {
            std::cout << "  - " << message << "\n";
        }
    }
}

}  // namespace

int main() {
    runDemo();
    return 0;
}
