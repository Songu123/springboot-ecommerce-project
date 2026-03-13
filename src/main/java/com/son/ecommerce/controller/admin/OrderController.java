package com.son.ecommerce.controller.admin;

import com.son.ecommerce.entity.Order;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * List all orders with optional filters
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Model model) {

        java.util.List<Order> orders;

        if (search != null && !search.trim().isEmpty()) {
            orders = orderService.searchOrders(search);
        } else if (status != null && !status.isEmpty()) {
            orders = orderService.findByStatus(status);
        } else if (startDate != null && endDate != null) {
            orders = orderService.findByDateRange(startDate, endDate);
        } else {
            orders = orderService.findAll();
        }

        // Get statistics
        Map<String, Long> statistics = orderService.getOrderStatistics();
        double totalRevenue = orderService.getTotalRevenue();

        model.addAttribute("orders", orders);
        model.addAttribute("statistics", statistics);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("content", "admin/order/list");

        return "admin-layout";
    }

    /**
     * View order details
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            Order order = orderService.findById(id);
            model.addAttribute("order", order);
            model.addAttribute("content", "admin/order/view");
            return "admin-layout";
        } catch (RuntimeException e) {
            return "redirect:/admin/orders?error=" + e.getMessage();
        }
    }

    /**
     * Show edit order form
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            Order order = orderService.findById(id);
            model.addAttribute("order", order);
            model.addAttribute("allUsers", userService.findAll());
            model.addAttribute("content", "admin/order/edit");
            return "admin-layout";
        } catch (RuntimeException e) {
            return "redirect:/admin/orders?error=" + e.getMessage();
        }
    }

    /**
     * Update order
     */
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String paymentStatus,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String shippingCity,
            @RequestParam(required = false) String shippingPostalCode,
            @RequestParam(required = false) String shippingPhone,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String note,
            @RequestParam(defaultValue = "0") double shippingFee,
            @RequestParam(defaultValue = "0") double discount,
            RedirectAttributes redirectAttributes) {

        try {
            Order order = orderService.findById(id);

            order.setStatus(status);
            order.setPaymentStatus(paymentStatus);
            order.setPaymentMethod(paymentMethod);
            order.setShippingAddress(shippingAddress);
            order.setShippingCity(shippingCity);
            order.setShippingPostalCode(shippingPostalCode);
            order.setShippingPhone(shippingPhone);
            order.setTrackingNumber(trackingNumber);
            order.setNote(note);
            order.setShippingFee(shippingFee);
            order.setDiscount(discount);

            if ("DELIVERED".equalsIgnoreCase(status)) {
                order.setDeliveredAt(LocalDateTime.now());
            }

            orderService.save(order);

            redirectAttributes.addFlashAttribute("success", "Order updated successfully!");
            return "redirect:/admin/orders/" + id;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update order: " + e.getMessage());
            return "redirect:/admin/orders/edit/" + id;
        }
    }

    /**
     * Quick update order status
     */
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated to: " + status);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }

    /**
     * Quick update payment status
     */
    @PostMapping("/{id}/payment")
    public String updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String paymentStatus,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.updatePaymentStatus(id, paymentStatus);
            redirectAttributes.addFlashAttribute("success", "Payment status updated to: " + paymentStatus);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update payment status: " + e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }

    /**
     * Delete order
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Order deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete order: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    /**
     * Print order / Invoice
     */
    @GetMapping("/{id}/print")
    public String printOrder(@PathVariable Long id, Model model) {
        try {
            Order order = orderService.findById(id);
            model.addAttribute("order", order);
            return "admin/order/print";
        } catch (RuntimeException e) {
            return "redirect:/admin/orders?error=" + e.getMessage();
        }
    }
}

