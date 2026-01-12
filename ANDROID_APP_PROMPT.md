# 📱 PROMPT: Android eCommerce App với Spring Boot Backend API

## 🎯 Mục tiêu dự án

Tạo ứng dụng Android eCommerce hoàn chỉnh sử dụng Kotlin/Java với kiến trúc MVVM, kết nối với Spring Boot REST API backend đã có sẵn.

---

## 🏗️ Kiến trúc & Công nghệ

### **Tech Stack**
- **Language**: Kotlin (ưu tiên) hoặc Java
- **Architecture**: MVVM (Model-View-ViewModel)
- **Minimum SDK**: Android 7.0 (API Level 24)
- **Target SDK**: Android 14 (API Level 34)

### **Thư viện chính**
```gradle
// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'

// Image Loading
implementation 'com.github.bumptech.glide:glide:4.16.0'
kapt 'com.github.bumptech.glide:compiler:4.16.0'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'

// Lifecycle & ViewModel
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'

// Navigation Component
implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'

// Room Database (cho cache)
implementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'

// Material Design
implementation 'com.google.android.material:material:1.11.0'

// ViewBinding/DataBinding
viewBinding { enabled = true }

// Security (SharedPreferences mã hóa)
implementation 'androidx.security:security-crypto:1.1.0-alpha06'

// SwipeRefreshLayout
implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
```

---

## 🌐 Backend API Configuration

### **Base URL**
```kotlin
const val BASE_URL = "http://10.0.2.2:8080/"  // Android Emulator
// hoặc
const val BASE_URL = "http://192.168.1.x:8080/" // Device thật
```

### **Server Info**
- Port: `8080`
- Database: MySQL (`ecommerce_db_new`)
- Authentication: Spring Security (Session-based)

---

## 📋 API Endpoints Documentation

### **1. Authentication APIs**

#### Register User
```http
POST /register
Content-Type: application/x-www-form-urlencoded

Parameters:
- username: String (required)
- password: String (required)
- email: String (optional)

Response: 302 Redirect to /login
```

#### Login
```http
POST /login
Content-Type: application/x-www-form-urlencoded

Parameters:
- username: String
- password: String

Response: 302 Redirect on success
```

#### Logout
```http
POST /logout

Response: 302 Redirect to /login
```

---

### **2. Category APIs**

#### Get All Categories
```http
GET /api/categories

Response: 200 OK
[
  {
    "id": 1,
    "name": "Electronics",
    "slug": "electronics",
    "products": [...]  // Có thể null nếu lazy loading
  }
]
```

#### Get Category By ID
```http
GET /api/categories/{id}

Response: 200 OK
{
  "id": 1,
  "name": "Electronics",
  "slug": "electronics",
  "products": [...]
}

Error: 404 Not Found
```

#### Create Category (Admin)
```http
POST /api/categories
Content-Type: application/json

Body:
{
  "name": "New Category",
  "slug": "new-category"
}

Response: 201 Created
```

#### Update Category (Admin)
```http
PUT /api/categories/{id}
Content-Type: application/json

Body:
{
  "name": "Updated Category",
  "slug": "updated-category"
}

Response: 200 OK
Error: 404 Not Found
```

#### Delete Category (Admin)
```http
DELETE /api/categories/{id}

Response: 204 No Content
Error: 404 Not Found
```

---

### **3. Product APIs**

#### Get All Products
```http
GET /api/products

Response: 200 OK
[
  {
    "id": 1,
    "name": "Product Name",
    "price": 999.99,
    "quantity": 50,
    "image": "image-url.jpg",
    "description": "Product description",
    "category": {
      "id": 1,
      "name": "Electronics",
      "slug": "electronics"
    }
  }
]
```

#### Get Product By ID
```http
GET /api/products/{id}

Response: 200 OK
{
  "id": 1,
  "name": "Product Name",
  "price": 999.99,
  "quantity": 50,
  "image": "image-url.jpg",
  "description": "Product description",
  "category": {...}
}

Error: 404 Not Found
```

#### Get Products By Category
```http
GET /api/products/category/{categoryId}

Response: 200 OK
[
  {
    "id": 1,
    "name": "Product Name",
    ...
  }
]
```

#### Create Product (Admin)
```http
POST /api/products
Content-Type: application/json

Body:
{
  "name": "New Product",
  "price": 999.99,
  "quantity": 100,
  "image": "image-url.jpg",
  "description": "Product description",
  "category": {
    "id": 1
  }
}

Response: 201 Created
```

#### Update Product (Admin)
```http
PUT /api/products/{id}
Content-Type: application/json

Body:
{
  "name": "Updated Product",
  "price": 1299.99,
  "quantity": 75,
  "image": "new-image-url.jpg",
  "description": "Updated description",
  "category": {
    "id": 1
  }
}

Response: 200 OK
Error: 404 Not Found
```

