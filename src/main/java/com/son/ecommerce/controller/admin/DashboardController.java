package com.son.ecommerce.controller.admin;

import com.son.ecommerce.service.CategoryService;
import com.son.ecommerce.service.ProductService;
import com.son.ecommerce.service.UserService;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.OrderItemService;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.entity.OrderItem;
import com.son.ecommerce.entity.Order;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    public DashboardController(UserService userService,
                               ProductService productService,
                               CategoryService categoryService,
                               OrderService orderService,
                               OrderItemService orderItemService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        List<Order> allOrders = orderService.findAll();

        // ── KPI ───────────────────────────────────────────────────
        model.addAttribute("totalUsers",      userService.findAll().size());
        model.addAttribute("totalProducts",   productService.findAll().size());
        model.addAttribute("totalCategories", categoryService.findAll().size());
        model.addAttribute("totalOrders",     allOrders.size());
        model.addAttribute("totalRevenue",    orderService.getTotalRevenue());
        model.addAttribute("enabledUsers",    userService.findAll().stream()
                .filter(com.son.ecommerce.entity.User::isEnabled).count());

        // ── Order status map (dùng cho chart doughnut) ────────────
        Map<String, Long> orderStatuses = new TreeMap<>();
        orderStatuses.put("PENDING",   allOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
        orderStatuses.put("CONFIRMED", allOrders.stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count());
        orderStatuses.put("SHIPPED",   allOrders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count());
        orderStatuses.put("DELIVERED", allOrders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count());
        orderStatuses.put("CANCELLED", allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count());
        model.addAttribute("orderStatuses", orderStatuses);

        // Giữ lại các biến riêng lẻ nếu view khác cần
        model.addAttribute("pendingCount",   orderStatuses.get("PENDING"));
        model.addAttribute("confirmedCount", orderStatuses.get("CONFIRMED"));
        model.addAttribute("shippedCount",   orderStatuses.get("SHIPPED"));
        model.addAttribute("deliveredCount", orderStatuses.get("DELIVERED"));
        model.addAttribute("cancelledCount", orderStatuses.get("CANCELLED"));

        // ── Monthly revenue map (dùng cho line chart) ─────────────
        Map<YearMonth, Double> monthlyRevenue = new TreeMap<>();
        for (int i = 5; i >= 0; i--) {
            monthlyRevenue.put(YearMonth.now().minusMonths(i), 0.0);
        }
        for (Order order : allOrders) {
            if (order.getCreatedAt() != null) {
                YearMonth ym = YearMonth.from(order.getCreatedAt());
                if (monthlyRevenue.containsKey(ym)) {
                    monthlyRevenue.merge(ym, order.getTotalPrice(), Double::sum);
                }
            }
        }
        model.addAttribute("monthlyRevenue", monthlyRevenue);

        // ── Best sellers (top 5) ───────────────────────────────────
        Map<Product, Integer> productSales = new HashMap<>();
        for (OrderItem item : orderItemService.findAll()) {
            Product p = item.getProduct();
            if (p != null) {
                productSales.merge(p, item.getQuantity(), Integer::sum);
            }
        }
        List<Map.Entry<Product, Integer>> bestSellers = productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("bestSellers", bestSellers);

        // ── Recent data ────────────────────────────────────────────
        model.addAttribute("recentProducts", productService.findAll().stream().limit(5).toList());
        model.addAttribute("recentUsers",    userService.findAll().stream().limit(5).toList());

        List<Order> recentOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList();
        model.addAttribute("recentOrders", recentOrders);

        model.addAttribute("content", "admin/dashboard");
        return "admin-layout";
    }

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    // ── API endpoint cho revenue chart (giữ nguyên) ───────────────
    @GetMapping("/api/revenue")
    @ResponseBody
    public Map<String, Object> getMonthlyRevenue() {
        List<Order> allOrders = orderService.findAll();
        Map<YearMonth, Double> monthlyRevenue = new TreeMap<>();

        for (int i = 5; i >= 0; i--) {
            monthlyRevenue.put(YearMonth.now().minusMonths(i), 0.0);
        }
        for (Order order : allOrders) {
            if (order.getCreatedAt() != null) {
                YearMonth ym = YearMonth.from(order.getCreatedAt());
                if (monthlyRevenue.containsKey(ym)) {
                    monthlyRevenue.merge(ym, order.getTotalPrice(), Double::sum);
                }
            }
        }

        List<String> labels = new ArrayList<>();
        List<Double> data   = new ArrayList<>();
        for (Map.Entry<YearMonth, Double> entry : monthlyRevenue.entrySet()) {
            labels.add("Tháng " + entry.getKey().getMonthValue());
            data.add(entry.getValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data",   data);
        return result;
    }
}