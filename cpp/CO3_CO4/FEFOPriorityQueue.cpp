#include "FEFOPriorityQueue.h"

#include <algorithm>
#include <iostream>

namespace coldflow {

void FEFOPriorityQueue::enqueue(const BatchDispatch& batch) {
    queue_.push(batch);
}

bool FEFOPriorityQueue::empty() const {
    return queue_.empty();
}

std::size_t FEFOPriorityQueue::size() const {
    return queue_.size();
}

BatchDispatch FEFOPriorityQueue::top() const {
    return queue_.top();
}

void FEFOPriorityQueue::pop() {
    if (!queue_.empty()) {
        queue_.pop();
    }
}

void FEFOPriorityQueue::display() const {
    if (queue_.empty()) {
        std::cout << "No batches in the FEFO dispatch queue.\n";
        return;
    }

    std::vector<BatchDispatch> batches;
    auto copy = queue_;
    while (!copy.empty()) {
        batches.push_back(copy.top());
        copy.pop();
    }

    std::sort(batches.begin(), batches.end(), [](const BatchDispatch& left, const BatchDispatch& right) {
        if (left.expiryDate == right.expiryDate) {
            return left.batchId < right.batchId;
        }
        return left.expiryDate < right.expiryDate;
    });

    std::cout << "FEFO dispatch queue (earliest expiry first):\n";
    for (std::size_t i = 0; i < batches.size(); ++i) {
        const auto& batch = batches[i];
        std::cout << "  [" << i + 1 << "] Batch " << batch.batchId << " | Product " << batch.productId
                  << " | Expiry " << batch.expiryDate << " | Qty " << batch.quantity << "\n";
    }
}

bool FEFOPriorityQueue::dispatchNext(std::vector<ProductRecord>& inventory, int lowStockThreshold, std::vector<std::string>& lowStockAlerts) {
    if (queue_.empty()) {
        return false;
    }

    BatchDispatch batch = queue_.top();
    queue_.pop();

    auto it = std::find_if(inventory.begin(), inventory.end(), [&](const ProductRecord& record) {
        return record.productId == batch.productId;
    });

    if (it == inventory.end()) {
        std::cout << "Warning: Product " << batch.productId << " not found in inventory.\n";
        return true;
    }

    if (it->quantity >= batch.quantity) {
        it->quantity -= batch.quantity;
    } else {
        it->quantity = 0;
    }

    std::cout << "Dispatched batch " << batch.batchId << " for product " << batch.productId
              << " (expiry: " << batch.expiryDate << ")\n";
    std::cout << "Updated stock for product " << it->productId << " -> " << it->quantity << "\n";

    if (it->quantity <= lowStockThreshold) {
        const std::string message = buildLowStockMessage(it->productId, it->quantity);
        lowStockAlerts.push_back(message);
        std::cout << "  -> " << message << "\n";
    }

    return true;
}

}  // namespace coldflow
