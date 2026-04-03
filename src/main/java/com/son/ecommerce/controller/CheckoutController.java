package com.son.ecommerce.controller;

import com.son.ecommerce.dto.CustomUserDetails;
import com.son.ecommerce.entity.CartItemEntity;
import com.son.ecommerce.entity.Order;
import com.son.ecommerce.entity.OrderItem;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.service.CartService;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?error=Please login to proceed to checkout";
        }
        try {
            List<CartItemEntity> cartItems = cartService.getCartItems(userDetails.getId());

            if (cartItems.isEmpty()) {
                return "redirect:/cart?error=Cart is empty";
            }

            double subtotal = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("content", "checkout");

            return "layout/main";
        } catch (Exception e) {
            return "redirect:/cart?error=" + e.getMessage();
        }
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String shippingAddress,
            @RequestParam String shippingCity,
            @RequestParam String shippingPhone,
            @RequestParam String shippingMethod,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login?error=Please login to place an order";
        }

        Long userId = userDetails.getId();

        try {
            List<CartItemEntity> cartItems = cartService.getCartItems(userId);

            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống, không thể đặt hàng.");
                return "redirect:/cart";
            }

            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            double shippingFee = "express".equals(shippingMethod) ? 30000 :
                                "nextday".equals(shippingMethod) ? 60000 : 0;

            User user = userService.findById(userId);
            Order order = Order.builder()
                    .user(user)
                    .totalPrice(totalPrice)
                    .shippingFee(shippingFee)
                    .status("PENDING")
                    .paymentMethod(paymentMethod)
                    .paymentStatus("UNPAID")
                    .shippingAddress(shippingAddress)
                    .shippingCity(shippingCity)
                    .shippingPhone(shippingPhone)
                    .note(note)
                    .createdAt(LocalDateTime.now())
                    .build();

            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItemEntity cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                orderItems.add(orderItem);
            }
            order.setItems(orderItems);

            Order savedOrder = orderService.save(order);

            // Clear cart after successful order placement
            cartService.clearCart(userId);

            // Broadcast real-time order notification via WebSocket
            try {
                String payload = String.format("{\"id\": %d, \"customerName\": \"%s\", \"total\": %s}",
                        savedOrder.getId(), user.getFullName() != null ? user.getFullName() : user.getUsername(), String.valueOf(totalPrice));
                messagingTemplate.convertAndSend("/topic/admin/orders", payload);
            } catch (Exception wsEx) {
                // Ignore websocket send errors (don't break the checkout flow)
            }

            redirectAttributes.addFlashAttribute("orderSuccess", true);
            return "redirect:/orders/" + savedOrder.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}

