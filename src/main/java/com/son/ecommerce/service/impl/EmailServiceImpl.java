package com.son.ecommerce.service.impl;

import com.son.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@ecommerce.com");
            message.setTo(toEmail);
            message.setSubject("🔐 Đặt lại mật khẩu - E-Commerce");
            message.setText(buildPasswordResetEmailContent(fullName, resetToken, resetLink));

            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw - email service failure shouldn't crash the app
            System.err.println("Failed to send password reset email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetSuccessEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@ecommerce.com");
            message.setTo(toEmail);
            message.setSubject("✅ Mật khẩu đã được đặt lại - E-Commerce");
            message.setText(buildPasswordResetSuccessEmailContent(fullName));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send password reset success email to " + toEmail + ": " + e.getMessage());
        }
    }

    private String buildPasswordResetEmailContent(String fullName, String resetToken, String resetLink) {
        return "Xin chào " + fullName + ",\n\n" +
                "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" +
                "Nhấp vào liên kết dưới đây để đặt lại mật khẩu của bạn:\n" +
                resetLink + "\n\n" +
                "Hoặc sao chép mã này vào biểu mẫu đặt lại mật khẩu:\n" +
                resetToken + "\n\n" +
                "Liên kết này sẽ hết hạn trong 1 giờ.\n\n" +
                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ E-Commerce";
    }

    private String buildPasswordResetSuccessEmailContent(String fullName) {
        return "Xin chào " + fullName + ",\n\n" +
                "Mật khẩu của bạn đã được đặt lại thành công.\n\n" +
                "Bạn có thể sử dụng mật khẩu mới của mình để đăng nhập vào tài khoản của bạn.\n\n" +
                "Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ E-Commerce";
    }
}

