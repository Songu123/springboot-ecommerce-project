package com.son.ecommerce.controller;

import com.son.ecommerce.entity.Order;
import com.son.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        try {
            Order order = orderService.findById(id);
            model.addAttribute("order", order);
            model.addAttribute("content", "order-confirmation");
            return "layout/main";
        } catch (Exception e) {
            return "redirect:/cart?error=Order not found";
        }
    }
}

