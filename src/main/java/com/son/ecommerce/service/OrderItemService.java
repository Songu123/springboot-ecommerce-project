package com.son.ecommerce.service;

import com.son.ecommerce.entity.OrderItem;
import java.util.List;

public interface OrderItemService {
    List<OrderItem> findAll();
    OrderItem findById(Long id);
    OrderItem save(OrderItem orderItem);
    void deleteById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
}

