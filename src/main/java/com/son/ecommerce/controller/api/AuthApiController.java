package com.son.ecommerce.controller.api;

import com.son.ecommerce.dto.*;
import com.son.ecommerce.entity.RefreshToken;
import com.son.ecommerce.entity.Role;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.UserRepository;
import com.son.ecommerce.security.JwtUtil;
import com.son.ecommerce.service.AuthService;
import com.son.ecommerce.service.PasswordResetService;
import com.son.ecommerce.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * Register a new user
     * POST /api/auth/register
     * Request body: { "username": "user1", "password": "pass123", "email": "user@example.com" }
     * Response: { "message": "User registered successfully", "user": {...} }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            // Get the registered user to return in response
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after registration"));

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", convertUserToResponse(user));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Login user and get JWT tokens (access token + refresh token)
     * POST /api/auth/login
     * Request body: { "email": "user@example.com", "password": "pass123" }
     * Response: { "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.loginWithTokens(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     * Request body: { "refreshToken": "..." }
     * Response: { "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            RefreshToken token = refreshTokenService.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            refreshTokenService.verifyExpiration(token);

            String username = token.getUser().getUsername();
            String newAccessToken = jwtUtil.generateAccessToken(username);

            AuthResponse response = new AuthResponse(newAccessToken, refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Logout user - delete refresh token
     * POST /api/auth/logout
     * Request body: { "refreshToken": "..." }
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        try {
            refreshTokenService.deleteByToken(request.getRefreshToken());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Logout failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Test endpoint to verify authentication is working
     * GET /api/auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Auth API is working");
        return ResponseEntity.ok(response);
    }

    /**
     * Forgot Password - Request password reset
     * POST /api/auth/forgot-password
     * Request body: { "email": "user@example.com" }
     * Response: { "message": "Password reset email sent" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.generatePasswordResetToken(request.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email sent to your email address");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Reset Password - Set new password with token
     * POST /api/auth/reset-password
     * Request body: { "token": "...", "newPassword": "...", "confirmPassword": "..." }
     * Response: { "message": "Password reset successfully" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Passwords do not match");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Validate password strength
            if (request.getNewPassword().length() < 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password must be at least 6 characters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
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
