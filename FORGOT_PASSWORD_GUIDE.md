# 🔐 Hướng Dẫn Quên Mật Khẩu (Forgot Password)

## 📌 Giới Thiệu

Tính năng **Quên Mật Khẩu** cho phép người dùng:
1. Yêu cầu đặt lại mật khẩu bằng email
2. Nhận email với liên kết đặt lại
3. Tạo mật khẩu mới bằng token từ email
4. Đăng nhập với mật khẩu mới

---

## 🔄 Quy Trình Hoạt Động

```
┌─────────────────────────────────────────────────────┐
│          FORGOT PASSWORD WORKFLOW                   │
└─────────────────────────────────────────────────────┘

1️⃣  USER CLICKS "FORGOT PASSWORD"
    GET /forgot-password
    └─> Shows forgot password form

2️⃣  USER SUBMITS EMAIL
    POST /api/auth/forgot-password
    Input: { "email": "user@example.com" }
    └─> Email sent with reset link!
    └─> Reset token: UUID (generated)
    └─> Reset link: /reset-password?token=...

3️⃣  USER RECEIVES EMAIL
    Subject: "🔐 Đặt lại mật khẩu - E-Commerce"
    Body: Contains reset link
    └─> Reset token expires in 1 hour!

4️⃣  USER CLICKS EMAIL LINK (or manual token entry)
    GET /reset-password?token={token}
    └─> Shows reset password form
    └─> Validates token

5️⃣  USER SUBMITS NEW PASSWORD
    POST /api/auth/reset-password
    Input: {
      "token": "...",
      "newPassword": "newpass123",
      "confirmPassword": "newpass123"
    }
    └─> Password updated!
    └─> Token marked as used
    └─> Confirmation email sent!

6️⃣  USER LOGS IN WITH NEW PASSWORD
    POST /api/auth/login
    Input: { "email": "user@example.com", "password": "newpass123" }
    └─> Success! Get tokens
```

---

## 🧪 Test Bằng Postman

### Step 1: Forgot Password (Request Reset)

```
POST http://localhost:8080/api/auth/forgot-password
Headers: Content-Type: application/json

Body:
{
  "email": "user123@example.com"
}

✅ Expected: 200 OK
Response:
{
  "message": "Password reset email sent to your email address"
}

⏰ Token expires in: 1 hour
```

**Chú ý:** Nếu sử dụng email service thực, email sẽ được gửi. 
Nếu không có email service config, có thể xem log để tìm reset token.

### Step 2: Get Reset Token

**Cách 1: Từ Email (Production)**
- Người dùng nhận email với link reset
- Link có format: `http://localhost:8080/reset-password?token=xxxxx`

**Cách 2: Từ Console Log (Development)**
```
Nếu email service không config, token sẽ in ra console
Tìm message: "Failed to send password reset email..."
Token được lưu trong database table: password_reset_token
```

**Cách 3: Query Database Directly (Development Only)**
```sql
SELECT token FROM password_reset_token 
WHERE user_id = (SELECT id FROM users WHERE email = 'user123@example.com')
AND used = false
AND expiry_date > NOW();
```

### Step 3: Reset Password (Set New Password)

```
POST http://localhost:8080/api/auth/reset-password
Headers: Content-Type: application/json

Body:
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}

✅ Expected: 200 OK
Response:
{
  "message": "Password reset successfully"
}
```

**Validation:**
- ✅ Token must be valid and not expired (1 hour)
- ✅ Token must not have been used before
- ✅ Password must be at least 6 characters
- ✅ Both passwords must match
- ✅ Email will be sent confirming password change

### Step 4: Login With New Password

```
POST http://localhost:8080/api/auth/login
Headers: Content-Type: application/json

Body:
{
  "email": "user123@example.com",
  "password": "newpassword123"
}

✅ Expected: 200 OK
Response:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "550e8400...",
  "tokenType": "Bearer"
}

✅ Old password no longer works!
✅ New password is now active!
```

---

## 🌐 Web Form Test

### Forgot Password Page

1. Open browser: `http://localhost:8080/forgot-password`
2. Enter email: `user123@example.com`
3. Click "Gửi Hướng Dẫn Đặt Lại"
4. Message: "Password reset email sent to your email address"

### Reset Password Page

1. Open browser: `http://localhost:8080/reset-password?token=<TOKEN>`
2. Enter new password: `newpass123`
3. Confirm password: `newpass123`
4. Click "Cập Nhật Mật Khẩu"
5. Message: "Password reset successfully"
6. Click link: "Đăng nhập với mật khẩu mới →"

---

## ⏱️ Token Expiration

```
PASSWORD RESET TOKEN LIFETIME
┌──────────────────────────┐
│   Expires in 1 Hour      │
│   (3600 seconds)         │
│                          │
│  ✅ Valid when:          │
│  - Token not expired     │
│  - Token not used        │
│  - User exists           │
│                          │
│  ❌ Invalid when:        │
│  - More than 1h passed   │
│  - Token already used    │
│  - Token doesn't exist   │
└──────────────────────────┘
```

