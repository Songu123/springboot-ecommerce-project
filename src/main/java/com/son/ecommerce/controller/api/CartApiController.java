package com.son.ecommerce.controller.api;

import com.son.ecommerce.dto.AddToCartRequest;
import com.son.ecommerce.dto.CartResponse;
import com.son.ecommerce.dto.UpdateCartItemRequest;
import com.son.ecommerce.entity.Cart;
import com.son.ecommerce.entity.CartItemEntity;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.UserRepository;
import com.son.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final UserRepository userRepository;

    // GET cart by user ID
    @GetMapping("/{userId}")
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
    @GetMapping("/{userId}/items")
    public ResponseEntity<List<CartItemEntity>> getCartItems(@PathVariable Long userId) {
        try {
            List<CartItemEntity> items = cartService.getCartItems(userId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST - Add item to cart
    @PostMapping("/{userId}/items")
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
    @PutMapping("/{userId}/items")
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
    @DeleteMapping("/{userId}/items/{productId}")
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
    @DeleteMapping("/{userId}")
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

        // ✅ FIX: Populate price and productId for each cart item before returning
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            for (CartItemEntity item : cart.getItems()) {
                if (item.getProduct() != null) {
                    // Set productId from product
                    item.setProductId(item.getProduct().getId());
                    // Set price from product
                    item.setPrice(item.getProduct().getPrice());
                    System.out.println("✅ [CartController] CartItem populated - ID: " + item.getId() +
                                       ", ProductID: " + item.getProductId() +
                                       ", Price: " + item.getPrice());
                }
            }
        }

        response.setItems(cart.getItems());
        response.setTotalItems(cart.getTotalItems());
        response.setTotalPrice(cart.getTotalPrice());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }

    // ===== AUTHENTICATED ENDPOINTS (Get userId from JWT token) =====

    /**
     * Get current authenticated user's ID from JWT token
     * Extracts username from authentication principal and fetches user ID from database
     */
    private Long getCurrentUserId() {
        System.out.println("🔍 [CartController] Getting current authenticated user");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔍 [CartController] Authentication: " + authentication);

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("❌ [CartController] User not authenticated!");
            throw new RuntimeException("User not authenticated");
        }

        // Get username from authentication (JWT filter sets username as principal)
        String username = authentication.getName();
        System.out.println("✅ [CartController] Authenticated user: " + username);

        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        System.out.println("✅ [CartController] User ID: " + user.getId());

        return user.getId();
    }

    /**
     * Add item to cart for authenticated user
     * POST /api/cart/add
     *
     * Request body:
     * {
     *   "productId": 1,
     *   "quantity": 2
     * }
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("🔍 [CartController] Received /api/cart/add request");
            System.out.println("🔍 [CartController] Request body: " + request);

            // Validate request
            if (request.get("productId") == null || request.get("quantity") == null) {
                System.out.println("❌ [CartController] Missing productId or quantity");
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "productId and quantity are required");
                return ResponseEntity.badRequest().body(error);
            }

            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            // Validate quantity
            if (quantity <= 0) {
                System.out.println("❌ [CartController] Invalid quantity: " + quantity);
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Quantity must be greater than 0");
                return ResponseEntity.badRequest().body(error);
            }

            // Get current authenticated user ID
            Long userId = getCurrentUserId();
            System.out.println("✅ [CartController] Processing cart/add for user: " + userId);

            // Add item to cart
            CartItemEntity cartItem = cartService.addItemToCart(userId, productId, quantity);
            System.out.println("✅ [CartController] Item added to cart successfully");

            // ✅ FIX: Map to DTO to prevent Jackson Entity Serialization issues
            Map<String, Object> cartItemDto = new HashMap<>();
            cartItemDto.put("id", cartItem.getId());
            cartItemDto.put("quantity", cartItem.getQuantity());
            if (cartItem.getProduct() != null) {
                cartItemDto.put("productId", cartItem.getProduct().getId());
                cartItemDto.put("price", cartItem.getProduct().getPrice());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product added to cart successfully");
            response.put("cartItem", cartItemDto);
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("❌ [CartController] Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to add product to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get cart count for authenticated user
     * GET /api/cart/count
     *
     * Response:
     * {
     *   "count": 5,
     *   "success": true
     * }
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCartCount() {
        try {
            // Get current authenticated user ID
            Long userId = getCurrentUserId();

            // Get cart items
            List<CartItemEntity> items = cartService.getCartItems(userId);
            int count = items.stream().mapToInt(CartItemEntity::getQuantity).sum();

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("success", true);
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Get full cart for authenticated user
     * GET /api/cart/my-cart
     */
    @GetMapping("/my-cart")
    public ResponseEntity<Map<String, Object>> getMyCart() {
        try {
            System.out.println("🔍 [CartController] GET /api/cart/my-cart request received");

            Long userId = getCurrentUserId();
            System.out.println("✅ [CartController] User ID: " + userId);

            Cart cart = cartService.getOrCreateCartForUser(userId);
            System.out.println("✅ [CartController] Cart retrieved with " + (cart.getItems() != null ? cart.getItems().size() : 0) + " items");

            CartResponse cartResponse = convertToResponse(cart);
            System.out.println("✅ [CartController] CartResponse populated with price and productId for all items");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cart", cartResponse);
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("❌ [CartController] Error in getMyCart: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Remove item from authenticated user's cart
     * DELETE /api/cart/remove/{productId}
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeItemFromMyCart(@PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            cartService.removeItemFromCart(userId, productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item removed from cart");
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update item quantity in authenticated user's cart
     * PUT /api/cart/update
     *
     * Request body:
     * {
     *   "productId": 1,
     *   "quantity": 5
     * }
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateMyCartItem(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("productId") == null || request.get("quantity") == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "productId and quantity are required");
                return ResponseEntity.badRequest().body(error);
            }

            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            Long userId = getCurrentUserId();
            CartItemEntity cartItem = cartService.updateCartItemQuantity(userId, productId, quantity);

            // ✅ FIX: Populate into DTO to avoid JSON serialization failures
            Map<String, Object> cartItemDto = new HashMap<>();
            if (cartItem != null) {
                cartItemDto.put("id", cartItem.getId());
                cartItemDto.put("quantity", cartItem.getQuantity());
                if (cartItem.getProduct() != null) {
                    cartItemDto.put("productId", cartItem.getProduct().getId());
                    cartItemDto.put("price", cartItem.getProduct().getPrice());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart item updated successfully");
            response.put("cartItem", cartItemDto);
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Clear authenticated user's entire cart
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearMyCart() {
        try {
            Long userId = getCurrentUserId();
            cartService.clearCart(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart cleared successfully");
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
