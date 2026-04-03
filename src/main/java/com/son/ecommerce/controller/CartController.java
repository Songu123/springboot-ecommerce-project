package com.son.ecommerce.controller;

import com.son.ecommerce.dto.CustomUserDetails;
import com.son.ecommerce.dto.UserResponse;
import com.son.ecommerce.entity.CartItemEntity;
import com.son.ecommerce.security.SecurityConfig;
import com.son.ecommerce.service.AuthService;
import com.son.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public String viewCart(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        try {
            // Check if user is logged in
            if (currentUser == null) {
                System.out.println("❌ [CartController] User not authenticated");
                model.addAttribute("error", "Please login to view your cart");
                model.addAttribute("content", "cart");
                return "redirect:/login";
            }

            Long userId = currentUser.getId();
            System.out.println("✅ [CartController] Viewing cart for user: " + userId);

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
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("userId", userId);
            model.addAttribute("userEmail", currentUser.getEmail());
            model.addAttribute("fullName", currentUser.getFullName());
            model.addAttribute("content", "cart");

            return "layout/main";
        } catch (Exception e) {
            System.out.println("❌ [CartController] Error loading cart: " + e.getMessage());
            e.printStackTrace();
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
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            if (currentUser == null) {
                System.out.println("❌ [CartController] User not authenticated for update");
                redirectAttributes.addFlashAttribute("error", "Please login to update cart");
                return "redirect:/login";
            }

            Long userId = currentUser.getId();
            System.out.println("✅ [CartController] Updating cart for user: " + userId + ", productId: " + productId + ", quantity: " + quantity);

            if (quantity > 0) {
                cartService.updateCartItemQuantity(userId, productId, quantity);
                redirectAttributes.addFlashAttribute("success", "Cart updated successfully!");
            } else {
                cartService.removeItemFromCart(userId, productId);
                redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
            }
        } catch (Exception e) {
            System.out.println("❌ [CartController] Error updating cart: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update cart: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Remove item from cart
     */
    @GetMapping("/remove/{productId}")
    public String removeItem(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            if (currentUser == null) {
                System.out.println("❌ [CartController] User not authenticated for remove");
                redirectAttributes.addFlashAttribute("error", "Please login to remove items");
                return "redirect:/login";
            }

            Long userId = currentUser.getId();
            System.out.println("✅ [CartController] Removing item from cart for user: " + userId + ", productId: " + productId);
            cartService.removeItemFromCart(userId, productId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
        } catch (Exception e) {
            System.out.println("❌ [CartController] Error removing item: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to remove item: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Clear entire cart
     */
    @GetMapping("/clear")
    public String clearCart(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            if (currentUser == null) {
                System.out.println("❌ [CartController] User not authenticated for clear");
                redirectAttributes.addFlashAttribute("error", "Please login to clear cart");
                return "redirect:/login";
            }

            Long userId = currentUser.getId();
            System.out.println("✅ [CartController] Clearing cart for user: " + userId);
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("success", "Cart cleared successfully!");
        } catch (Exception e) {
            System.out.println("❌ [CartController] Error clearing cart: " + e.getMessage());
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
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            if (currentUser == null) {
                System.out.println("❌ [CartController] User not authenticated for coupon");
                redirectAttributes.addFlashAttribute("error", "Please login to apply coupon");
                return "redirect:/login";
            }

            Long userId = currentUser.getId();
            System.out.println("✅ [CartController] Applying coupon for user: " + userId + ", coupon: " + couponCode);

            // TODO: Implement coupon validation and application
            redirectAttributes.addFlashAttribute("info", "Coupon feature coming soon!");
        } catch (Exception e) {
            System.out.println("❌ [CartController] Error applying coupon: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to apply coupon: " + e.getMessage());
        }

        return "redirect:/cart";
    }
}

