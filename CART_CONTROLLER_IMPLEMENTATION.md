# 🛒 Xử Lý CartController - Implementation Guide

## 📋 Tổng Quan Cập Nhật

Đã cập nhật CartController để:
- ✅ Sử dụng `@AuthenticationPrincipal CustomUserDetails` để lấy thông tin user
- ✅ CustomUserDetails chứa đầy đủ thông tin: `id`, `username`, `email`, `fullName`, `roles`
- ✅ Kiểm tra null currentUser trước mỗi operation
- ✅ Thêm logging chi tiết cho debugging
- ✅ Redirect về /login nếu user chưa đăng nhập

---

## 🔑 Cấu Trúc CustomUserDetails

### CustomUserDetails.java
```java
public class CustomUserDetails implements UserDetails {
    private User user;

    // ✅ Các method đã implement:
    public Long getId()                      // Lấy user ID
    public String getFullName()              // Lấy tên đầy đủ
    public String getEmail()                 // Lấy email
    public Collection<? extends GrantedAuthority> getAuthorities()  // Lấy roles/permissions
    public String getPassword()              // Lấy password hash
    public String getUsername()              // Lấy username
    public boolean isEnabled()               // Kiểm tra user enabled
    public boolean isAccountNonExpired()     // Account chưa hết hạn
    public boolean isAccountNonLocked()      // Account chưa bị khóa
    public boolean isCredentialsNonExpired() // Credentials chưa hết hạn
}
```

---

## 🔐 CustomUserDetailsService.java

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        // ✅ Tìm user theo username hoặc email
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(...));
        
        // ✅ Return CustomUserDetails object
        return new CustomUserDetails(user);
    }
}
```

**Features:**
- Tìm user theo username HOẶC email
- Trả về CustomUserDetails object (chứa full user info)
- Tự động lấy roles và permissions

---

## 🛒 CartController Methods

### 1. GET /cart - View Cart
```java
@GetMapping
public String viewCart(@AuthenticationPrincipal CustomUserDetails currentUser, Model model)
```

**Flow:**
1. ✅ Kiểm tra nếu currentUser == null → redirect /login
2. ✅ Lấy userId từ currentUser.getId()
3. ✅ Load cart items từ database
4. ✅ Tính toán subtotal, shipping fee, total
5. ✅ Add tất cả info vào model:
   - `cartItems` - Danh sách items
   - `subtotal` - Tổng giá sản phẩm
   - `shippingFee` - Phí vận chuyển (free nếu > $50)
   - `total` - Tổng cộng
   - `currentUser` - User object
   - `userId`, `userEmail`, `fullName`

**Response:**
```html
<!-- Thymeleaf template nhận được: -->
<div th:if="${currentUser}">
  <p>Welcome, <span th:text="${currentUser.fullName}"></span>!</p>
  <p>Email: <span th:text="${currentUser.email}"></span></p>
  <p>Cart Items: <span th:text="${cartItems.size()}"></span></p>
  <p>Total: $<span th:text="${total}"></span></p>
</div>
```

---

### 2. POST /cart/update/{productId} - Update Item
```java
@PostMapping("/update/{productId}")
public String updateCartItem(
    @PathVariable Long productId,
    @RequestParam int quantity,
    @AuthenticationPrincipal CustomUserDetails currentUser,
    RedirectAttributes redirectAttributes)
```

**Flow:**
1. ✅ Kiểm tra currentUser null
2. ✅ Nếu quantity > 0: cập nhật số lượng
3. ✅ Nếu quantity <= 0: xóa sản phẩm
4. ✅ Set flash message success/error
5. ✅ Redirect /cart

**Example Request:**
```html
<form action="/cart/update/5" method="POST">
  <input type="number" name="quantity" value="3">
  <button type="submit">Update</button>
</form>
```

---

### 3. GET /cart/remove/{productId} - Remove Item
```java
@GetMapping("/remove/{productId}")
public String removeItem(
    @PathVariable Long productId,
    @AuthenticationPrincipal CustomUserDetails currentUser,
    RedirectAttributes redirectAttributes)
```

**Flow:**
1. ✅ Kiểm tra currentUser null
2. ✅ Xóa product từ cart
3. ✅ Set flash message
4. ✅ Redirect /cart

**Example Link:**
```html
<a href="/cart/remove/5" class="btn btn-danger">Remove</a>
```

---

### 4. GET /cart/clear - Clear Cart
```java
@GetMapping("/clear")
public String clearCart(
    @AuthenticationPrincipal CustomUserDetails currentUser,
    RedirectAttributes redirectAttributes)
```

**Flow:**
1. ✅ Kiểm tra currentUser null
2. ✅ Xóa tất cả items từ cart
3. ✅ Set flash message
4. ✅ Redirect /cart

---

### 5. POST /cart/apply-coupon - Apply Coupon
```java
@PostMapping("/apply-coupon")
public String applyCoupon(
    @RequestParam String couponCode,
    @AuthenticationPrincipal CustomUserDetails currentUser,
    RedirectAttributes redirectAttributes)
```

**Status:** ⏳ Coming soon
**Future Implementation:** Coupon validation, discount calculation

---

## 🧪 Testing

### Scenario 1: User Not Logged In
```
1. Try to visit /cart
2. ❌ currentUser == null
3. ✅ Redirect to /login
4. User sees: "Please login to view your cart"
```

### Scenario 2: User Logged In - View Cart
```
1. User logged in with email: user@example.com
2. GET /cart
3. ✅ currentUser loaded with all info
4. Display:
   - Welcome message with fullName
   - Email: user@example.com
   - Cart items count
   - Prices and totals
   - Update/Remove buttons
