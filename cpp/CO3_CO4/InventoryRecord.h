#ifndef INVENTORY_RECORD_H
#define INVENTORY_RECORD_H

#include <string>
#include <vector>

namespace coldflow {

struct ProductRecord {
    std::string productId;
    std::string batchId;
    std::string expiryDate;
    int quantity = 0;
};

bool isValidDateFormat(const std::string& date);
std::string buildLowStockMessage(const std::string& productId, int quantity);

}  // namespace coldflow

#endif  // INVENTORY_RECORD_H
