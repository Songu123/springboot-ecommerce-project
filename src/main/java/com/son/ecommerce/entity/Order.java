package com.son.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double totalPrice;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private LocalDateTime deliveredAt;

    // Shipping Information
    @Column(length = 200)
    private String shippingAddress;

    @Column(length = 100)
    private String shippingCity;

    @Column(length = 20)
    private String shippingPostalCode;

    @Column(length = 20)
    private String shippingPhone;

    // Payment Information
    @Column(length = 50)
    private String paymentMethod; // COD, CREDIT_CARD, BANK_TRANSFER, MOMO, VNPAY

    @Column(length = 20)
    private String paymentStatus = "UNPAID"; // UNPAID, PAID, REFUNDED

    // Additional fields
    @Column(length = 500)
    private String note;

    @Column(length = 100)
    private String trackingNumber;

    private double shippingFee = 0.0;

    private double discount = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"orders", "password", "enabled", "roles"})
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("order")
    private List<OrderItem> items;

    // Helper methods
    public double getFinalTotal() {
        return totalPrice + shippingFee - discount;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public int getTotalItems() {
        return items != null ? items.stream().mapToInt(OrderItem::getQuantity).sum() : 0;
    }
}
