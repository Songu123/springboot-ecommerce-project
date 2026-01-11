package com.son.ecommerce.service;

import com.son.ecommerce.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
    List<Product> findByCategoryId(Long categoryId);
}

