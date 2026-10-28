#include "SortInventory.h"

#include <algorithm>

namespace coldflow {

void sortByProductId(std::vector<ProductRecord>& inventory) {
    std::sort(inventory.begin(), inventory.end(), [](const ProductRecord& left, const ProductRecord& right) {
        return left.productId < right.productId;
    });
}

void sortByExpiryDate(std::vector<ProductRecord>& inventory) {
    std::sort(inventory.begin(), inventory.end(), [](const ProductRecord& left, const ProductRecord& right) {
        if (left.expiryDate == right.expiryDate) {
            return left.productId < right.productId;
        }
        return left.expiryDate < right.expiryDate;
    });
}

void sortByQuantity(std::vector<ProductRecord>& inventory) {
    std::sort(inventory.begin(), inventory.end(), [](const ProductRecord& left, const ProductRecord& right) {
        if (left.quantity == right.quantity) {
            return left.productId < right.productId;
        }
        return left.quantity < right.quantity;
    });
}

}  // namespace coldflow
