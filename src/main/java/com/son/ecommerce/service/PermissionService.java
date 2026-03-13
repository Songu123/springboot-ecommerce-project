package com.son.ecommerce.service;

import com.son.ecommerce.entity.Permission;
import java.util.List;

public interface PermissionService {
    List<Permission> findAll();
    Permission findById(Long id);
    Permission findByName(String name);
    List<Permission> findByCategory(String category);
    Permission save(Permission permission);
    void deleteById(Long id);
}

