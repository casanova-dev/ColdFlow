#include "QueueAlert.h"

#include <iostream>

namespace coldflow {

void AlertQueue::enqueue(const std::string& alertId, const std::string& message) {
    alerts_.push_back({alertId, message});
}

bool AlertQueue::empty() const {
    return alerts_.empty();
}

std::size_t AlertQueue::size() const {
    return alerts_.size();
}

void AlertQueue::display() const {
    if (alerts_.empty()) {
        std::cout << "No alerts pending.\n";
        return;
    }

    std::cout << "Pending alerts:\n";
    for (std::size_t i = 0; i < alerts_.size(); ++i) {
        std::cout << "  [" << i + 1 << "] " << alerts_[i].alertId << " - " << alerts_[i].message << "\n";
    }
}

AlertQueue::Alert AlertQueue::dequeue() {
    if (alerts_.empty()) {
        return {"", "No alert available."};
    }

    Alert alert = alerts_.front();
    alerts_.pop_front();
    return alert;
}

}  // namespace coldflow
