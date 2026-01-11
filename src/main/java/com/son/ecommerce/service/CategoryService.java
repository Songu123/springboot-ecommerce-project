package com.son.ecommerce.service;


import com.son.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> findAll();

    Category save(Category category);

    Category findById(Long id);

    void deleteById(Long id);
}