#### Delete Product (Admin)
```http
DELETE /api/products/{id}

Response: 204 No Content
Error: 404 Not Found
```

---

### **4. Order APIs**

#### Get All Orders
```http
GET /api/orders

Response: 200 OK
[
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
        "product": {
          "id": 1,
          "name": "Product Name",
          ...
        }
      }
    ]
  }
]
```

#### Get Order By ID
```http
GET /api/orders/{id}

Response: 200 OK
{
  "id": 1,
  "totalPrice": 2999.99,
  "status": "NEW",
  "createdAt": "2026-01-12T10:30:00",
  "user": {...},
  "items": [...]
}

Error: 404 Not Found
```

#### Get Orders By User
```http
GET /api/orders/user/{userId}

Response: 200 OK
[
  {
    "id": 1,
    "totalPrice": 2999.99,
    ...
  }
]
```

#### Get Orders By Status
```http
GET /api/orders/status/{status}

Status values: NEW, CONFIRMED, PAID, CANCELLED

Response: 200 OK
[...]
```

#### Create Order
```http
POST /api/orders
Content-Type: application/json

Body:
{
  "totalPrice": 2999.99,
  "status": "NEW",
  "user": {
    "id": 1
  },
  "items": [
    {
      "quantity": 2,
      "price": 999.99,
      "product": {
        "id": 1
      }
    }
  ]
}

Response: 201 Created
```

#### Update Order
```http
PUT /api/orders/{id}
Content-Type: application/json

Body:
{
  "totalPrice": 2999.99,
  "status": "CONFIRMED",
  "user": {
    "id": 1
  },
  "items": [...]
}

Response: 200 OK
Error: 404 Not Found
```

---

### **5. User APIs**

#### Get All Users (Admin)
```http
GET /api/users

Response: 200 OK
[
  {
    "id": 1,
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com",
    "role": "ROLE_USER",
    "enabled": true
    // Note: password không được trả về (đã dùng @JsonIgnore)
  }
]
```

#### Get User By ID
```http
GET /api/users/{id}

Response: 200 OK
{
  "id": 1,
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "ROLE_USER",
  "enabled": true
}

Error: 404 Not Found
```

#### Get User By Email
```http
GET /api/users/email/{email}

Response: 200 OK
{...}

Error: 404 Not Found
```

#### Get User By Username
```http
GET /api/users/username/{username}

Response: 200 OK
{...}

Error: 404 Not Found
```

#### Create User (Admin)
```http
POST /api/users
Content-Type: application/json

Body:
{
  "username": "new_user",
  "fullName": "New User",
  "email": "newuser@example.com",
  "password": "password123",
  "role": "ROLE_USER",
  "enabled": true
}

Response: 201 Created
```

#### Update User
```http
PUT /api/users/{id}
Content-Type: application/json

Body:
{
  "username": "updated_user",
  "fullName": "Updated Name",
  "email": "updated@example.com",
  "password": "newpassword",  // Optional
  "enabled": true
}

Response: 200 OK
Error: 404 Not Found
```

#### Delete User (Admin)
```http
DELETE /api/users/{id}

Response: 204 No Content
Error: 404 Not Found
```

---

## 📱 Android App Features

### **Core Screens**

#### 1. Authentication
- **Splash Screen** (với logo animation)
- **Login Screen**
  - Username/email field
  - Password field
  - Remember me checkbox
  - Login button
  - Navigate to Register
  - Forgot password (optional)

- **Register Screen**
  - Username field
  - Full name field
  - Email field
  - Password field
  - Confirm password field
  - Register button
  - Navigate back to Login

#### 2. Home & Product Browsing
- **Home Screen** (Bottom Navigation)
  - Categories carousel/grid
  - Featured products
  - Search bar
  - Banner/slider
  - Pull-to-refresh

- **Product List Screen**
  - RecyclerView với GridLayoutManager (2 columns)
  - Filter by category
  - Sort options (price, name, newest)
  - Endless scrolling/pagination
  - Search functionality

- **Product Detail Screen**
  - Product image (với zoom)
  - Product name & price
  - Description
  - Quantity selector
  - Add to cart button
  - Category info
  - Stock status

#### 3. Shopping Cart
- **Cart Screen** (Bottom Navigation)
  - List of cart items
  - Quantity adjustment (+/-)
  - Remove item (swipe to delete)
  - Total price calculation
  - Checkout button
  - Empty cart state

- **Checkout Screen**
  - Order summary
  - Delivery address (optional)
  - Payment method selection
  - Place order button
  - Order confirmation

