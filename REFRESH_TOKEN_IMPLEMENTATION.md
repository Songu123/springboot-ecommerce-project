# 🔐 Refresh Token Implementation - Tổng Hợp Thay Đổi

## 📌 Tóm Tắt Tính Năng

Đã thêm hệ thống **Refresh Token** để bảo vệ API với cơ chế:
- **Access Token:** Token ngắn hạn (15 phút) để truy cập API
- **Refresh Token:** Token dài hạn (7 ngày) để lấy Access Token mới

---

## 📂 Danh Sách File Thay Đổi/Tạo Mới

### ✅ File Được Tạo Mới

1. **Entity:**
   - `src/main/java/com/son/ecommerce/entity/RefreshToken.java` - Model lưu refresh tokens

2. **Repository:**
   - `src/main/java/com/son/ecommerce/repository/RefreshTokenRepository.java` - Database operations

3. **Service:**
   - `src/main/java/com/son/ecommerce/service/RefreshTokenService.java` - Xử lý refresh token logic

4. **DTO:**
   - `src/main/java/com/son/ecommerce/dto/RefreshTokenRequest.java` - Request body cho refresh endpoint

5. **Database Migration:**
   - `src/main/resources/db/migration/V4__create_refresh_token_table.sql` - Tạo bảng refresh_token

6. **Documentation:**
   - `REFRESH_TOKEN_GUIDE.md` - Hướng dẫn chi tiết test

### 🔄 File Được Cập Nhật

1. **Security:**
   - `src/main/java/com/son/ecommerce/security/JwtUtil.java`
     - ✅ Thêm `generateAccessToken()` - Tạo access token (15 phút)
     - ✅ Thêm `generateRefreshToken()` - Tạo refresh token (7 ngày)
     - ✅ Thêm `validateToken()` - Kiểm tra token hợp lệ
     - ✅ Thêm `isTokenExpired()` - Kiểm tra token hết hạn

2. **Service:**
   - `src/main/java/com/son/ecommerce/service/AuthService.java`
     - ✅ Thêm `RefreshTokenService` dependency
     - ✅ Thêm `loginWithTokens()` - Return cả access + refresh token
     - ✅ Giữ `login()` cũ để backward compatibility

3. **DTO:**
   - `src/main/java/com/son/ecommerce/dto/AuthResponse.java`
     - ✅ Thêm `accessToken` field
     - ✅ Thêm `refreshToken` field
     - ✅ Thêm `tokenType` field (mặc định "Bearer")
     - ✅ Giữ constructor cũ để backward compatibility

4. **Controller:**
   - `src/main/java/com/son/ecommerce/controller/api/AuthApiController.java`
     - ✅ Cập nhật `/api/auth/login` dùng `loginWithTokens()`
     - ✅ Thêm **`POST /api/auth/refresh`** - Lấy access token mới
     - ✅ Thêm **`POST /api/auth/logout`** - Xóa refresh token

---

## 🎯 API Endpoints

### 1. Register (Đăng Ký)
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "password123",
  "email": "user123@example.com",
  "fullName": "John Doe"
}

Response: 201 Created
{
  "message": "User registered successfully"
}
```

### 2. Login (Đăng Nhập)
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user123@example.com",
  "password": "password123"
}

Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

### 3. Refresh Token (Lấy Access Token Mới)
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}

Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

### 4. Logout (Đăng Xuất)
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}

Response: 200 OK
{
  "message": "Logged out successfully"
}
```

### 5. Protected API Call
```http
GET /api/products
Authorization: Bearer {accessToken}
```

---

## ⏱️ Token Lifespan

| Token | Thời Gian | Mục Đích |
|-------|-----------|---------|
| **Access Token** | 15 phút (900 giây) | Truy cập API |
| **Refresh Token** | 7 ngày (604800 giây) | Lấy access token mới |

---

## 🔄 Quy Trình Hoạt Động

```
┌─────────────────────────────────────────────────────────────┐
│                      AUTHENTICATION FLOW                     │
└─────────────────────────────────────────────────────────────┘

1. USER REGISTERS
   └─> POST /api/auth/register
       └─> User saved to database

2. USER LOGS IN
   └─> POST /api/auth/login
       └─> Generate Access Token (15 min)
       └─> Generate & Save Refresh Token (7 days)
       └─> Return both tokens

3. USER USES API
   └─> Request with Authorization: Bearer {accessToken}
       └─> API validates token
       └─> Token is valid → Proceed
       └─> Token expired → Return 401

4. TOKEN EXPIRED
   └─> POST /api/auth/refresh
       └─> Verify Refresh Token
       └─> Generate new Access Token
       └─> Return new tokens
       └─> Continue using API

5. USER LOGS OUT
   └─> POST /api/auth/logout
       └─> Delete Refresh Token from DB
       └─> Refresh Token invalid

6. USER NEEDS TO LOGIN AGAIN
   └─> Repeat from step 2
```

