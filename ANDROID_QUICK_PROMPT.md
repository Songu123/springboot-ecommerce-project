# 🤖 Quick AI Prompt - Android eCommerce App Generator

Copy và paste prompt này vào ChatGPT/Claude/GitHub Copilot để tạo Android app nhanh chóng.

---

## 📋 Prompt cho AI Assistant

```
Tạo ứng dụng Android eCommerce hoàn chỉnh với các yêu cầu sau:

TECH STACK:
- Language: Kotlin
- Architecture: MVVM
- Min SDK: 24, Target SDK: 34
- Libraries: Retrofit, Glide, Coroutines, ViewModel, Navigation Component, Room

BACKEND API:
- Base URL: http://10.0.2.2:8080/ (Android emulator)
- Authentication: Session-based với JSESSIONID cookie
- API Format: REST JSON

API ENDPOINTS:

1. CATEGORIES:
GET /api/categories - Lấy tất cả categories
GET /api/categories/{id} - Lấy category theo ID

Response mẫu:
{
  "id": 1,
  "name": "Electronics",
  "slug": "electronics",
  "products": [...]
}

2. PRODUCTS:
GET /api/products - Lấy tất cả products
GET /api/products/{id} - Lấy product theo ID
GET /api/products/category/{categoryId} - Lấy products theo category

Response mẫu:
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "quantity": 50,
  "image": "laptop.jpg",
  "description": "Gaming laptop",
  "category": {
    "id": 1,
    "name": "Electronics",
    "slug": "electronics"
  }
}

3. ORDERS:
GET /api/orders/user/{userId} - Lấy orders của user
POST /api/orders - Tạo order mới
GET /api/orders/{id} - Lấy order detail

Response mẫu:
{
  "id": 1,
  "totalPrice": 2999.99,
  "status": "NEW",
  "createdAt": "2026-01-12T10:30:00",
  "user": {
    "id": 1,
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com"
  },
  "items": [
    {
      "id": 1,
      "quantity": 2,
      "price": 999.99,
      "product": {...}
    }
  ]
}

4. AUTHENTICATION:
POST /register - Đăng ký (form-urlencoded: username, password, email)
POST /login - Đăng nhập (form-urlencoded: username, password)
POST /logout - Đăng xuất

5. USERS:
GET /api/users/{id} - Lấy user info
PUT /api/users/{id} - Update user

Response mẫu:
{
  "id": 1,
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "ROLE_USER",
  "enabled": true
}

APP FEATURES:
1. Authentication: Login, Register với session management
2. Home: Hiển thị categories và featured products
3. Product List: Grid layout, filter by category, search
4. Product Detail: Image, description, add to cart
5. Shopping Cart: Quản lý giỏ hàng local (chưa cần API)
6. Checkout: Tạo order từ cart items
7. My Orders: Xem lịch sử đơn hàng, filter theo status
8. Profile: Xem và edit thông tin user

PACKAGE STRUCTURE:
com.ecommerce.app/
├── data/
│   ├── model/ (Category, Product, Order, OrderItem, User)
│   ├── remote/ (ApiService, RetrofitClient, AuthInterceptor)
│   └── repository/ (ProductRepository, CategoryRepository, OrderRepository)
├── ui/
│   ├── auth/ (LoginActivity, RegisterActivity)
│   ├── main/ (MainActivity với Bottom Navigation)
│   ├── home/ (HomeFragment)
│   ├── product/ (ProductListFragment, ProductDetailFragment)
│   ├── cart/ (CartFragment)
│   ├── order/ (OrdersFragment, OrderDetailFragment)
│   └── profile/ (ProfileFragment)
├── viewmodel/ (các ViewModels)
└── utils/ (Constants, Resource, PreferenceManager)

YÊU CẦU:
1. Implement AuthInterceptor để tự động thêm JSESSIONID vào headers
2. Sử dụng PreferenceManager để lưu session và user info
3. Implement Resource sealed class cho state management
4. Sử dụng ViewBinding cho tất cả layouts
5. Bottom Navigation với 4 tabs: Home, Cart, Orders, Profile
6. Glide để load images
7. SwipeRefreshLayout cho lists
8. Error handling với user-friendly messages
9. Loading states (ProgressBar hoặc Shimmer)
10. Cart lưu local (SharedPreferences hoặc Room)

HÃY TẠO:
1. build.gradle với tất cả dependencies
2. Tất cả data models
3. RetrofitClient và ApiService hoàn chỉnh
4. AuthInterceptor và PreferenceManager
5. Tất cả repositories với error handling
6. Tất cả ViewModels với LiveData
7. Tất cả layouts (XML) theo Material Design
8. Tất cả Fragments và Activities
9. Navigation graph
10. strings.xml và colors.xml

Ưu tiên code clean, comments đầy đủ, và follow best practices.
```

