package com.son.ecommerce.controller;

import com.son.ecommerce.dto.CustomUserDetails;
import com.son.ecommerce.entity.Order;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * Danh sách đơn hàng của user hiện tại
     */
    @GetMapping
    public String listOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String error,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            List<Order> orders = orderService.findByUserId(userDetails.getId());
            model.addAttribute("user", userService.findById(userDetails.getId()));
            model.addAttribute("orders", orders);
            if (error != null) {
                model.addAttribute("errorMsg", error);
            }
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Không thể tải danh sách đơn hàng: " + e.getMessage());
            model.addAttribute("orders", List.of());
            model.addAttribute("user", userService.findById(userDetails.getId()));
        }

        model.addAttribute("content", "profile/orders");
        return "layout/main";
    }

    /**
     * Chi tiết một đơn hàng
     */
    @GetMapping("/{id}")
    public String viewOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.findById(id);

            // Kiểm tra quyền: đơn hàng phải thuộc về user đang đăng nhập
            if (order.getUser() == null || !order.getUser().getId().equals(userDetails.getId())) {
                redirectAttributes.addFlashAttribute("errorMsg", "Bạn không có quyền xem đơn hàng này.");
                return "redirect:/orders";
            }

            model.addAttribute("user", userService.findById(userDetails.getId()));
            model.addAttribute("order", order);
            model.addAttribute("content", "order-confirmation");
            return "layout/main";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy đơn hàng #" + id);
            return "redirect:/orders";
        }
    }
}
