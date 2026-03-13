package com.son.ecommerce.service;

import com.son.ecommerce.dto.LoginRequest;
import com.son.ecommerce.dto.RegisterRequest;
import com.son.ecommerce.entity.Role;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.RoleRepository;
import com.son.ecommerce.repository.UserRepository;
import com.son.ecommerce.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private  final PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder =  passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Get the default user role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName() != null ? request.getFullName() : request.getUsername());
        user.setRoles(new HashSet<>(Set.of(userRole)));
        user.setEnabled(true);

        userRepository.save(user);
    }

    public String login(LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("Invalid username"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(user.getUsername());
    }
}