---

## 🎯 Prompt tạo từng phần cụ thể

### 1. Setup Project & Dependencies
```
Tạo file build.gradle (Module: app) cho Android eCommerce app với:
- Kotlin
- ViewBinding enabled
- Dependencies: Retrofit 2.9.0, Gson converter, OkHttp logging interceptor, Glide 4.16.0, Coroutines, ViewModel, LiveData, Navigation Component, Room, Material Design 3

Sau đó tạo file Constants.kt với BASE_URL = "http://10.0.2.2:8080/"
```

### 2. Data Models
```
Tạo data classes Kotlin cho Android app dựa trên backend Spring Boot entities:

1. Category.kt: id (Long), name (String), slug (String), products (List<Product>?)
2. Product.kt: id (Long), name (String), price (Double), quantity (Int), image (String), description (String), category (Category?)
3. Order.kt: id (Long), totalPrice (Double), status (String), createdAt (String), user (User?), items (List<OrderItem>?)
4. OrderItem.kt: id (Long), quantity (Int), price (Double), product (Product?), order (Order?)
5. User.kt: id (Long), username (String), fullName (String), email (String), role (String), enabled (Boolean)

Tất cả với @SerializedName annotations cho Gson
```

### 3. Retrofit Setup
```
Tạo:
1. ApiService interface với tất cả endpoints của backend API:
   - Categories: GET all, GET by id
   - Products: GET all, GET by id, GET by category
   - Orders: GET by user, POST create, GET by id
   - Auth: POST register, POST login
   - Users: GET by id, PUT update

2. RetrofitClient object với:
   - Logging interceptor
   - Auth interceptor (thêm JSESSIONID cookie)
   - Gson converter
   - Base URL từ Constants

3. AuthInterceptor class: Lấy sessionId từ PreferenceManager và thêm vào header

4. PreferenceManager class: Save/get session, user info, isLoggedIn, clearSession
```

### 4. Repository Layer
```
Tạo repositories với error handling:
1. CategoryRepository: getAllCategories(), getCategoryById()
2. ProductRepository: getAllProducts(), getProductById(), getProductsByCategory()
3. OrderRepository: getOrdersByUser(), createOrder(), getOrderById()
4. AuthRepository: register(), login(), logout()
5. UserRepository: getUserById(), updateUser()

Mỗi function return Resource<T> (Success/Error/Loading)
```

### 5. ViewModel Layer
```
Tạo ViewModels với LiveData:
1. AuthViewModel: login(), register(), logout(), isLoggedIn
2. CategoryViewModel: loadCategories(), categories LiveData
3. ProductViewModel: loadProducts(), loadProductsByCategory(), productDetail LiveData
4. OrderViewModel: loadUserOrders(), createOrder(), orders LiveData
5. UserViewModel: loadUserProfile(), updateProfile()

Sử dụng viewModelScope.launch cho coroutines
```

### 6. UI Layouts
```
Tạo XML layouts cho:
1. activity_main.xml: FrameLayout + BottomNavigationView
2. fragment_home.xml: RecyclerView cho categories, RecyclerView cho products
3. fragment_product_list.xml: SearchView, RecyclerView với GridLayoutManager
4. fragment_product_detail.xml: ScrollView với ImageView, TextViews, Button "Add to Cart"
5. fragment_cart.xml: RecyclerView, TextView total price, Button checkout
6. fragment_orders.xml: TabLayout cho filter, RecyclerView
7. fragment_profile.xml: CardView user info, buttons
8. activity_login.xml: TextInputLayouts, Button
9. activity_register.xml: TextInputLayouts, Button

Tất cả theo Material Design 3 với proper constraints
```

