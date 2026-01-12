# 📝 Android App Code Examples

Các đoạn code mẫu ready-to-use cho Android eCommerce app.

---

## 🔧 1. Build.gradle (Module: app)

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
    id 'androidx.navigation.safeargs.kotlin'
}

android {
    namespace 'com.ecommerce.app'
    compileSdk 34

    defaultConfig {
        applicationId "com.ecommerce.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
    }
    
    buildFeatures {
        viewBinding true
    }
}

dependencies {
    // Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

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

    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'

    // Navigation
    implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
    implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'

    // Room
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'

    // Security
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'

    // SwipeRefresh
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

---

## 📦 2. Data Models

### Product.kt
```kotlin
package com.ecommerce.app.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("category")
    val category: Category?
)
```

### Category.kt
```kotlin
package com.ecommerce.app.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("slug")
    val slug: String,
    
    @SerializedName("products")
    val products: List<Product>? = null
)
```

### Order.kt
```kotlin
package com.ecommerce.app.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("totalPrice")
    val totalPrice: Double,
    
    @SerializedName("status")
    val status: String, // NEW, CONFIRMED, PAID, CANCELLED
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("items")
    val items: List<OrderItem>
)
```

### OrderItem.kt
```kotlin
package com.ecommerce.app.data.model

import com.google.gson.annotations.SerializedName

data class OrderItem(
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("product")
    val product: Product? = null
)
```

### User.kt
```kotlin
package com.ecommerce.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("fullName")
    val fullName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("enabled")
    val enabled: Boolean
)
```

---

## 🌐 3. Networking

### Constants.kt
```kotlin
package com.ecommerce.app.utils

object Constants {
    // Cho Android Emulator
    const val BASE_URL = "http://10.0.2.2:8080/"
    
    // Cho device thật - uncomment và thay IP của bạn
    // const val BASE_URL = "http://192.168.1.100:8080/"
    
    const val PREF_NAME = "ecommerce_prefs"
    const val KEY_SESSION_ID = "session_id"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
}
```

### Resource.kt
```kotlin
package com.ecommerce.app.utils

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}
```

### PreferenceManager.kt
```kotlin
package com.ecommerce.app.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    
    fun saveSession(sessionId: String, userId: Long, username: String) {
        sharedPreferences.edit().apply {
            putString(Constants.KEY_SESSION_ID, sessionId)
            putLong(Constants.KEY_USER_ID, userId)
            putString(Constants.KEY_USERNAME, username)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun getSessionId(): String? {
        return sharedPreferences.getString(Constants.KEY_SESSION_ID, null)
    }
    
    fun getUserId(): Long {
        return sharedPreferences.getLong(Constants.KEY_USER_ID, -1)
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString(Constants.KEY_USERNAME, null)
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
    }
    
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
}
```

### AuthInterceptor.kt
```kotlin
package com.ecommerce.app.data.remote.interceptor

import com.ecommerce.app.utils.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val preferenceManager: PreferenceManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        
        // Thêm session ID vào header nếu có
        val sessionId = preferenceManager.getSessionId()
        if (sessionId != null) {
            requestBuilder.addHeader("Cookie", "JSESSIONID=$sessionId")
        }
        
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
```

### RetrofitClient.kt
```kotlin
package com.ecommerce.app.data.remote

import com.ecommerce.app.data.remote.interceptor.AuthInterceptor
import com.ecommerce.app.utils.Constants
import com.ecommerce.app.utils.PreferenceManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private var retrofit: Retrofit? = null
    
    fun getInstance(preferenceManager: PreferenceManager): Retrofit {
        if (retrofit == null) {
            // Logging interceptor
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            // Auth interceptor
            val authInterceptor = AuthInterceptor(preferenceManager)
            
            // OkHttp client
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Retrofit instance
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
```

### ApiService.kt
```kotlin
package com.ecommerce.app.data.remote

import com.ecommerce.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ==================== AUTH ====================
    
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
    
    @POST("logout")
    suspend fun logout(): Response<Unit>
    
    // ==================== CATEGORIES ====================
    
    @GET("api/categories")
    suspend fun getAllCategories(): Response<List<Category>>
    
    @GET("api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): Response<Category>
    
    // ==================== PRODUCTS ====================
    
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>
    
    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<Product>
    
    @GET("api/products/category/{categoryId}")
    suspend fun getProductsByCategory(@Path("categoryId") categoryId: Long): Response<List<Product>>
    
    // ==================== ORDERS ====================
    
    @GET("api/orders")
    suspend fun getAllOrders(): Response<List<Order>>
    
    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<Order>
    
    @GET("api/orders/user/{userId}")
    suspend fun getOrdersByUser(@Path("userId") userId: Long): Response<List<Order>>
    
    @GET("api/orders/status/{status}")
    suspend fun getOrdersByStatus(@Path("status") status: String): Response<List<Order>>
    
    @POST("api/orders")
    suspend fun createOrder(@Body order: Order): Response<Order>
    
    @PUT("api/orders/{id}")
    suspend fun updateOrder(@Path("id") id: Long, @Body order: Order): Response<Order>
    
    // ==================== USERS ====================
    
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>
    
    @GET("api/users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): Response<User>
    
    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body user: User): Response<User>
}
```

---

## 🗄️ 4. Repository

### ProductRepository.kt
```kotlin
package com.ecommerce.app.data.repository

import com.ecommerce.app.data.model.Product
import com.ecommerce.app.data.remote.ApiService
import com.ecommerce.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(private val apiService: ApiService) {
    
    suspend fun getAllProducts(): Resource<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllProducts()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun getProductById(id: Long): Resource<Product> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductById(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun getProductsByCategory(categoryId: Long): Resource<List<Product>> = 
        withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductsByCategory(categoryId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
}
```

### AuthRepository.kt
```kotlin
package com.ecommerce.app.data.repository

import com.ecommerce.app.data.remote.ApiService
import com.ecommerce.app.utils.PreferenceManager
import com.ecommerce.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {
    
    suspend fun register(username: String, password: String, email: String?): Resource<Unit> = 
        withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(username, password, email)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Registration failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun login(username: String, password: String): Resource<Unit> = 
        withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(username, password)
            if (response.isSuccessful) {
                // Lấy session ID từ response headers
                val cookies = response.headers()["Set-Cookie"]
                val sessionId = cookies?.let { extractSessionId(it) }
                
                if (sessionId != null) {
                    // Sau đó lấy user info
                    val userResponse = apiService.getUserByUsername(username)
                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        val user = userResponse.body()!!
                        preferenceManager.saveSession(sessionId, user.id, user.username)
                        Resource.Success(Unit)
                    } else {
                        Resource.Error("Failed to get user info")
                    }
                } else {
                    Resource.Error("No session ID found")
                }
            } else {
                Resource.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun logout(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.logout()
            preferenceManager.clearSession()
            Resource.Success(Unit)
        } catch (e: Exception) {
            preferenceManager.clearSession()
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    private fun extractSessionId(cookieHeader: String): String? {
        // Format: JSESSIONID=xxxxx; Path=/; HttpOnly
        val regex = "JSESSIONID=([^;]+)".toRegex()
        val matchResult = regex.find(cookieHeader)
        return matchResult?.groupValues?.get(1)
    }
}
```

---

## 🎭 5. ViewModel

### ProductViewModel.kt
```kotlin
package com.ecommerce.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecommerce.app.data.model.Product
import com.ecommerce.app.data.repository.ProductRepository
import com.ecommerce.app.utils.Resource
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    
    private val _products = MutableLiveData<Resource<List<Product>>>()
    val products: LiveData<Resource<List<Product>>> = _products
    
    private val _productDetail = MutableLiveData<Resource<Product>>()
    val productDetail: LiveData<Resource<Product>> = _productDetail
    
    fun loadAllProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading()
            val result = repository.getAllProducts()
            _products.value = result
        }
    }
    
    fun loadProductsByCategory(categoryId: Long) {
        viewModelScope.launch {
            _products.value = Resource.Loading()
            val result = repository.getProductsByCategory(categoryId)
            _products.value = result
        }
    }
    
    fun loadProductDetail(id: Long) {
        viewModelScope.launch {
            _productDetail.value = Resource.Loading()
            val result = repository.getProductById(id)
            _productDetail.value = result
        }
    }
    
    fun searchProducts(query: String) {
        val currentProducts = _products.value?.data ?: return
        val filteredProducts = currentProducts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
        _products.value = Resource.Success(filteredProducts)
    }
}
```

### AuthViewModel.kt
```kotlin
package com.ecommerce.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecommerce.app.data.repository.AuthRepository
import com.ecommerce.app.utils.PreferenceManager
import com.ecommerce.app.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    
    private val _loginResult = MutableLiveData<Resource<Unit>>()
    val loginResult: LiveData<Resource<Unit>> = _loginResult
    
    private val _registerResult = MutableLiveData<Resource<Unit>>()
    val registerResult: LiveData<Resource<Unit>> = _registerResult
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = Resource.Loading()
            val result = repository.login(username, password)
            _loginResult.value = result
        }
    }
    
    fun register(username: String, password: String, email: String?) {
        viewModelScope.launch {
            _registerResult.value = Resource.Loading()
            val result = repository.register(username, password, email)
            _registerResult.value = result
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
    
    fun isLoggedIn(): Boolean = preferenceManager.isLoggedIn()
}
```

---

## 🛒 6. Cart Manager

### CartManager.kt
```kotlin
package com.ecommerce.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.ecommerce.app.data.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CartItem(
    val product: Product,
    var quantity: Int
)

class CartManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val CART_KEY = "cart_items"
    
    fun addToCart(product: Product, quantity: Int = 1) {
        val cartItems = getCartItems().toMutableList()
        val existingItem = cartItems.find { it.product.id == product.id }
        
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cartItems.add(CartItem(product, quantity))
        }
        
        saveCart(cartItems)
    }
    
    fun removeFromCart(productId: Long) {
        val cartItems = getCartItems().toMutableList()
        cartItems.removeAll { it.product.id == productId }
        saveCart(cartItems)
    }
    
    fun updateQuantity(productId: Long, quantity: Int) {
        val cartItems = getCartItems().toMutableList()
        val item = cartItems.find { it.product.id == productId }
        item?.quantity = quantity
        saveCart(cartItems)
    }
    
    fun getCartItems(): List<CartItem> {
        val json = prefs.getString(CART_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun getTotalPrice(): Double {
        return getCartItems().sumOf { it.product.price * it.quantity }
    }
    
    fun getTotalItems(): Int {
        return getCartItems().sumOf { it.quantity }
    }
    
    fun clearCart() {
        prefs.edit().remove(CART_KEY).apply()
    }
    
    private fun saveCart(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        prefs.edit().putString(CART_KEY, json).apply()
    }
}
```

---

## 🎨 7. RecyclerView Adapters

### ProductAdapter.kt
```kotlin
package com.ecommerce.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecommerce.app.R
import com.ecommerce.app.data.model.Product
import com.ecommerce.app.databinding.ItemProductBinding

class ProductAdapter(
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    
    private var products = listOf<Product>()
    
    fun submitList(list: List<Product>) {
        products = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }
    
    override fun getItemCount(): Int = products.size
    
    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductPrice.text = "$${String.format("%.2f", product.price)}"
                
                Glide.with(itemView.context)
                    .load(product.image)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(ivProductImage)
                
                root.setOnClickListener {
                    onItemClick(product)
                }
            }
        }
    }
}
```

### CategoryAdapter.kt
```kotlin
package com.ecommerce.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecommerce.app.data.model.Category
import com.ecommerce.app.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    
    private var categories = listOf<Category>()
    
    fun submitList(list: List<Category>) {
        categories = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }
    
    override fun getItemCount(): Int = categories.size
    
    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            binding.root.setOnClickListener {
                onItemClick(category)
            }
        }
    }
}
```

---

**Còn tiếp...**

Đây là phần 1 với các code cơ bản nhất. Phần 2 sẽ bao gồm:
- Layouts XML
- Fragments implementation
- Activities implementation
- Navigation graph
- Complete examples

---

*Generated: January 2026 for Spring Boot eCommerce Backend*