```

### Scenario 3: Update Item Quantity
```
1. User logged in
2. POST /cart/update/5?quantity=3
3. ✅ cartService.updateCartItemQuantity(userId, 5, 3)
4. Flash message: "Cart updated successfully!"
5. Redirect /cart
```

### Scenario 4: Remove Item
```
1. User logged in
2. GET /cart/remove/5
3. ✅ cartService.removeItemFromCart(userId, 5)
4. Flash message: "Item removed from cart!"
5. Redirect /cart
```

---

## 📊 Database Flow

```
┌─────────────────────────────────────────────┐
│  User Logs In                               │
│  POST /api/auth/login                       │
├─────────────────────────────────────────────┤
│ CustomUserDetailsService.loadUserByUsername │
│ → Tìm user theo username/email              │
│ → Return new CustomUserDetails(user)        │
├─────────────────────────────────────────────┤
│ Spring Security tạo Authentication token    │
│ → Lưu vào SecurityContext                   │
│ → User đã authenticated!                    │
├─────────────────────────────────────────────┤
│ User Request GET /cart                      │
│ @AuthenticationPrincipal CustomUserDetails  │
│ → Spring inject CustomUserDetails object    │
│ → CartController có full user info          │
├─────────────────────────────────────────────┤
│ cartService.getCartItems(userId)            │
│ → Query: SELECT * FROM cart_item            │
│   WHERE cart.user_id = userId               │
│ → Return list of CartItemEntity             │
├─────────────────────────────────────────────┤
│ Calculate totals                            │
│ → subtotal = Σ(price × quantity)            │
│ → shippingFee = subtotal > 50 ? 0 : 10      │
│ → total = subtotal + shipping - discount    │
├─────────────────────────────────────────────┤
│ Render template: layout/main.html            │
│ → Thymeleaf có access tới model attributes   │
│ → currentUser.fullName, currentUser.email    │
│ → cartItems, subtotal, total                 │
└─────────────────────────────────────────────┘
```

---

## 🔍 Logging Output

### Successful Cart Load
```
✅ [CartController] Viewing cart for user: 1
✅ [CartController] Cart loaded with 3 items
```

### User Not Authenticated
```
❌ [CartController] User not authenticated
→ Redirect to /login
```

### Update Item
```
✅ [CartController] Updating cart for user: 1, productId: 5, quantity: 3
✅ [CartController] Item updated successfully
```

### Error Handling
```
❌ [CartController] Error loading cart: Product not found
→ Display error message: "Failed to load cart: Product not found"
```

---

## 🛠️ Thymeleaf Template Integration

### cart.html (Fragment)
```html
<!-- Check user logged in -->
<div th:if="${currentUser}">
  
  <!-- Display user info -->
  <div class="user-info">
    <p>Hi, <span th:text="${currentUser.fullName}"></span>!</p>
    <p>Email: <span th:text="${currentUser.email}"></span></p>
  </div>
  
  <!-- Display cart items -->
  <table th:if="${cartItems != null and cartItems.size() > 0}">
    <tbody>
      <tr th:each="item : ${cartItems}">
        <td th:text="${item.product.name}"></td>
        <td>
          <form action="/cart/update/" method="POST" style="display:inline;">
            <input type="hidden" name="productId" th:value="${item.product.id}">
            <input type="number" name="quantity" th:value="${item.quantity}" min="1">
            <button type="submit">Update</button>
          </form>
        </td>
        <td th:text="${item.quantity * item.product.price}"></td>
        <td>
          <a th:href="@{/cart/remove/{id}(id=${item.product.id})}" 
             class="btn btn-danger">Remove</a>
        </td>
      </tr>
    </tbody>
  </table>
  
  <!-- Display totals -->
  <div class="cart-totals">
    <p>Subtotal: $<span th:text="${subtotal}"></span></p>
    <p>Shipping: $<span th:text="${shippingFee}"></span></p>
    <p><strong>Total: $<span th:text="${total}"></span></strong></p>
  </div>
  
  <!-- Action buttons -->
  <button onclick="location.href='/cart/clear'" class="btn btn-warning">Clear Cart</button>
  <button onclick="location.href='/checkout'" class="btn btn-success">Checkout</button>
  
</div>

<!-- User not logged in -->
<div th:unless="${currentUser}">
  <p>Please <a href="/login">login</a> to view your cart.</p>
</div>
```

---

## ✅ Implementation Checklist

- [x] CustomUserDetails fully implemented
- [x] CustomUserDetailsService returns CustomUserDetails
- [x] CartController uses @AuthenticationPrincipal
- [x] All methods check null currentUser
- [x] Add comprehensive logging
- [x] Handle errors gracefully
- [x] Redirect to /login for unauthenticated users
- [x] Pass user info to view layer
- [x] Calculate totals correctly

---

## 🚀 Next Steps

1. **Checkout Functionality** - Implement order processing
2. **Payment Integration** - Connect to payment gateway
3. **Order Confirmation** - Send email confirmation
4. **Coupon System** - Implement coupon validation
5. **Wishlist** - Add product to wishlist feature


