package com.son.ecommerce.service.impl;

import com.son.ecommerce.entity.Order;
import com.son.ecommerce.repository.OrderRepository;
import com.son.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser() != null && o.getUser().getId().equals(userId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(String status) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus().equalsIgnoreCase(status))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Order updateStatus(Long orderId, String newStatus) {
        Order order = findById(orderId);
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        if ("DELIVERED".equalsIgnoreCase(newStatus)) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setPaymentStatus("PAID");
        }

        return orderRepository.save(order);
    }

    @Override
    public Order updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = findById(orderId);
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findAll().stream()
                .filter(o -> !o.getCreatedAt().isBefore(startDate) && !o.getCreatedAt().isAfter(endDate))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> searchOrders(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return orderRepository.findAll().stream()
                .filter(o ->
                    (o.getId() != null && o.getId().toString().contains(lowerKeyword)) ||
                    (o.getUser() != null && o.getUser().getFullName() != null &&
                        o.getUser().getFullName().toLowerCase().contains(lowerKeyword)) ||
                    (o.getUser() != null && o.getUser().getEmail() != null &&
                        o.getUser().getEmail().toLowerCase().contains(lowerKeyword)) ||
                    (o.getShippingPhone() != null && o.getShippingPhone().contains(lowerKeyword)) ||
                    (o.getTrackingNumber() != null && o.getTrackingNumber().toLowerCase().contains(lowerKeyword))
                )
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getOrderStatistics() {
        List<Order> allOrders = orderRepository.findAll();
        Map<String, Long> stats = new HashMap<>();

        stats.put("TOTAL", (long) allOrders.size());
        stats.put("PENDING", allOrders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count());
        stats.put("CONFIRMED", allOrders.stream().filter(o -> "CONFIRMED".equalsIgnoreCase(o.getStatus())).count());
        stats.put("PROCESSING", allOrders.stream().filter(o -> "PROCESSING".equalsIgnoreCase(o.getStatus())).count());
        stats.put("SHIPPED", allOrders.stream().filter(o -> "SHIPPED".equalsIgnoreCase(o.getStatus())).count());
        stats.put("DELIVERED", allOrders.stream().filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus())).count());
        stats.put("CANCELLED", allOrders.stream().filter(o -> "CANCELLED".equalsIgnoreCase(o.getStatus())).count());

        return stats;
    }

    @Override
    public double getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()) || "PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .mapToDouble(Order::getFinalTotal)
                .sum();
    }

    @Override
    public double getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return findByDateRange(startDate, endDate).stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()) || "PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .mapToDouble(Order::getFinalTotal)
                .sum();
    }

    @Override
    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}

