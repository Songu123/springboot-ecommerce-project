package com.son.ecommerce.controller.admin;

import com.son.ecommerce.service.CategoryService;
import com.son.ecommerce.service.ProductService;
import com.son.ecommerce.service.UserService;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.OrderItemService;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.entity.OrderItem;
import com.son.ecommerce.entity.Order;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

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

        // Recent orders (last 5)
        List<com.son.ecommerce.entity.Order> recentOrders = orderService.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList();
        model.addAttribute("recentOrders", recentOrders);

        // Order status statistics
        List<com.son.ecommerce.entity.Order> allOrders = orderService.findAll();
        long pendingCount = allOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
        long confirmedCount = allOrders.stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count();
        long shippedCount = allOrders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count();
        long deliveredCount = allOrders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count();
        long cancelledCount = allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippedCount", shippedCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);

        model.addAttribute("content", "admin/dashboard");
        return "admin-layout";
    }

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/api/revenue")
    @ResponseBody
    public Map<String, Object> getMonthlyRevenue() {
        List<Order> allOrders = orderService.findAll();
        Map<YearMonth, Double> monthlyRevenue = new TreeMap<>();

        // Lấy 6 tháng gần nhất
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            monthlyRevenue.put(ym, 0.0);
        }

        // Tính tổng doanh thu theo tháng
        for (Order order : allOrders) {
            if (order.getCreatedAt() != null) {
                YearMonth ym = YearMonth.from(order.getCreatedAt());
                if (monthlyRevenue.containsKey(ym)) {
                    monthlyRevenue.put(ym, monthlyRevenue.get(ym) + order.getTotalPrice());
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        for (Map.Entry<YearMonth, Double> entry : monthlyRevenue.entrySet()) {
            labels.add(String.format("Tháng %d", entry.getKey().getMonthValue()));
            data.add(entry.getValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }
}
