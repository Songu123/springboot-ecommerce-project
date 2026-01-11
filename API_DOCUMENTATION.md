# REST API Documentation

## Base URL
```
http://localhost:8080/api
```

## Category API Endpoints

### GET /api/categories
Lấy danh sách tất cả categories
- **Response**: `200 OK` - List<Category>

### GET /api/categories/{id}
Lấy category theo ID
- **Response**: `200 OK` - Category hoặc `404 Not Found`

### POST /api/categories
Tạo category mới
- **Request Body**: Category (JSON)
- **Response**: `201 Created` - Category

### PUT /api/categories/{id}
Cập nhật category
- **Request Body**: Category (JSON)
- **Response**: `200 OK` - Category hoặc `404 Not Found`

### DELETE /api/categories/{id}
Xóa category
- **Response**: `204 No Content` hoặc `404 Not Found`

---

## Product API Endpoints

### GET /api/products
Lấy danh sách tất cả products
- **Response**: `200 OK` - List<Product>

### GET /api/products/{id}
Lấy product theo ID
- **Response**: `200 OK` - Product hoặc `404 Not Found`

### GET /api/products/category/{categoryId}
Lấy products theo category
- **Response**: `200 OK` - List<Product>

### POST /api/products
Tạo product mới
- **Request Body**: Product (JSON)
- **Response**: `201 Created` - Product

### PUT /api/products/{id}
Cập nhật product
- **Request Body**: Product (JSON)
- **Response**: `200 OK` - Product hoặc `404 Not Found`

### DELETE /api/products/{id}
Xóa product
- **Response**: `204 No Content` hoặc `404 Not Found`

---

## Order API Endpoints

### GET /api/orders
Lấy danh sách tất cả orders
- **Response**: `200 OK` - List<Order>

### GET /api/orders/{id}
Lấy order theo ID
- **Response**: `200 OK` - Order hoặc `404 Not Found`

### GET /api/orders/user/{userId}
Lấy orders theo user
- **Response**: `200 OK` - List<Order>

### GET /api/orders/status/{status}
Lấy orders theo status (NEW, CONFIRMED, PAID, CANCELLED)
- **Response**: `200 OK` - List<Order>

### POST /api/orders
Tạo order mới
- **Request Body**: Order (JSON)
- **Response**: `201 Created` - Order

### PUT /api/orders/{id}
Cập nhật order
- **Request Body**: Order (JSON)
- **Response**: `200 OK` - Order hoặc `404 Not Found`

### PATCH /api/orders/{id}/status
Cập nhật status của order
- **Query Param**: status (String)
- **Response**: `200 OK` - Order hoặc `404 Not Found`

### DELETE /api/orders/{id}
Xóa order
- **Response**: `204 No Content` hoặc `404 Not Found`

---

## OrderItem API Endpoints

### GET /api/order-items
Lấy danh sách tất cả order items
- **Response**: `200 OK` - List<OrderItem>

### GET /api/order-items/{id}
Lấy order item theo ID
- **Response**: `200 OK` - OrderItem hoặc `404 Not Found`

### GET /api/order-items/order/{orderId}
Lấy order items theo order
- **Response**: `200 OK` - List<OrderItem>

### POST /api/order-items
Tạo order item mới
- **Request Body**: OrderItem (JSON)
- **Response**: `201 Created` - OrderItem

### PUT /api/order-items/{id}
Cập nhật order item
- **Request Body**: OrderItem (JSON)
- **Response**: `200 OK` - OrderItem hoặc `404 Not Found`

### DELETE /api/order-items/{id}
Xóa order item
- **Response**: `204 No Content` hoặc `404 Not Found`

---

## User API Endpoints

### GET /api/users
Lấy danh sách tất cả users
- **Response**: `200 OK` - List<User>

### GET /api/users/{id}
Lấy user theo ID
- **Response**: `200 OK` - User hoặc `404 Not Found`

### GET /api/users/email/{email}
Lấy user theo email
- **Response**: `200 OK` - User hoặc `404 Not Found`

### GET /api/users/username/{username}
Lấy user theo username
- **Response**: `200 OK` - User hoặc `404 Not Found`

### POST /api/users
Tạo user mới
- **Request Body**: User (JSON)
- **Response**: `201 Created` - User

### PUT /api/users/{id}
Cập nhật user
- **Request Body**: User (JSON)
- **Response**: `200 OK` - User hoặc `404 Not Found`

### DELETE /api/users/{id}
Xóa user
- **Response**: `204 No Content` hoặc `404 Not Found`

---

## Ví dụ JSON Request/Response

### Category
```json
{
  "id": 1,
  "name": "Electronics",
  "slug": "electronics"
}
```

### Product
```json
{
  "id": 1,
  "name": "Laptop Dell XPS 13",
  "price": 25000000,
  "quantity": 10,
  "image": "laptop.jpg",
  "description": "High performance laptop",
  "category": {
    "id": 1,
    "name": "Electronics",
    "slug": "electronics"
  }
}
```

### Order
```json
{
  "id": 1,
  "totalPrice": 50000000,
  "status": "NEW",
  "createdAt": "2026-01-06T10:30:00",
  "user": {
    "id": 1,
    "username": "john_doe"
  }
}
```

### OrderItem
```json
{
  "id": 1,
  "quantity": 2,
  "price": 25000000,
  "product": {
    "id": 1,
    "name": "Laptop Dell XPS 13"
  }
}
```

### User
```json
{
  "id": 1,
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "hashed_password",
  "enabled": true
}
```

---

## Testing với Postman hoặc cURL

### Ví dụ tạo Category
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics",
    "slug": "electronics"
  }'
```

### Ví dụ lấy tất cả Products
```bash
curl http://localhost:8080/api/products
```

### Ví dụ update Order status
```bash
curl -X PATCH http://localhost:8080/api/orders/1/status?status=CONFIRMED
```