---

## 📧 Email Format

### Reset Email (English)

```
Subject: 🔐 Set Password - E-Commerce

Hello [Full Name],

We received a request to reset the password for your account.

Click the link below to reset your password:
http://localhost:8080/reset-password?token=...

Or copy this code to the password reset form:
[TOKEN]

This link will expire in 1 hour.

If you did not request a password reset, please ignore this email.

Best regards,
E-Commerce Team
```

### Reset Success Email (English)

```
Subject: ✅ Password Reset - E-Commerce

Hello [Full Name],

Your password has been successfully reset.

You can now use your new password to log in to your account.

If you have any questions, please contact us.

Best regards,
E-Commerce Team
```

---

## 🛡️ Security Features

### ✅ Implemented

1. **Token Expiration** - Expires after 1 hour
2. **One-Time Use** - Token marked as used after reset
3. **User Verification** - Email must exist in database
4. **Password Hashing** - New password is hashed (BCrypt)
5. **Confirmation Email** - Sent after successful reset
6. **Password Validation** - Min 6 characters required

### 🔒 Best Practices Applied

- Token is UUID (random and secure)
- Token stored in database (verifiable)
- Email-based verification (only account owner can reset)
- Confirmation email (user knows if account was compromised)
- Token invalidation (old token can't be reused)

---

## ❌ Error Scenarios

### 1. User Not Found
```
Request:
POST /api/auth/forgot-password
{ "email": "nonexistent@example.com" }

Response: 404 Not Found
{
  "error": "User with email not found: nonexistent@example.com"
}
```

### 2. Invalid Token
```
Request:
POST /api/auth/reset-password
{ "token": "invalid-token", ... }

Response: 400 Bad Request
{
  "error": "Invalid or expired token"
}
```

### 3. Token Expired
```
Request:
POST /api/auth/reset-password
{ "token": "expired-token", ... }
(Token was issued more than 1 hour ago)

Response: 400 Bad Request
{
  "error": "Token is invalid or already used"
}
```

### 4. Token Already Used
```
Request:
POST /api/auth/reset-password
{ "token": "already-used-token", ... }
(Token was already used for reset)

Response: 400 Bad Request
{
  "error": "Token is invalid or already used"
}
```

### 5. Passwords Don't Match
```
Request:
POST /api/auth/reset-password
{
  "token": "...",
  "newPassword": "pass123",
  "confirmPassword": "pass456"  // Different!
}

Response: 400 Bad Request
{
  "error": "Passwords do not match"
}
```

### 6. Password Too Short
```
Request:
POST /api/auth/reset-password
{
  "token": "...",
  "newPassword": "pass",  // Only 4 chars
  "confirmPassword": "pass"
}

Response: 400 Bad Request
{
  "error": "Password must be at least 6 characters"
}
```

---

## 🧪 Complete Test Scenario

### Step-by-Step Test

**Prerequisites:** 
- User registered with email: `test@example.com`
- Password: `oldpass123`

**Test Sequence:**

1. **Verify user exists** (Optional login)
```
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "oldpass123"
}
✅ Login successful
```

2. **Request password reset**
```
POST /api/auth/forgot-password
{
  "email": "test@example.com"
}
✅ Email sent (or check console/database)
```

3. **Get reset token**
- From email link, or
- From console log, or
- From database query

4. **Reset password**
```
POST /api/auth/reset-password
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
✅ Password updated
```

5. **Try old password** (should fail)
```
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "oldpass123"
}
❌ Invalid password
```

6. **Try new password** (should succeed)
```
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "newpass123"
}
✅ Login successful with new password!
```

7. **Try reusing token** (should fail)
```
POST /api/auth/reset-password
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "anotherpass",
  "confirmPassword": "anotherpass"
}
❌ Token is invalid or already used
```

---

## 🔧 Configuration (Optional)

### Email Service Configuration

To enable actual email sending, configure in `application.yaml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

### Token Expiration Time

To change token expiration time (currently 1 hour):

Edit `PasswordResetService.java`:
```java
private final long PASSWORD_RESET_TOKEN_EXPIRATION_MS = 3600000; // Change this
// Examples:
// 30 minutes: 1800000
// 2 hours: 7200000
// 24 hours: 86400000
```

---

## 📊 Database Schema

```sql
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_used (used)
);
```

---

## ✅ Checklist

- [ ] Server running: `./gradlew bootRun`
- [ ] Database migration ran (V5)
- [ ] User exists with email
- [ ] Test forgot password API
- [ ] Check token in database or console
- [ ] Test reset password API
- [ ] Verify password changed
- [ ] Verify token marked as used
- [ ] Try reusing token (should fail)
- [ ] Test web forms in browser

---

**Forgot Password Feature Complete! 🎉**

Ready to test and deploy!

