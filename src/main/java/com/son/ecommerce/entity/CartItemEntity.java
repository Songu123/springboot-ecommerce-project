package com.son.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonIgnoreProperties({"items", "user"})
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties("category")
    private Product product;

    private int quantity;

    // ✅ NEW: These fields are populated from product for JSON serialization
    // @Transient means they are NOT persisted to database, only used in JSON responses
    @Transient
    private Long productId;

    @Transient
    private double price;

    private LocalDateTime addedAt = LocalDateTime.now();

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

