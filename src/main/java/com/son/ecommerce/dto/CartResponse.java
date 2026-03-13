package com.son.ecommerce.dto;

import com.son.ecommerce.entity.CartItemEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private Long userId;
    private List<CartItemEntity> items;
    private int totalItems;
    private double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

