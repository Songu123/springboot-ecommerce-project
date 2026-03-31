package com.son.ecommerce.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String fullName, String resetToken, String resetLink);
    void sendPasswordResetSuccessEmail(String toEmail, String fullName);
}

