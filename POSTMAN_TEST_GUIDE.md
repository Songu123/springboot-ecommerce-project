# Hướng Dẫn Test API Bằng Postman

## 1. Cài Đặt Postman

- Tải về từ: https://www.postman.com/downloads/
- Cài đặt và mở ứng dụng

## 2. Tạo Collection Mới

1. Mở Postman
2. Click "Create a new collection"
3. Đặt tên: "E-Commerce Auth API"
4. Click "Create"

---

## 3. TEST REGISTER (Đăng Ký)

### Tạo Request Mới

1. Click "+ Add request" trong collection
2. Đặt tên: "Register User"
3. Chọn method: **POST**
4. URL: `http://localhost:8080/api/auth/register`

### Headers

```
Content-Type: application/json
```

### Body (JSON)

```json
{
  "username": "user123",
  "password": "password123",
  "email": "user123@example.com",
  "fullName": "John Doe"
}
```

### Cách Nhập:
1. Click vào tab "Body"
2. Chọn "raw"
3. Chọn "JSON" từ dropdown
4. Paste JSON ở trên
5. Click "Send"

### Response Thành Công (201 Created):
```json
{
  "message": "User registered successfully"
}
```

---

## 4. TEST LOGIN (Đăng Nhập)

### Tạo Request Mới

1. Click "+ Add request" trong collection
2. Đặt tên: "Login User"
3. Chọn method: **POST**
4. URL: `http://localhost:8080/api/auth/login`

### Headers

```
Content-Type: application/json
```

### Body (JSON)
**Sử dụng email từ bước register:**

```json
{
  "email": "user123@example.com",
  "password": "password123"
}
```

### Cách Nhập:
1. Click vào tab "Body"
2. Chọn "raw"
3. Chọn "JSON" từ dropdown
4. Paste JSON ở trên
5. Click "Send"

### Response Thành Công (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzE0NTc1MjAwfQ.xxxx"
}
```

---

## 5. TEST FORM LOGIN (Đăng Nhập Qua Form)

### Tạo Request Mới

1. Click "+ Add request" trong collection
2. Đặt tên: "Login Form"
3. Chọn method: **POST**
4. URL: `http://localhost:8080/login`

### Headers

```
Content-Type: application/x-www-form-urlencoded
```

### Body (Form Data)

| Key | Value |
|-----|-------|
| email | user123@example.com |
| password | password123 |

### Cách Nhập:
1. Click vào tab "Body"
2. Chọn "form-data"
3. Nhập key và value như bảng trên
4. Click "Send"

### Response:
Sẽ redirect đến `/home` (Status 302)

---

## 6. TEST REGISTER FORM (Đăng Ký Qua Form)

### Tạo Request Mới

1. Click "+ Add request" trong collection
2. Đặt tên: "Register Form"
3. Chọn method: **POST**
4. URL: `http://localhost:8080/register`

### Headers

```
Content-Type: application/x-www-form-urlencoded
```

### Body (Form Data)

| Key | Value |
|-----|-------|
| username | user456 |
| password | password456 |
| email | user456@example.com |
| fullName | Jane Doe |

### Cách Nhập:
1. Click vào tab "Body"
2. Chọn "form-data"
3. Nhập các key-value như bảng trên
4. Click "Send"

### Response:
Sẽ redirect đến `/login` (Status 302)

---

## 7. Thứ Tự Test Đúng

1. **Trước tiên: Register User** → Tạo tài khoản mới
2. **Sau đó: Login User** → Lấy JWT token
3. **Kiểm tra:** Token sẽ được sử dụng cho các request khác

---

## 8. Ghi Chú Quan Trọng

### ❌ Lỗi Phổ Biến:

| Lỗi | Nguyên Nhân | Giải Pháp |
|-----|-----------|----------|
| `Invalid email` | Email không tồn tại | Register user trước |
| `Invalid password` | Sai password | Kiểm tra lại password |
| `Email already exists` | Email đã đăng ký | Dùng email khác |
| `Username already exists` | Username đã tồn tại | Dùng username khác |
| `Connection refused` | Server không chạy | Start server: `./gradlew bootRun` |

### ✅ Kiểm Tra Server:

Trước khi test, đảm bảo server đang chạy:

```bash
cd D:\SpringMVC\son\son
./gradlew bootRun
```

Nếu thành công, bạn sẽ thấy:
```
Started SonApplication in X.XXX seconds
```

---

## 9. Environment Variables (Optional)

Bạn có thể lưu base URL để sử dụng lại:

1. Click "Environment" (góc trên bên phải)
2. Click "Create environment"
3. Đặt tên: "Development"
4. Thêm variable:
   - Variable: `base_url`
   - Value: `http://localhost:8080`

5. Sử dụng trong URL: `{{base_url}}/api/auth/login`

---

## 10. Ví Dụ Kịch Bản Test Hoàn Chỉnh

### Scenario: Đăng ký và đăng nhập người dùng mới

**Step 1: Register**
- Method: POST
- URL: http://localhost:8080/api/auth/register
- Body:
```json
{
  "username": "testuser",
  "password": "test123",
  "email": "test@example.com",
  "fullName": "Test User"
}
```

**Step 2: Login**
- Method: POST
- URL: http://localhost:8080/api/auth/login
- Body:
```json
{
  "email": "test@example.com",
  "password": "test123"
}
```

**Step 3: Kiểm tra Token**
- Copy token từ response bước 2
- Sử dụng token này trong header `Authorization: Bearer <token>` cho các request khác

---

## 11. Reset Data (Nếu Cần)

Nếu muốn xóa tất cả users để test lại:

1. Xóa database hoặc
2. Run SQL: `DELETE FROM user;` (qua DB client)
3. Khởi động lại server

---

**Chúc bạn test thành công! 🎉**

