package com.son.ecommerce.controller.api;

import com.son.ecommerce.dto.AddToCartRequest;
import com.son.ecommerce.dto.CartResponse;
import com.son.ecommerce.dto.UpdateCartItemRequest;
import com.son.ecommerce.entity.Cart;
import com.son.ecommerce.entity.CartItemEntity;
import com.son.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;

    // GET cart by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCartByUserId(@PathVariable Long userId) {
        try {
            Cart cart = cartService.getOrCreateCartForUser(userId);
            CartResponse response = convertToResponse(cart);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET cart items by user ID
    @GetMapping("/user/{userId}/items")
    public ResponseEntity<List<CartItemEntity>> getCartItems(@PathVariable Long userId) {
        try {
            List<CartItemEntity> items = cartService.getCartItems(userId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Add item to cart
    @PostMapping("/user/{userId}/items")
    public ResponseEntity<CartItemEntity> addItemToCart(
            @PathVariable Long userId,
            @RequestBody AddToCartRequest request) {
        try {
            CartItemEntity cartItem = cartService.addItemToCart(
                    userId,
                    request.getProductId(),
                    request.getQuantity()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT - Update cart item quantity
    @PutMapping("/user/{userId}/items")
    public ResponseEntity<CartItemEntity> updateCartItem(
            @PathVariable Long userId,
            @RequestBody UpdateCartItemRequest request) {
        try {
            CartItemEntity cartItem = cartService.updateCartItemQuantity(
                    userId,
                    request.getProductId(),
                    request.getQuantity()
            );
            if (cartItem == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Remove item from cart
    @DeleteMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        try {
            cartService.removeItemFromCart(userId, productId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Clear cart
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper method to convert Cart entity to CartResponse DTO
    private CartResponse convertToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setItems(cart.getItems());
        response.setTotalItems(cart.getTotalItems());
        response.setTotalPrice(cart.getTotalPrice());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }

    // ===== SIMPLE ENDPOINTS FOR FRONTEND =====

    @PostMapping("/add")
    public ResponseEntity<java.util.Map<String, Object>> addToCart(@RequestBody java.util.Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            // For now, use userId = 1 as default (guest user)
            // TODO: Get actual userId from SecurityContext
            Long userId = 1L;

            CartItemEntity cartItem = cartService.addItemToCart(userId, productId, quantity);

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Product added to cart successfully");
            response.put("cartItem", cartItem);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to add product to cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<java.util.Map<String, Integer>> getCartCount() {
        try {
            // For now, use userId = 1 as default
            // TODO: Get actual userId from SecurityContext
            Long userId = 1L;

            List<CartItemEntity> items = cartService.getCartItems(userId);
            int count = items.stream().mapToInt(CartItemEntity::getQuantity).sum();

            java.util.Map<String, Integer> response = new java.util.HashMap<>();
            response.put("count", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            java.util.Map<String, Integer> response = new java.util.HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }
    }
}
