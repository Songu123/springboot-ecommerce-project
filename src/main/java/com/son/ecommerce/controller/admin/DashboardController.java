package com.son.ecommerce.controller.admin;

import com.son.ecommerce.service.CategoryService;
import com.son.ecommerce.service.ProductService;
import com.son.ecommerce.service.UserService;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.OrderItemService;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.entity.OrderItem;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    public DashboardController(UserService userService, ProductService productService, CategoryService categoryService, OrderService orderService, OrderItemService orderItemService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get statistics
        long totalUsers = userService.findAll().size();
        long totalProducts = productService.findAll().size();
        long totalCategories = categoryService.findAll().size();
        long totalOrders = orderService.findAll().size();
        double totalRevenue = orderService.getTotalRevenue();

        // Calculate enabled users
        long enabledUsers = userService.findAll().stream()
                .filter(com.son.ecommerce.entity.User::isEnabled)
                .count();

        // Sản phẩm bán chạy (top 5 theo quantity)
        List<OrderItem> allOrderItems = orderItemService.findAll();
        Map<Product, Integer> productSales = new HashMap<>();
        for (OrderItem item : allOrderItems) {
            Product p = item.getProduct();
            if (p != null) {
                productSales.put(p, productSales.getOrDefault(p, 0) + item.getQuantity());
            }
        }
        List<Map.Entry<Product, Integer>> bestSellers = productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        // Add to model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("enabledUsers", enabledUsers);
        model.addAttribute("bestSellers", bestSellers);

        // Recent data for tables
        model.addAttribute("recentProducts", productService.findAll().stream().limit(5).toList());
        model.addAttribute("recentUsers", userService.findAll().stream().limit(5).toList());

        model.addAttribute("content", "admin/dashboard");
        return "admin-layout";
    }

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }
}

