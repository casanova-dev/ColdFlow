#ifndef QUEUE_ALERT_H
#define QUEUE_ALERT_H

#include <deque>
#include <string>

namespace coldflow {

class AlertQueue {
public:
    struct Alert {
        std::string alertId;
        std::string message;
    };

    void enqueue(const std::string& alertId, const std::string& message);
    bool empty() const;
    std::size_t size() const;
    void display() const;
    Alert dequeue();

private:
    std::deque<Alert> alerts_;
};

}  // namespace coldflow

#endif  // QUEUE_ALERT_H
