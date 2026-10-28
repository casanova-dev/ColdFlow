#include "InventoryRecord.h"

#include <cctype>
#include <sstream>

namespace coldflow {

bool isValidDateFormat(const std::string& date) {
    if (date.size() != 10) {
        return false;
    }

    if (date[4] != '-' || date[7] != '-') {
        return false;
    }

    for (std::size_t i = 0; i < date.size(); ++i) {
        if (i == 4 || i == 7) {
            continue;
        }
        if (!std::isdigit(static_cast<unsigned char>(date[i]))) {
            return false;
        }
    }

    const int year = std::stoi(date.substr(0, 4));
    const int month = std::stoi(date.substr(5, 2));
    const int day = std::stoi(date.substr(8, 2));

    if (month < 1 || month > 12) {
        return false;
    }

    if (day < 1 || day > 31) {
        return false;
    }

    if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
        return false;
    }

    if (month == 2) {
        const bool leap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
        const int maxDay = leap ? 29 : 28;
        if (day > maxDay) {
            return false;
        }
    }

    return true;
}

std::string buildLowStockMessage(const std::string& productId, int quantity) {
    std::ostringstream oss;
    oss << "Low stock alert: Product " << productId << " quantity is now " << quantity << ".";
    return oss.str();
}

}  // namespace coldflow
