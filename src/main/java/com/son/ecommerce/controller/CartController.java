package com.son.ecommerce.controller;

import com.son.ecommerce.entity.CartItemEntity;
import com.son.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * View cart page
     */
    @GetMapping
    public String viewCart(Model model) {
        // TODO: Get actual user ID from SecurityContext
        // For now, use default userId = 1
        Long userId = 1L;

        try {
            List<CartItemEntity> cartItems = cartService.getCartItems(userId);

            // Calculate totals
            double subtotal = cartItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            double shippingFee = subtotal > 50 ? 0 : 10; // Free shipping over $50
            double discount = 0; // Can implement coupon logic later
            double total = subtotal + shippingFee - discount;

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("discount", discount);
            model.addAttribute("total", total);
            model.addAttribute("itemCount", cartItems.size());
            model.addAttribute("content", "cart");

            return "layout/main";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load cart: " + e.getMessage());
            model.addAttribute("content", "cart");
            return "layout/main";
        }
    }

    /**
     * Update cart item quantity
     */
    @PostMapping("/update/{productId}")
    public String updateCartItem(
            @PathVariable Long productId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {

        Long userId = 1L;

        try {
            if (quantity > 0) {
                cartService.updateCartItemQuantity(userId, productId, quantity);
                redirectAttributes.addFlashAttribute("success", "Cart updated successfully!");
            } else {
                cartService.removeItemFromCart(userId, productId);
                redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update cart: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Remove item from cart
     */
    @GetMapping("/remove/{productId}")
    public String removeItem(@PathVariable Long productId, RedirectAttributes redirectAttributes) {
        Long userId = 1L;

        try {
            cartService.removeItemFromCart(userId, productId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove item: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Clear entire cart
     */
    @GetMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        Long userId = 1L;

        try {
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("success", "Cart cleared successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to clear cart: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Apply coupon code
     */
    @PostMapping("/apply-coupon")
    public String applyCoupon(
            @RequestParam String couponCode,
            RedirectAttributes redirectAttributes) {

        // TODO: Implement coupon validation and application
        redirectAttributes.addFlashAttribute("info", "Coupon feature coming soon!");

        return "redirect:/cart";
    }
}

