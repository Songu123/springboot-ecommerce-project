package com.son.ecommerce.service;

import com.son.ecommerce.entity.PasswordResetToken;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.PasswordResetTokenRepository;
import com.son.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final long PASSWORD_RESET_TOKEN_EXPIRATION_MS = 3600000; // 1 hour

    /**
     * Generate password reset token and send email
     */
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email not found: " + email));

        // Delete any existing reset tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(Instant.now().plusMillis(PASSWORD_RESET_TOKEN_EXPIRATION_MS));
        resetToken.setUsed(false);

        passwordResetTokenRepository.save(resetToken);

        // Send email with reset link
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token, resetLink);
    }

    /**
     * Validate password reset token
     */
    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Token is invalid or already used");
        }

        return resetToken;
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = validateToken(token);
        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Send confirmation email
        emailService.sendPasswordResetSuccessEmail(user.getEmail(), user.getFullName());
    }

    /**
     * Clean up expired tokens (can be called by scheduled task)
     */
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.findAll().stream()
                .filter(PasswordResetToken::isExpired)
                .forEach(passwordResetTokenRepository::delete);
    }
}

