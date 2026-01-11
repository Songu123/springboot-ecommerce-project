package com.son.ecommerce.service;

import com.son.ecommerce.entity.Order;
import java.util.List;

public interface OrderService {
    List<Order> findAll();
    Order findById(Long id);
    Order save(Order order);
    void deleteById(Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
}

