package com.son.ecommerce.controller;

import com.son.ecommerce.entity.CartItemEntity;
import com.son.ecommerce.entity.Order;
import com.son.ecommerce.entity.OrderItem;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.service.CartService;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping
    public String checkout(Model model) {
        Long userId = 1L;

        try {
            List<CartItemEntity> cartItems = cartService.getCartItems(userId);

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
            @RequestParam String shippingAddress,
            @RequestParam String shippingCity,
            @RequestParam String shippingPhone,
            @RequestParam String shippingMethod,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes) {

        Long userId = 1L;

        try {
            List<CartItemEntity> cartItems = cartService.getCartItems(userId);

            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cart is empty");
                return "redirect:/cart";
            }

            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            double shippingFee = "express".equals(shippingMethod) ? 10 :
                                "nextday".equals(shippingMethod) ? 20 : 0;

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
            cartService.clearCart(userId);

            redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order #" + savedOrder.getId());
            return "redirect:/orders/" + savedOrder.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to place order: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}

