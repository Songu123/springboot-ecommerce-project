package com.son.ecommerce.controller.api;

import com.son.ecommerce.entity.Order;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.service.OrderService;
import com.son.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;
    private final UserService userService;

    // ✅ Helper: Get current authenticated user from JWT
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    // GET all orders - Only admin can view all
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            System.out.println("❌ [OrderController] Unauthorized: getAllOrders");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Optional: Check if user is admin - for now just require authentication
        System.out.println("✅ [OrderController] User " + currentUser.getId() + " accessing all orders");
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orders);
    }

    // GET order by ID - ✅ WITH JWT VALIDATION
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        try {
            // ✅ Get current user from JWT
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                System.out.println("❌ [OrderController] Unauthorized: getOrderById");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Order order = orderService.findById(id);

            // ✅ SECURITY: Check if order belongs to current user
            if (!order.getUser().getId().equals(currentUser.getId())) {
                System.out.println("🔴 [OrderController] SECURITY BREACH ATTEMPT!");
                System.out.println("   - Authenticated user: " + currentUser.getId());
                System.out.println("   - Order belongs to: " + order.getUser().getId());
                System.out.println("   - Request DENIED!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            System.out.println("✅ [OrderController] User " + currentUser.getId() + " accessing own order " + id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET orders by user - ✅ WITH JWT VALIDATION
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        // ✅ Get current user from JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("❌ [OrderController] User not authenticated!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = authentication.getName();
        User currentUser = userService.findByUsername(username);

        if (currentUser == null) {
            System.out.println("❌ [OrderController] User not found in database: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // ✅ SECURITY: Check if requesting user's own orders or is admin
        if (!currentUser.getId().equals(userId)) {
            System.out.println("🔴 [OrderController] SECURITY BREACH ATTEMPT!");
            System.out.println("   - Authenticated user: " + currentUser.getId());
            System.out.println("   - Requested user: " + userId);
            System.out.println("   - Request DENIED!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        System.out.println("✅ [OrderController] User verified - ID: " + currentUser.getId() + " requesting own orders");
        List<Order> orders = orderService.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // GET orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.findByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // POST - Create new order
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // ✅ Get current user from JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println("🔐 [OrderController] Current user: " + username);

        // ✅ Load user from database
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // ✅ Set user for order
        order.setUser(user);
        System.out.println("✅ [OrderController] User set for order - ID: " + user.getId() + ", Username: " + user.getUsername());

        // Save order with user
        Order savedOrder = orderService.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    // PUT - Update order - ✅ WITH JWT VALIDATION
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        try {
            // ✅ Get current user from JWT
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                System.out.println("❌ [OrderController] Unauthorized: updateOrder");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Order existingOrder = orderService.findById(id);

            // ✅ SECURITY: Check if order belongs to current user
            if (!existingOrder.getUser().getId().equals(currentUser.getId())) {
                System.out.println("🔴 [OrderController] SECURITY BREACH: Unauthorized update attempt!");
                System.out.println("   - Authenticated user: " + currentUser.getId());
                System.out.println("   - Order belongs to: " + existingOrder.getUser().getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            existingOrder.setTotalPrice(order.getTotalPrice());
            existingOrder.setStatus(order.getStatus());
            Order updatedOrder = orderService.save(existingOrder);
            System.out.println("✅ [OrderController] User " + currentUser.getId() + " updated order " + id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH - Update order status - ✅ WITH JWT VALIDATION
    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            // ✅ Get current user from JWT
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                System.out.println("❌ [OrderController] Unauthorized: updateOrderStatus");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            Order order = orderService.findById(id);

            // ✅ SECURITY: Check if order belongs to current user
            if (!order.getUser().getId().equals(currentUser.getId())) {
                System.out.println("🔴 [OrderController] SECURITY BREACH: Unauthorized status update attempt!");
                System.out.println("   - Authenticated user: " + currentUser.getId());
                System.out.println("   - Order belongs to: " + order.getUser().getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            order.setStatus(status);
            Order updatedOrder = orderService.save(order);
            System.out.println("✅ [OrderController] User " + currentUser.getId() + " updated status of order " + id);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE order - ✅ WITH JWT VALIDATION
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            // ✅ Get current user from JWT
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                System.out.println("❌ [OrderController] Unauthorized: deleteOrder");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Order order = orderService.findById(id);

            // ✅ SECURITY: Check if order belongs to current user
            if (!order.getUser().getId().equals(currentUser.getId())) {
                System.out.println("🔴 [OrderController] SECURITY BREACH: Unauthorized deletion attempt!");
                System.out.println("   - Authenticated user: " + currentUser.getId());
                System.out.println("   - Order belongs to: " + order.getUser().getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            orderService.deleteById(id);
            System.out.println("✅ [OrderController] User " + currentUser.getId() + " deleted order " + id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

