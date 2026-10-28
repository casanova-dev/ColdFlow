#ifndef SEARCH_INVENTORY_H
#define SEARCH_INVENTORY_H

#include <string>
#include <vector>

#include "InventoryRecord.h"

namespace coldflow {

int linearSearchByProductId(const std::vector<ProductRecord>& inventory, const std::string& productId);
int binarySearchByProductId(const std::vector<ProductRecord>& sortedInventory, const std::string& productId);

}  // namespace coldflow

#endif  // SEARCH_INVENTORY_H
