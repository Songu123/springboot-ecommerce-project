package com.son.ecommerce.service;

import com.son.ecommerce.entity.Role;
import java.util.List;

public interface RoleService {
    List<Role> findAll();
    Role findById(Long id);
    Role findByName(String name);
    Role save(Role role);
    void deleteById(Long id);
}

