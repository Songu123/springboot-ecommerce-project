package com.son.ecommerce.controller.admin;

import com.son.ecommerce.entity.Order;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.ProductService;
import com.son.ecommerce.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    public ReportController(OrderService orderService, ProductService productService, UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    public String reports(Model model) {
        // Thống kê tổng quát
        long totalOrders = orderService.findAll().size();
        long totalProducts = productService.findAll().size();
        long totalUsers = userService.findAll().size();
        double totalRevenue = orderService.getTotalRevenue();

        // Đơn hàng theo trạng thái
        List<Order> allOrders = orderService.findAll();
        Map<String, Long> orderStatuses = new TreeMap<>();
        orderStatuses.put("PENDING", allOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
        orderStatuses.put("CONFIRMED", allOrders.stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count());
        orderStatuses.put("SHIPPED", allOrders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count());
        orderStatuses.put("DELIVERED", allOrders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count());
        orderStatuses.put("CANCELLED", allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count());

        // Doanh thu theo tháng
        Map<YearMonth, Double> monthlyRevenue = new TreeMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            monthlyRevenue.put(ym, 0.0);
        }

        for (Order order : allOrders) {
            if (order.getCreatedAt() != null) {
                YearMonth ym = YearMonth.from(order.getCreatedAt());
                if (monthlyRevenue.containsKey(ym)) {
                    monthlyRevenue.put(ym, monthlyRevenue.get(ym) + order.getTotalPrice());
                }
            }
        }

        // Top sản phẩm bán chạy
        Map<Product, Integer> productSales = new HashMap<>();
        for (Order order : allOrders) {
            if (order.getItems() != null) {
                order.getItems().forEach(item -> {
                    Product p = item.getProduct();
                    if (p != null) {
                        productSales.put(p, productSales.getOrDefault(p, 0) + item.getQuantity());
                    }
                });
            }
        }

        List<Map.Entry<Product, Integer>> topProducts = productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        // Top khách hàng
        Map<User, Double> customerSpending = new HashMap<>();
        for (Order order : allOrders) {
            User user = order.getUser();
            if (user != null) {
                customerSpending.put(user, customerSpending.getOrDefault(user, 0.0) + order.getTotalPrice());
            }
        }

        List<Map.Entry<User, Double>> topCustomers = customerSpending.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        // Tăng trưởng người dùng theo tháng
        Map<YearMonth, Long> userGrowth = new TreeMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            userGrowth.put(ym, 0L);
        }

        for (User user : userService.findAll()) {
            if (user.getCreatedAt() != null) {
                YearMonth ym = YearMonth.from(user.getCreatedAt());
                if (userGrowth.containsKey(ym)) {
                    userGrowth.put(ym, userGrowth.get(ym) + 1);
                }
            }
        }

        // Thêm vào model
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("orderStatuses", orderStatuses);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("topCustomers", topCustomers);
        model.addAttribute("userGrowth", userGrowth);

        model.addAttribute("content", "admin/report/list");
        return "admin-layout";
    }
}

