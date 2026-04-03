package com.son.ecommerce.repository;

import com.son.ecommerce.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    
    @EntityGraph(attributePaths = {"product", "product.category"})
    List<CartItemEntity> findByCartId(Long cartId);

    // ✅ FIX: Use custom JPQL query that navigates through product relationship
    // Instead of c.productId (which doesn't exist), use c.product.id
    @EntityGraph(attributePaths = {"product", "product.category"})
    @Query("SELECT c FROM CartItemEntity c WHERE c.cart.id = :cartId AND c.product.id = :productId")
    Optional<CartItemEntity> findByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);

    void deleteByCartId(Long cartId);
}

