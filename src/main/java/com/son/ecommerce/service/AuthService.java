package com.son.ecommerce.service;

import com.son.ecommerce.dto.AuthResponse;
import com.son.ecommerce.dto.LoginRequest;
import com.son.ecommerce.dto.RegisterRequest;
import com.son.ecommerce.dto.UserResponse;
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
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    private UserResponse currentUser;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
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

    // New login method that returns AuthResponse with both tokens and user info
    public AuthResponse loginWithTokens(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate access token with user information
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user);
        String refreshToken = refreshTokenService.generateRefreshToken(user.getId());
        UserResponse userResponse = convertUserToResponse(user);

        AuthResponse response = new AuthResponse();
        response.setUser(userResponse);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");

        return response;
    }

    // Keep old login method for backward compatibility
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        currentUser.setId(user.getId());
        currentUser.setUsername(user.getUsername());
        currentUser.setEmail(user.getEmail());
        currentUser.setFullName(user.getFullName());
        currentUser.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        return jwtUtil.generateToken(user.getUsername());
    }

    public UserResponse getCurrentUser() {

        return currentUser;
    }

    // Helper method to convert User entity to UserResponse
    private UserResponse convertUserToResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .enabled(user.isEnabled())
                .build();
    }
}




