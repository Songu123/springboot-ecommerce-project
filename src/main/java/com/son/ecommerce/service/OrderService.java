package com.son.ecommerce.service;

import com.son.ecommerce.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    List<Order> findAll();
    Order findById(Long id);
    Order save(Order order);
    void deleteById(Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);

    // Advanced methods
    Order updateStatus(Long orderId, String newStatus);
    Order updatePaymentStatus(Long orderId, String paymentStatus);
    List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> searchOrders(String keyword);
    Map<String, Long> getOrderStatistics();
    double getTotalRevenue();
    double getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> getRecentOrders(int limit);
}