---

## 📊 Database Schema

```sql
CREATE TABLE refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_token (token),
    INDEX idx_user_id (user_id)
);
```

---

## 🚀 Cách Sử Dụng

### 1. Chạy Server
```bash
cd D:\SpringMVC\son\son
./gradlew bootRun
```

### 2. Test Bằng Postman
- Xem hướng dẫn chi tiết: `REFRESH_TOKEN_GUIDE.md`

### 3. Các Bước:
1. **Register** → Tạo tài khoản
2. **Login** → Lấy tokens
3. **Refresh** → Lấy access token mới (tùy chọn)
4. **Logout** → Xóa refresh token

---

## 🔒 Bảo Vệ

### ✅ Bảo Vệ Được Cung Cấp

1. **Access Token Hết Hạn**
   - Ngắn hạn (15 phút)
   - Giảm rủi ro nếu token bị lộ

2. **Refresh Token Độc Lập**
   - Lưu trong database
   - Có thể xóa khi logout
   - Có thể theo dõi và quản lý

3. **Token Validation**
   - Kiểm tra chữ ký JWT
   - Kiểm tra thời gian hết hạn
   - Xác minh trong database (refresh token)

4. **Logout Thực Sự**
   - Xóa refresh token → Không lấy được access token mới
   - Bắt buộc login lại

### 🛡️ Best Practices (Nên Thêm)

1. **HTTPS Only** - Mã hóa truyền tải
2. **HttpOnly Cookies** - Lưu refresh token an toàn
3. **CORS Policies** - Giới hạn origins
4. **Rate Limiting** - Giới hạn login attempts
5. **Token Blacklist** - Theo dõi tokens bị thu hồi
6. **Rotate Refresh Tokens** - Thay mới refresh token mỗi khi dùng

---

## 🧪 Test Cases

### ✅ Successful Login & Refresh
```
1. Register user
2. Login → Get tokens
3. Refresh → Get new access token
4. Logout → Delete refresh token
```

### ✅ Token Expiration
```
1. Login → Get tokens
2. Wait 15 minutes
3. Try API call → 401 Unauthorized
4. Refresh → Get new token
5. API call → Success
```

### ✅ Invalid Refresh Token
```
1. Logout → Delete refresh token
2. Try refresh → Error: "Refresh token not found"
3. Must login again
```

---

## 🐛 Troubleshooting

| Lỗi | Nguyên Nhân | Giải Pháp |
|-----|-----------|----------|
| `Invalid email` | Email không tồn tại | Register user trước |
| `Invalid password` | Sai password | Kiểm tra password |
| `Refresh token not found` | Token không tồn tại | Login lại |
| `Refresh token was expired` | Token hết hạn (7 ngày) | Login lại |
| `Connection refused` | Server không chạy | `./gradlew bootRun` |
| `401 Unauthorized` | Access token invalid/expired | Dùng refresh để lấy token mới |

---

## 📝 Ghi Chú

### Migration Database
- File migration tự động tạo bảng `refresh_token`
- Flyway sẽ chạy khi server khởi động

### Backward Compatibility
- Hàm `login()` cũ vẫn hoạt động
- Constructor `AuthResponse(String token)` cũ vẫn hỗ trợ
- API cũ vẫn chạy bình thường

### Cấu Hình Token
Để thay đổi thời gian hết hạn:
```java
// File: JwtUtil.java
private final long ACCESS_TOKEN_EXPIRATION = 900000;      // Đổi số này
private final long REFRESH_TOKEN_EXPIRATION = 604800000;  // Đổi số này
```

---

## 📚 Tài Liệu Liên Quan

- `POSTMAN_TEST_GUIDE.md` - Hướng dẫn test login/register cơ bản
- `REFRESH_TOKEN_GUIDE.md` - Hướng dẫn test refresh token chi tiết
- JWT: https://jwt.io/
- OAuth 2.0: https://oauth.net/2/

---

**✅ Implementation Complete! 🎉**

Hệ thống refresh token đã sẵn sàng để sử dụng. Hãy bắt đầu test bằng Postman!

