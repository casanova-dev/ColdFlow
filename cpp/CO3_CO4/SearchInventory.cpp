#include "SearchInventory.h"

#include <algorithm>

namespace coldflow {

int linearSearchByProductId(const std::vector<ProductRecord>& inventory, const std::string& productId) {
    for (std::size_t i = 0; i < inventory.size(); ++i) {
        if (inventory[i].productId == productId) {
            return static_cast<int>(i);
        }
    }
    return -1;
}

int binarySearchByProductId(const std::vector<ProductRecord>& sortedInventory, const std::string& productId) {
    int left = 0;
    int right = static_cast<int>(sortedInventory.size()) - 1;

    while (left <= right) {
        const int mid = left + (right - left) / 2;
        if (sortedInventory[mid].productId == productId) {
            return mid;
        }
        if (sortedInventory[mid].productId < productId) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return -1;
}

}  // namespace coldflow
