#ifndef FEFOPRIORITYQUEUE_H
#define FEFOPRIORITYQUEUE_H

#include <queue>
#include <string>
#include <vector>

#include "InventoryRecord.h"

namespace coldflow {

struct BatchDispatch {
    std::string batchId;
    std::string productId;
    std::string expiryDate;
    int quantity = 0;
};

struct BatchDispatchCompare {
    bool operator()(const BatchDispatch& left, const BatchDispatch& right) const {
        if (left.expiryDate == right.expiryDate) {
            return left.batchId > right.batchId;
        }
        return left.expiryDate > right.expiryDate;
    }
};

class FEFOPriorityQueue {
public:
    void enqueue(const BatchDispatch& batch);
    bool empty() const;
    std::size_t size() const;
    BatchDispatch top() const;
    void pop();
    void display() const;
    bool dispatchNext(std::vector<ProductRecord>& inventory, int lowStockThreshold, std::vector<std::string>& lowStockAlerts);

private:
    std::priority_queue<BatchDispatch, std::vector<BatchDispatch>, BatchDispatchCompare> queue_;
};

}  // namespace coldflow

#endif  // FEFOPRIORITYQUEUE_H