#### 4. Orders & Profile
- **Orders Screen** (Bottom Navigation)
  - My orders list
  - Filter by status (NEW, CONFIRMED, PAID, CANCELLED)
  - Order date & total
  - Click to view order details

- **Order Detail Screen**
  - Order ID & status
  - Order items list
  - Total price
  - Created date
  - Cancel order button (if status = NEW)

- **Profile Screen** (Bottom Navigation)
  - User info display
  - Edit profile button
  - Settings
  - Logout button

- **Edit Profile Screen**
  - Update full name
  - Update email
  - Change password
  - Save button

#### 5. Admin Features (Optional)
- **Admin Dashboard**
  - Statistics overview
  - Manage products
  - Manage categories
  - Manage orders
  - Manage users

---

## 🏛️ Android App Architecture

### **Package Structure**
```
com.yourname.ecommerceapp/
├── data/
│   ├── model/           # Data classes
│   │   ├── Category.kt
│   │   ├── Product.kt
│   │   ├── Order.kt
│   │   ├── OrderItem.kt
│   │   └── User.kt
│   ├── remote/          # API services
│   │   ├── ApiService.kt
│   │   ├── RetrofitClient.kt
│   │   └── interceptor/
│   │       └── AuthInterceptor.kt
│   ├── local/           # Room database
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   └── repository/      # Repository pattern
│       ├── ProductRepository.kt
│       ├── CategoryRepository.kt
│       ├── OrderRepository.kt
│       └── UserRepository.kt
├── ui/
│   ├── auth/            # Login, Register
│   ├── home/            # Home screen
│   ├── product/         # Product list, detail
│   ├── cart/            # Shopping cart
│   ├── order/           # Orders, order detail
│   ├── profile/         # Profile, settings
│   └── admin/           # Admin features
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── ProductViewModel.kt
│   ├── CategoryViewModel.kt
│   ├── OrderViewModel.kt
│   └── UserViewModel.kt
├── utils/
│   ├── Constants.kt
│   ├── Resource.kt      # Sealed class cho state
│   ├── NetworkUtils.kt
│   └── PreferenceManager.kt
└── MainActivity.kt
```

---

## 🔐 Authentication Flow

### **Session Management**
```kotlin
// 1. Lưu session sau login thành công
class PreferenceManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    fun saveUserSession(user: User, sessionId: String) {
        sharedPreferences.edit().apply {
            putString("user_id", user.id.toString())
            putString("username", user.username)
            putString("session_id", sessionId)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
    
    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean("is_logged_in", false)
    
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}

// 2. Thêm session vào header cho mọi request
class AuthInterceptor(private val prefManager: PreferenceManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val sessionId = prefManager.getSessionId()
        if (sessionId != null) {
            request.addHeader("Cookie", "JSESSIONID=$sessionId")
        }
        return chain.proceed(request.build())
    }
}
```

---

## 🎨 UI/UX Guidelines

### **Design Principles**
- **Material Design 3** guidelines
- **Dark mode support**
- **Responsive layouts** (phones & tablets)
- **Smooth animations** (shared element transitions)
- **Error handling** với user-friendly messages
- **Loading states** (Shimmer effect)
- **Empty states** (no data illustrations)

### **Color Scheme**
```xml
<color name="primary">#FF6200EE</color>
<color name="primary_variant">#FF3700B3</color>
<color name="secondary">#FF03DAC5</color>
<color name="background">#FFFFFFFF</color>
<color name="surface">#FFFFFFFF</color>
<color name="error">#FFB00020</color>
```

---

## 🛠️ Implementation Guidelines

### **1. Retrofit API Service**
```kotlin
interface ApiService {
    // Categories
    @GET("api/categories")
    suspend fun getAllCategories(): Response<List<Category>>
    
    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): Response<Category>
    
    // Products
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>
    
    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<Product>
    
    @GET("api/products/category/{categoryId}")
    suspend fun getProductsByCategory(@Path("categoryId") categoryId: Long): Response<List<Product>>
    
    // Orders
    @GET("api/orders/user/{userId}")
    suspend fun getOrdersByUser(@Path("userId") userId: Long): Response<List<Order>>
    
    @POST("api/orders")
    suspend fun createOrder(@Body order: Order): Response<Order>
    
    // Auth
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("email") email: String?
    ): Response<Unit>
    
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<Unit>
}
```

### **2. Resource State Management**
```kotlin
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}
```

