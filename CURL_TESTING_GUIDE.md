# 🧪 API TESTING GUIDE - cURL COMMANDS

**Base URL:** `http://localhost:8080`

---

## 1️⃣ AUTHENTICATION

### Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }' | jq '.'
```

💾 **Save the token:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' | jq -r '.accessToken')

echo $TOKEN
```

### Test Token
```bash
curl -X GET http://localhost:8080/api/auth/test \
  -H "Authorization: Bearer $TOKEN"
```

---

## 2️⃣ PRODUCTS

### Get All Products
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"
```

### Get Product by ID
```bash
curl -X GET http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Search Products
```bash
curl -X GET "http://localhost:8080/api/products/search?keyword=hat" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 3️⃣ CART

### Get My Cart
```bash
curl -X GET http://localhost:8080/api/cart/my-cart \
  -H "Authorization: Bearer $TOKEN"
```

### Add Item to Cart
```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 2,
    "quantity": 1
  }'
```

### Update Cart Item
```bash
curl -X PUT http://localhost:8080/api/cart/update \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 2,
    "quantity": 3
  }'
```

### Remove Item from Cart
```bash
curl -X DELETE "http://localhost:8080/api/cart/remove/2" \
  -H "Authorization: Bearer $TOKEN"
```

### Clear Cart
```bash
curl -X DELETE http://localhost:8080/api/cart/clear \
  -H "Authorization: Bearer $TOKEN"
```

---

## 4️⃣ ORDERS

### Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyen Van Son",
    "shippingPhone": "0987654321",
    "shippingAddress": "123 Main Street",
    "shippingDistrict": "District 1",
    "shippingCity": "Ho Chi Minh",
    "shippingPostalCode": "70000",
    "totalPrice": 9918000.0,
    "shippingFee": 30000.0,
    "discount": 0.0,
    "paymentMethod": "COD",
    "note": "Please handle carefully",
    "items": [
      {
        "productId": 2,
        "quantity": 1,
        "price": 9918000.0
      }
    ]
  }' | jq '.'
```

### Get My Orders (User 4)
```bash
curl -X GET "http://localhost:8080/api/orders/user/4" \
  -H "Authorization: Bearer $TOKEN"
```

### Get Order Details
```bash
curl -X GET "http://localhost:8080/api/orders/123" \
  -H "Authorization: Bearer $TOKEN"
```

### Update Order Status
```bash
curl -X PATCH "http://localhost:8080/api/orders/123/status?status=SHIPPED" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "SHIPPED"
  }'
```

---

## 5️⃣ USERS

### Get Current User
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### Update User Profile
```bash
curl -X PUT "http://localhost:8080/api/users/4" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyen Van Son",
    "email": "new_email@example.com"
  }'
```

### Change Password
```bash
curl -X POST http://localhost:8080/api/users/change-password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "password123",
    "newPassword": "newpassword456"
  }'
```

---

## 6️⃣ CATEGORIES

### Get All Categories
```bash
curl -X GET http://localhost:8080/api/categories
```

### Get Products by Category
```bash
curl -X GET "http://localhost:8080/api/categories/1/products"
```

---

## 🔒 SECURITY TESTS

### Test 1: Missing Token (Should fail with 401)
```bash
curl -X GET http://localhost:8080/api/cart/my-cart
# Expected: 401 Unauthorized
```

### Test 2: Invalid Token (Should fail with 401)
```bash
curl -X GET http://localhost:8080/api/cart/my-cart \
  -H "Authorization: Bearer invalid_token_here"
# Expected: 401 Unauthorized
```

### Test 3: Access Another User's Orders (Should fail with 403)
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@test.com","password":"pass123"}' | jq -r '.accessToken')

# User 1 tries to access User 2's orders (userId=2)
curl -X GET "http://localhost:8080/api/orders/user/2" \
  -H "Authorization: Bearer $TOKEN"
# Expected: 403 Forbidden
```

### Test 4: Access Own Orders (Should succeed)
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"vanson@example.com","password":"password123"}' | jq -r '.accessToken')

# Get current user ID
USER_ID=$(curl -s -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN" | jq '.id')

# Get own orders
curl -X GET "http://localhost:8080/api/orders/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN"
# Expected: 200 OK with orders list
```

---

## 📊 FULL WORKFLOW TEST

### Step 1: Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "workflow_test",
    "password": "testpass123",
    "email": "workflow@test.com"
  }'
```

### Step 2: Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"workflow@test.com","password":"testpass123"}' | jq -r '.accessToken')

echo "Token: $TOKEN"
```

### Step 3: Get Products
```bash
curl -s -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" | jq '.[0]'
```

### Step 4: Add to Cart
```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 1}'
```

### Step 5: Get My Cart
```bash
curl -s -X GET http://localhost:8080/api/cart/my-cart \
  -H "Authorization: Bearer $TOKEN" | jq '.cart.items'
```

### Step 6: Create Order
```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "shippingPhone": "0123456789",
    "shippingAddress": "Test Address",
    "shippingCity": "Test City",
    "totalPrice": 1012000.0,
    "shippingFee": 30000.0,
    "paymentMethod": "COD",
    "items": [
      {
        "productId": 1,
        "quantity": 1,
        "price": 1012000.0
      }
    ]
  }' | jq '.id'
```

### Step 7: Get My Orders
```bash
USER_ID=$(curl -s -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN" | jq '.id')

curl -s -X GET "http://localhost:8080/api/orders/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '.[0]'
```

---

## 💡 USEFUL jq FILTERS

### Pretty print JSON
```bash
curl -s ... | jq '.'
```

### Extract specific field
```bash
curl -s ... | jq '.accessToken'
```

### Extract from array
```bash
curl -s ... | jq '.[0].id'
```

### Filter array
```bash
curl -s ... | jq '.[] | select(.status == "PENDING")'
```

---

## 📋 RESPONSE CODES QUICK CHECK

```bash
# Check HTTP status code
curl -w "\nStatus: %{http_code}\n" -s -X GET http://localhost:8080/api/cart/my-cart \
  -H "Authorization: Bearer $TOKEN"

# Expected responses:
# 200 = OK
# 201 = Created
# 400 = Bad Request
# 401 = Unauthorized (no token)
# 403 = Forbidden (wrong user)
# 404 = Not Found
# 500 = Server Error
```

---

**Last Updated:** April 2, 2026

