package com.son.ecommerce.service;

import com.son.ecommerce.entity.User;
import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User save(User user);
    void deleteById(Long id);
    User findByEmail(String email);
    User findByUsername(String username);
}