### **3. ViewModel Example**
```kotlin
class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _products = MutableLiveData<Resource<List<Product>>>()
    val products: LiveData<Resource<List<Product>>> = _products
    
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading()
            try {
                val response = repository.getAllProducts()
                if (response.isSuccessful) {
                    _products.value = Resource.Success(response.body()!!)
                } else {
                    _products.value = Resource.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _products.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### **4. RecyclerView Adapter Example**
```kotlin
class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    
    private var products = listOf<Product>()
    
    fun submitList(list: List<Product>) {
        products = list
        notifyDataSetChanged()
    }
    
    inner class ViewHolder(private val binding: ItemProductBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductPrice.text = "$${product.price}"
                Glide.with(itemView)
                    .load(product.image)
                    .placeholder(R.drawable.placeholder)
                    .into(ivProductImage)
                
                root.setOnClickListener { onItemClick(product) }
            }
        }
    }
}
```

---

## 🧪 Testing Requirements

### **Unit Tests**
- ViewModel tests với MockK
- Repository tests
- Utility function tests

### **UI Tests**
- Espresso tests cho critical flows
- Login flow test
- Add to cart test
- Checkout test

---

## 🚀 Additional Features (Nice to have)

1. **Search với autocomplete**
2. **Wishlist/Favorites**
3. **Product reviews & ratings**
4. **Push notifications** (Firebase)
5. **Image upload** cho user avatar
6. **Multiple payment methods**
7. **Order tracking**
8. **Promotional codes/coupons**
9. **Multi-language support**
10. **Analytics** (Firebase Analytics)

---

## 📝 Deliverables

1. **Android Studio Project** (full source code)
2. **APK file** để test
3. **README.md** với:
   - Setup instructions
   - Dependencies list
   - Build instructions
   - Screenshots
4. **Architecture diagram**
5. **API integration documentation**

---

## 🔧 Setup Instructions cho Developer

### **1. Chuẩn bị Backend**
```bash
# Chạy Spring Boot application
cd d:\SpringMVC\son\son
.\gradlew bootRun

# Server sẽ chạy ở http://localhost:8080
```

### **2. Tạo Android Project**
- Mở Android Studio
- Create new project: Empty Activity
- Package name: `com.yourname.ecommerceapp`
- Minimum SDK: API 24
- Language: Kotlin

### **3. Thêm dependencies vào build.gradle**
```gradle
plugins {
    id 'kotlin-kapt'
}

dependencies {
    // (Copy từ Tech Stack section ở trên)
}
```

### **4. Configure Base URL**
```kotlin
// utils/Constants.kt
object Constants {
    // Cho emulator
    const val BASE_URL = "http://10.0.2.2:8080/"
    
    // Cho device thật - thay bằng IP máy tính của bạn
    // const val BASE_URL = "http://192.168.1.100:8080/"
}
```

### **5. Test API Connection**
- Mở Postman hoặc browser
- Test: `http://localhost:8080/api/categories`
- Nếu thấy dữ liệu JSON → Backend OK!

---

## ⚠️ Important Notes

### **CORS Issues**
Backend đã được cấu hình cho web, nếu gặp vấn đề CORS từ Android app, cần thêm:
```java
// Trong SecurityConfig hoặc WebConfig
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("*");
        }
    };
}
```

### **JSON Serialization**
- Backend đã fix circular reference với `@JsonIgnoreProperties`
- Password field đã được ẩn với `@JsonIgnore`
- Không cần xử lý thêm về JSON serialization

### **Authentication**
- Backend sử dụng Spring Security với session-based auth
- Mobile app cần lưu và gửi JSESSIONID cookie trong mọi request sau khi login
- Implement AuthInterceptor để tự động thêm cookie header

### **Image URLs**
- Nếu backend trả về relative URLs, cần append BASE_URL
- VD: `image: "product1.jpg"` → `BASE_URL + "static/images/" + image`

---

## 📞 Support & Resources

### **Documentation**
- Spring Boot API: `http://localhost:8080/swagger-ui.html` (nếu có Swagger)
- API Documentation: Xem file `API_DOCUMENTATION.md` trong project

### **Learning Resources**
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
- Retrofit: https://square.github.io/retrofit/
- MVVM Architecture: https://developer.android.com/topic/architecture
- Material Design 3: https://m3.material.io/

---

## ✅ Checklist

- [ ] Setup Android Studio project
- [ ] Add all dependencies
- [ ] Create data models matching backend entities
- [ ] Setup Retrofit with interceptors
- [ ] Implement all API services
- [ ] Create repositories
- [ ] Implement ViewModels
- [ ] Design UI layouts
- [ ] Implement authentication flow
- [ ] Implement product browsing
- [ ] Implement shopping cart
- [ ] Implement order placement
- [ ] Add error handling & loading states
- [ ] Test on emulator
- [ ] Test on real device
- [ ] Add unit tests
- [ ] Add UI tests
- [ ] Generate APK
- [ ] Write documentation

---

**🎉 Chúc bạn code vui vẻ! Happy Coding! 🚀**

*Generated for Spring Boot eCommerce Backend API - January 2026*

