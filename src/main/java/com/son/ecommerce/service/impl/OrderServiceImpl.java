package com.son.ecommerce.service.impl;

import com.son.ecommerce.entity.Order;
import com.son.ecommerce.entity.OrderItem;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.repository.OrderRepository;
import com.son.ecommerce.service.OrderService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final EntityManager entityManager;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAllEager();
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findByIdEager(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + id));
    }

    @Override
    public Order save(Order order) {
        // ✅ CÁCH CHUẨN: Lấy productId từ JSON Android (trực tiếp từ item)

        // 1. Set timestamps
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());

        System.out.println("💾 [OrderService] Saving Order:");
        System.out.println("   - User: " + (order.getUser() != null ? order.getUser().getId() + " (" + order.getUser().getUsername() + ")" : "NULL"));
        System.out.println("   - Total Price: " + order.getTotalPrice());
        System.out.println("   - Items count: " + (order.getItems() != null ? order.getItems().size() : 0));

        // 2. Process OrderItems - Get productId từ JSON, không chỉ từ product object
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            System.out.println("🔄 [OrderService] Processing " + order.getItems().size() + " OrderItems...");

            List<OrderItem> processedItems = new ArrayList<>();

            for (OrderItem cartItem : order.getItems()) {
                // ✅ Create NEW OrderItem (clear ID để nó auto-generate)
                OrderItem newItem = new OrderItem();
                newItem.setQuantity(cartItem.getQuantity());
                newItem.setPrice(cartItem.getPrice());

                System.out.println("✅ [OrderService] Processing OrderItem:");
                System.out.println("   - Qty from request: " + cartItem.getQuantity());
                System.out.println("   - Price from request: " + cartItem.getPrice());

                // ✅ Get productId - PRIORITY: Use cartItem.getProductId() if available (from Android JSON)
                Long productId = null;

                // Cách 1: Lấy từ cartItem.productId (Android gửi trực tiếp)
                if (cartItem.getProductId() != null && cartItem.getProductId() > 0) {
                    productId = cartItem.getProductId().longValue();
                    System.out.println("   - Got productId from item.productId: " + productId);
                }
                // Cách 2: Nếu không có, lấy từ product.id
                else if (cartItem.getProduct() != null && cartItem.getProduct().getId() != null) {
                    productId = cartItem.getProduct().getId();
                    System.out.println("   - Got productId from product.id: " + productId);
                }

                // ✅ Load Product từ DB nếu có productId
                if (productId != null && productId > 0) {
                    try {
                        Product managedProduct = entityManager.getReference(Product.class, productId);
                        newItem.setProduct(managedProduct);
                        System.out.println("   - ✅ Product loaded - ID: " + productId);
                    } catch (Exception e) {
                        System.err.println("   - ❌ Error loading Product ID " + productId + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("   - ❌ WARNING: No productId found in request!");
                }

                // ✅ Set Order reference
                newItem.setOrder(order);
                processedItems.add(newItem);

                System.out.println("   - Final OrderItem: OrderID=" + (newItem.getOrder() != null ? newItem.getOrder().getId() : "null") +
                    ", ProductID=" + (newItem.getProduct() != null ? newItem.getProduct().getId() : "NULL") +
                    ", Qty=" + newItem.getQuantity() + ", Price=" + newItem.getPrice());
            }

            // Set processed items
            order.setItems(processedItems);
        }

        // 3. Save Order - cascade handles OrderItems
        System.out.println("💾 [OrderService] Saving Order to database...");
        Order savedOrder = orderRepository.save(order);
        System.out.println("✅ [OrderService] Order saved successfully!");
        System.out.println("   - Order ID: " + savedOrder.getId());
        System.out.println("   - User ID: " + (savedOrder.getUser() != null ? savedOrder.getUser().getId() : "NULL"));
        System.out.println("   - Items saved: " + (savedOrder.getItems() != null ? savedOrder.getItems().size() : 0));

        // Log all saved items - verify product_id is saved
        if (savedOrder.getItems() != null) {
            for (OrderItem item : savedOrder.getItems()) {
                System.out.println("   - OrderItem [ID:" + item.getId() +
                    ", OrderID:" + item.getOrder().getId() +
                    ", ProductID:" + (item.getProduct() != null ? item.getProduct().getId() : "NULL ⚠️") +
                    ", Qty:" + item.getQuantity() +
                    ", Price:" + item.getPrice() + "]");
            }
        }

        return savedOrder;
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserIdEager(userId);
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
        return orderRepository.findAllEager().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}

