package com.son.ecommerce.service.impl;

import com.son.ecommerce.entity.Cart;
import com.son.ecommerce.entity.CartItemEntity;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.CartItemRepository;
import com.son.ecommerce.repository.CartRepository;
import com.son.ecommerce.repository.ProductRepository;
import com.son.ecommerce.repository.UserRepository;
import com.son.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Cart findById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + id));
    }

    @Override
    public Cart findByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user id: " + userId));
    }

    @Override
    @Transactional
    public Cart getOrCreateCartForUser(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setCreatedAt(LocalDateTime.now());
                    cart.setUpdatedAt(LocalDateTime.now());
                    // Use saveAndFlush to ensure cart_id is generated immediately
                    return cartRepository.saveAndFlush(cart);
                });
    }

    @Override
    public Cart save(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        cartRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public CartItemEntity addItemToCart(Long userId, Long productId, int quantity) {
        // Validate inputs
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Invalid user ID: " + userId);
        }
        if (productId == null || productId <= 0) {
            throw new RuntimeException("Invalid product ID: " + productId);
        }
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Get or create cart for user
        Cart cart = getOrCreateCartForUser(userId);

        // Verify cart was created and has ID
        if (cart == null || cart.getId() == null) {
            throw new RuntimeException("Failed to create or retrieve cart for user: " + userId);
        }

        // Find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Check if item already exists in cart
        CartItemEntity existingCartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        CartItemEntity cartItem;
        if (existingCartItem != null) {
            // Update quantity if item exists
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            cartItem = existingCartItem;
        } else {
            // Create new cart item
            cartItem = new CartItemEntity();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setAddedAt(LocalDateTime.now());
        }

        // Save cart item and flush to database immediately
        CartItemEntity savedItem = cartItemRepository.saveAndFlush(cartItem);

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.saveAndFlush(cart);

        return savedItem;
    }

    @Override
    @Transactional
    public CartItemEntity updateCartItemQuantity(Long userId, Long productId, int quantity) {
        // Validate inputs
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Invalid user ID: " + userId);
        }
        if (productId == null || productId <= 0) {
            throw new RuntimeException("Invalid product ID: " + productId);
        }

        Cart cart = findByUserId(userId);

        CartItemEntity cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found for product: " + productId));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.saveAndFlush(cart);
            return null;
        }

        cartItem.setQuantity(quantity);
        CartItemEntity savedItem = cartItemRepository.saveAndFlush(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.saveAndFlush(cart);

        return savedItem;
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long userId, Long productId) {
        // Validate inputs
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Invalid user ID: " + userId);
        }
        if (productId == null || productId <= 0) {
            throw new RuntimeException("Invalid product ID: " + productId);
        }

        Cart cart = findByUserId(userId);
        CartItemEntity cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found for product: " + productId));

        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.saveAndFlush(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        // Validate input
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Invalid user ID: " + userId);
        }

        Cart cart = findByUserId(userId);
        cartItemRepository.deleteByCartId(cart.getId());

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.saveAndFlush(cart);
    }

    @Override
    public List<CartItemEntity> getCartItems(Long userId) {
        // Validate input
        if (userId == null || userId <= 0) {
            throw new RuntimeException("Invalid user ID: " + userId);
        }

        Cart cart = getOrCreateCartForUser(userId);
        return cartItemRepository.findByCartId(cart.getId());
    }
}

