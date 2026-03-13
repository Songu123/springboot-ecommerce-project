package com.son.ecommerce.service;

import com.son.ecommerce.entity.Cart;
import com.son.ecommerce.entity.CartItemEntity;

import java.util.List;

public interface CartService {
    Cart findById(Long id);
    Cart findByUserId(Long userId);
    Cart getOrCreateCartForUser(Long userId);
    Cart save(Cart cart);
    void deleteById(Long id);
    void deleteByUserId(Long userId);

    // Cart Item operations
    CartItemEntity addItemToCart(Long userId, Long productId, int quantity);
    CartItemEntity updateCartItemQuantity(Long userId, Long productId, int quantity);
    void removeItemFromCart(Long userId, Long productId);
    void clearCart(Long userId);
    List<CartItemEntity> getCartItems(Long userId);
}

