package com.son.ecommerce.repository;


import com.son.ecommerce.entity.Order;
import com.son.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