### 7. RecyclerView Adapters
```
Tạo adapters:
1. CategoryAdapter: Hiển thị category name, click listener
2. ProductAdapter: GridLayout, image với Glide, name, price, click listener
3. CartAdapter: LinearLayout, quantity +/-, remove button, swipe to delete
4. OrderAdapter: Order ID, date, status, total, click listener
5. OrderItemAdapter: Product info, quantity, price

Tất cả với ViewHolder pattern và ViewBinding
```

### 8. Fragments Implementation
```
Implement Fragments:
1. HomeFragment: Load categories và products, setup RecyclerViews
2. ProductListFragment: Search, filter, load products
3. ProductDetailFragment: Display product, add to cart functionality
4. CartFragment: Display cart items, calculate total, checkout
5. OrdersFragment: Load user orders, filter tabs
6. ProfileFragment: Display user info, edit profile, logout
```

### 9. Activities Implementation
```
Implement:
1. SplashActivity: Check login status, navigate
2. LoginActivity: Login form, validation, call API
3. RegisterActivity: Register form, validation, call API
4. MainActivity: Setup BottomNavigation, handle fragments
```

### 10. Cart Management
```
Tạo CartManager class:
- Singleton pattern
- Save cart items to SharedPreferences
- Methods: addToCart(), removeFromCart(), updateQuantity(), getCartItems(), getTotalPrice(), clearCart()
- Use Gson để serialize/deserialize cart items
```

---

## 🔥 One-Shot Complete Prompt

```
Tạo HOÀN CHỈNH một Android eCommerce app bằng Kotlin với MVVM architecture.

Backend API: http://10.0.2.2:8080/ (Spring Boot REST API)

Cấu trúc dữ liệu:
- Category: {id, name, slug, products[]}
- Product: {id, name, price, quantity, image, description, category}
- Order: {id, totalPrice, status, createdAt, user, items[]}
- OrderItem: {id, quantity, price, product}
- User: {id, username, fullName, email, role, enabled}

API Endpoints:
GET /api/categories, /api/categories/{id}
GET /api/products, /api/products/{id}, /api/products/category/{categoryId}
GET /api/orders/user/{userId}, POST /api/orders, GET /api/orders/{id}
POST /register (form-data), POST /login (form-data)
GET /api/users/{id}, PUT /api/users/{id}

Features cần có:
✅ Login/Register với session management (JSESSIONID cookie)
✅ Home screen: Categories + Products grid
✅ Product list: Search, filter by category, grid layout 2 columns
✅ Product detail: Image, description, add to cart
✅ Shopping cart: Local storage, quantity adjustment, checkout
✅ Orders: Xem lịch sử orders, filter by status
✅ Profile: View/edit user info, logout
✅ Bottom Navigation: Home, Cart, Orders, Profile

Tech stack:
- Retrofit + OkHttp + Gson
- Coroutines + ViewModel + LiveData
- Glide for images
- Material Design 3
- ViewBinding
- Room (optional for cache)

Tạo TẤT CẢ files cần thiết:
1. build.gradle với dependencies
2. Data models
3. RetrofitClient + ApiService + AuthInterceptor
4. PreferenceManager + CartManager
5. Repositories + ViewModels
6. All XML layouts
7. All Fragments + Activities
8. RecyclerView Adapters
9. Navigation graph
10. Resource files (strings, colors, themes)

Code phải clean, có comments, error handling đầy đủ, loading states, empty states.
```

---

## 💡 Tips sử dụng

1. **Copy toàn bộ prompt** vào AI assistant
2. **Tạo từng phần** nếu muốn control tốt hơn
3. **Test từng layer** trước khi qua layer tiếp theo
4. **Adjust base URL** theo IP máy của bạn nếu test trên device thật

---

**Generated: January 2026**

