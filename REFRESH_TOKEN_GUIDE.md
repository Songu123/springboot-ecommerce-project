# Hướng Dẫn Test API Với Refresh Token

## 📌 Giới Thiệu Refresh Token

**Refresh Token** là một token dài hạn dùng để lấy **Access Token** mới khi Access Token hết hạn.

### Cơ Chế Bảo Vệ:
```
1. Đăng nhập → Nhận Access Token (15 phút) + Refresh Token (7 ngày)
2. Sử dụng Access Token để gọi API
3. Access Token hết hạn → Dùng Refresh Token để lấy Access Token mới
4. Logout → Xóa Refresh Token
```

---

## 🚀 Quy Trình Test Từng Bước

### **Step 1: REGISTER (Đăng Ký)**

- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/register`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (JSON):**
  ```json
  {
    "username": "user123",
    "password": "password123",
    "email": "user123@example.com",
    "fullName": "John Doe"
  }
  ```
- **Response Thành Công (201 Created):**
  ```json
  {
    "message": "User registered successfully"
  }
  ```

---

### **Step 2: LOGIN (Đăng Nhập & Nhận Tokens)**

- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (JSON):**
  ```json
  {
    "email": "user123@example.com",
    "password": "password123"
  }
  ```
- **Response Thành Công (200 OK):**
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzE0NTc1MjAwfQ.xxxx",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer"
  }
  ```

**⚠️ QUAN TRỌNG:** Lưu **accessToken** và **refreshToken** từ response này!

---

### **Step 3: REFRESH TOKEN (Lấy Access Token Mới)**

Khi Access Token hết hạn (sau 15 phút), dùng Refresh Token để lấy Token mới **mà không cần login lại**.

- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/refresh`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (JSON):**
  ```json
  {
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }
  ```
  *(Paste refreshToken từ Step 2)*

- **Response Thành Công (200 OK):**
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzE0NTc1MzAwfQ.yyyy",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer"
  }
  ```

**✅ Bạn đã có Access Token mới, tiếp tục sử dụng API!**

---

### **Step 4: SỬ DỤNG ACCESS TOKEN (Gọi Protected APIs)**

Để gọi các API cần xác thực, thêm header:

```
Authorization: Bearer {accessToken}
```

Ví dụ:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzE0NTc1MzAwfQ.yyyy
```

---

### **Step 5: LOGOUT (Đăng Xuất)**

Xóa Refresh Token để vô hiệu hóa nó:

- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/logout`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (JSON):**
  ```json
  {
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }
  ```

- **Response Thành Công (200 OK):**
  ```json
  {
    "message": "Logged out successfully"
  }
  ```

**⚠️ Refresh Token này sẽ không còn được sử dụng!**

---

## 📋 Tóm Tắt Các Endpoint

| Endpoint | Method | Mục Đích | Token Cần? |
|----------|--------|---------|-----------|
| `/api/auth/register` | POST | Đăng ký | ❌ Không |
| `/api/auth/login` | POST | Đăng nhập | ❌ Không |
| `/api/auth/refresh` | POST | Làm mới access token | ❌ Không |
| `/api/auth/logout` | POST | Đăng xuất | ❌ Không |
| `/api/auth/test` | GET | Kiểm tra API | ❌ Không |

---

## 🔒 Bảo Vệ API Endpoints

Khi tạo các API khác cần bảo vệ, thêm header Authorization:

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer {accessToken}"
```

---

## ⏱️ Token Expiration Times

- **Access Token:** 15 phút (900 giây)
- **Refresh Token:** 7 ngày (604800 giây)

---

## ❌ Lỗi Phổ Biến & Giải Pháp

### 1. "Invalid email" hoặc "Invalid password"
- **Nguyên Nhân:** Email không đúng hoặc password sai
- **Giải Pháp:** Kiểm tra lại thông tin đăng nhập

### 2. "Refresh token not found"
- **Nguyên Nhân:** Refresh token không tồn tại hoặc đã bị xóa
- **Giải Pháp:** Login lại để lấy refresh token mới

### 3. "Refresh token was expired"
- **Nguyên Nhân:** Refresh token hết hạn (quá 7 ngày)
- **Giải Pháp:** Login lại với tài khoản của bạn

### 4. "Connection refused" (Port 8080)
- **Nguyên Nhân:** Server không chạy
- **Giải Pháp:** 
  ```bash
  cd D:\SpringMVC\son\son
  ./gradlew bootRun
  ```

---

## 🧪 Kịch Bản Test Hoàn Chỉnh

### **Scenario 1: Login & Refresh Token**

```
1. POST /api/auth/register
   Input: { username, password, email, fullName }
   
2. POST /api/auth/login
   Input: { email, password }
   Output: { accessToken, refreshToken, tokenType }
   
3. POST /api/auth/refresh
   Input: { refreshToken }
   Output: { accessToken (mới), refreshToken, tokenType }
   
4. POST /api/auth/logout
   Input: { refreshToken }
   Output: { message: "Logged out successfully" }
```

### **Scenario 2: Access Token Hết Hạn**

```
1. Login → Nhận access token (15 phút)
2. Chờ 15 phút hoặc modify token để test
3. Gọi API có Authorization header → Lỗi "Token expired"
4. Gọi /api/auth/refresh với refreshToken → Nhận access token mới
5. Gọi API lại với access token mới → Thành công ✅
```

---

## 💡 Best Practices

### ✅ Làm Nên
1. Lưu **accessToken** ở bộ nhớ RAM (không local storage nếu có XSS risk)
2. Lưu **refreshToken** ở `HttpOnly Cookie` (an toàn hơn)
3. Gửi accessToken trong header `Authorization: Bearer`
4. Refresh token tự động khi hết hạn
5. Logout khi người dùng thoát

### ❌ Không Nên
1. Lưu tokens ở URL hoặc query parameters
2. Lưu refreshToken ở localStorage (dễ bị XSS steal)
3. Gửi token ở request body
4. Sử dụng token hết hạn
5. Chia sẻ token giữa các users

---

## 🔐 Cấu Hình Token (Nếu Cần Thay Đổi)

Edit file `JwtUtil.java`:

```java
private final long ACCESS_TOKEN_EXPIRATION = 900000;      // 15 phút
private final long REFRESH_TOKEN_EXPIRATION = 604800000;  // 7 ngày
```

---

## 📚 Tài Liệu Thêm

- **JWT Documentation:** https://jwt.io/
- **OAuth 2.0 Refresh Token:** https://tools.ietf.org/html/rfc6749#section-6
- **Postman Documentation:** https://learning.postman.com/

---

**Chúc bạn test thành công! 🎉**

Nếu có vấn đề, hãy kiểm tra:
1. Server đang chạy?
2. URL đúng?
3. Headers đúng?
4. Body JSON hợp lệ?

