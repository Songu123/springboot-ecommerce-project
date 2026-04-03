package com.son.ecommerce.repository;

import com.son.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<Product> findAll();

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByNameContainingIgnoreCase(String keyword);
}