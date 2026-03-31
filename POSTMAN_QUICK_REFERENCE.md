# 🚀 Postman Quick Reference - Refresh Token

## Copy-Paste Ready Commands

### 1️⃣ REGISTER
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "password123",
  "email": "user123@example.com",
  "fullName": "John Doe"
}
```

### 2️⃣ LOGIN (Lấy Tokens)
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user123@example.com",
  "password": "password123"
}
```
**👉 SAVE Token từ response!**

### 3️⃣ REFRESH (Lấy Access Token Mới)
```
POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "PASTE_REFRESH_TOKEN_HERE"
}
```

### 4️⃣ LOGOUT (Xóa Refresh Token)
```
POST http://localhost:8080/api/auth/logout
Content-Type: application/json

{
  "refreshToken": "PASTE_REFRESH_TOKEN_HERE"
}
```

### 5️⃣ USE API (Gọi Protected Endpoint)
```
GET http://localhost:8080/api/products
Authorization: Bearer PASTE_ACCESS_TOKEN_HERE
```

---

## 📋 Postman Setup Steps

### Step 1: Create Collection
- File → New Collection → "E-Commerce API"

### Step 2: Add Requests (5 requests)
```
├── Register
├── Login
├── Refresh
├── Logout
└── Test API
```

### Step 3: Set Variables (Optional)
```
baseUrl: http://localhost:8080
accessToken: (paste from login response)
refreshToken: (paste from login response)
```

### Step 4: Use in Requests
```
URL: {{baseUrl}}/api/auth/login
Header: Authorization: Bearer {{accessToken}}
```

---

## 🔄 Recommended Test Flow

```
1. POST Register
   ↓ (Success: 201)
   
2. POST Login
   ↓ (Success: 200) Save tokens!
   
3. POST Refresh
   ↓ (Success: 200) Get new access token
   
4. GET Test API (with accessToken)
   ↓ (Success: 200)
   
5. POST Logout
   ↓ (Success: 200)
   
6. POST Refresh (with same refreshToken)
   ↓ (Error: Token not found) ✓ Expected!
   
7. POST Login Again
   ✓ Success! Got new tokens
```

---

## ✅ Expected Responses

### Login Success
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

### Refresh Success
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

### Logout Success
```json
{
  "message": "Logged out successfully"
}
```

### Error Response
```json
{
  "error": "Invalid email"
}
```

---

## 🎯 Token Times

- **Access Token:** Expires in 15 minutes
- **Refresh Token:** Expires in 7 days

---

## 💾 Environment Variables (Postman)

```javascript
// In Postman Tests tab (after login):
pm.globals.set("accessToken", pm.response.json().accessToken);
pm.globals.set("refreshToken", pm.response.json().refreshToken);
```

Then use in headers:
```
Authorization: Bearer {{accessToken}}
```

---

## ❌ Common Errors

| Error | Fix |
|-------|-----|
| `Invalid email` | Email not registered - Register first |
| `Invalid password` | Wrong password - Try again |
| `Refresh token not found` | Token expired/deleted - Login again |
| `Connection refused` | Server not running - Run `./gradlew bootRun` |

---

## 🔒 Security Tips

1. ✅ Always use HTTPS in production
2. ✅ Store refreshToken in HttpOnly cookies
3. ✅ Store accessToken in memory (RAM)
4. ✅ Send accessToken in Authorization header
5. ✅ Refresh token automatically when expired
6. ✅ Logout when user exits app

---

**Happy Testing! 🎉**

