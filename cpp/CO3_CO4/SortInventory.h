#ifndef SORT_INVENTORY_H
#define SORT_INVENTORY_H

#include <vector>

#include "InventoryRecord.h"

namespace coldflow {

void sortByProductId(std::vector<ProductRecord>& inventory);
void sortByExpiryDate(std::vector<ProductRecord>& inventory);
void sortByQuantity(std::vector<ProductRecord>& inventory);

}  // namespace coldflow

#endif  // SORT_INVENTORY_H
