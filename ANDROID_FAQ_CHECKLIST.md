# ✅ Android App Development Checklist & FAQ

---

## 📋 Development Checklist

### Phase 1: Project Setup (Day 1)
- [ ] Tạo Android Studio project mới (Empty Activity)
- [ ] Cấu hình `build.gradle` với tất cả dependencies
- [ ] Enable ViewBinding
- [ ] Tạo package structure (data, ui, viewmodel, utils)
- [ ] Tạo file Constants.kt với BASE_URL
- [ ] Test build project thành công

### Phase 2: Data Layer (Day 1-2)
- [ ] Tạo tất cả data models (Category, Product, Order, OrderItem, User)
- [ ] Tạo Resource sealed class
- [ ] Tạo PreferenceManager
- [ ] Tạo CartManager
- [ ] Tạo AuthInterceptor
- [ ] Tạo RetrofitClient
- [ ] Tạo ApiService interface
- [ ] Test API call đơn giản (GET categories)

### Phase 3: Repository Layer (Day 2)
- [ ] Tạo CategoryRepository
- [ ] Tạo ProductRepository
- [ ] Tạo OrderRepository
- [ ] Tạo AuthRepository
- [ ] Tạo UserRepository
- [ ] Test repositories với dummy ViewModels

### Phase 4: ViewModel Layer (Day 3)
- [ ] Tạo AuthViewModel
- [ ] Tạo CategoryViewModel
- [ ] Tạo ProductViewModel
- [ ] Tạo OrderViewModel
- [ ] Tạo UserViewModel
- [ ] Test ViewModels observing từ dummy Activity

### Phase 5: Authentication UI (Day 3-4)
- [ ] Design layout_login.xml
- [ ] Design layout_register.xml
- [ ] Tạo LoginActivity
- [ ] Tạo RegisterActivity
- [ ] Implement login logic
- [ ] Implement register logic
- [ ] Add validation
- [ ] Test login/register flow

### Phase 6: Main Structure (Day 4-5)
- [ ] Design activity_main.xml với BottomNavigation
- [ ] Tạo MainActivity
- [ ] Setup Navigation Component
- [ ] Tạo navigation graph
- [ ] Test navigation giữa các fragments

### Phase 7: Home Screen (Day 5-6)
- [ ] Design fragment_home.xml
- [ ] Design item_category.xml
- [ ] Design item_product.xml
- [ ] Tạo HomeFragment
- [ ] Tạo CategoryAdapter
- [ ] Tạo ProductAdapter
- [ ] Implement load categories
- [ ] Implement load products
- [ ] Add SwipeRefreshLayout
- [ ] Test home screen

### Phase 8: Product Screens (Day 6-7)
- [ ] Design fragment_product_list.xml
- [ ] Design fragment_product_detail.xml
- [ ] Tạo ProductListFragment
- [ ] Tạo ProductDetailFragment
- [ ] Implement filter by category
- [ ] Implement search functionality
- [ ] Implement product detail view
- [ ] Add to cart functionality
- [ ] Test product browsing

### Phase 9: Shopping Cart (Day 7-8)
- [ ] Design fragment_cart.xml
- [ ] Design item_cart.xml
- [ ] Tạo CartFragment
- [ ] Tạo CartAdapter
- [ ] Implement display cart items
- [ ] Implement quantity adjustment
- [ ] Implement remove item
- [ ] Implement total calculation
- [ ] Test cart operations

### Phase 10: Checkout & Orders (Day 8-9)
- [ ] Design fragment_checkout.xml
- [ ] Design fragment_orders.xml
- [ ] Design fragment_order_detail.xml
- [ ] Tạo CheckoutFragment
- [ ] Tạo OrdersFragment
- [ ] Tạo OrderDetailFragment
- [ ] Implement place order
- [ ] Implement load user orders
- [ ] Implement filter orders by status
- [ ] Test checkout flow

### Phase 11: Profile (Day 9-10)
- [ ] Design fragment_profile.xml
- [ ] Design fragment_edit_profile.xml
- [ ] Tạo ProfileFragment
- [ ] Tạo EditProfileFragment
- [ ] Implement display user info
- [ ] Implement edit profile
- [ ] Implement logout
- [ ] Test profile features

### Phase 12: Polish & Testing (Day 10-12)
- [ ] Add loading indicators
- [ ] Add error messages
- [ ] Add empty states
- [ ] Add animations
- [ ] Handle edge cases
- [ ] Test all flows end-to-end
- [ ] Fix bugs
- [ ] Optimize performance

### Phase 13: Final (Day 12-14)
- [ ] Add app icon
- [ ] Add splash screen
- [ ] Test trên emulator
- [ ] Test trên device thật
- [ ] Write documentation
- [ ] Generate APK
- [ ] Create README.md
- [ ] Add screenshots

---

## ❓ Frequently Asked Questions

### 🌐 Networking & API

**Q: Làm sao để kết nối Android Emulator với localhost?**

A: Sử dụng IP đặc biệt `10.0.2.2` thay vì `localhost`:
```kotlin
const val BASE_URL = "http://10.0.2.2:8080/"
```

**Q: Làm sao để kết nối device thật với backend?**

A: 
1. Đảm bảo device và máy tính cùng mạng WiFi
2. Tìm IP của máy tính: `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux)
3. Dùng IP đó trong BASE_URL:
```kotlin
const val BASE_URL = "http://192.168.1.100:8080/"
```

**Q: Gặp lỗi "Network Security Configuration" trên Android 9+?**

A: Thêm file `network_security_config.xml` trong `res/xml/`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

Và thêm vào `AndroidManifest.xml`:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

**Q: Làm sao để debug API calls?**

A: Sử dụng `HttpLoggingInterceptor` (đã có trong RetrofitClient) và xem Logcat với filter "OkHttp"

**Q: Session timeout thì xử lý thế nào?**

A: Thêm response interceptor để check status code 401/403, sau đó logout và navigate về Login:
```kotlin
class SessionInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401 || response.code == 403) {
            // Session expired
            // Clear session và navigate to login
        }
        return response
    }
}
```

---

### 🔐 Authentication

**Q: Backend trả về 302 redirect thay vì JSON response, làm sao xử lý?**

A: Retrofit sẽ tự động follow redirects. Nếu muốn lấy session từ headers:
```kotlin
val response = apiService.login(username, password)
val cookies = response.headers()["Set-Cookie"]
```

**Q: Lưu password để "Remember Me" có an toàn không?**

A: 
- KHÔNG lưu plain password
- Nếu cần "Remember Me", chỉ lưu session ID hoặc token
- Hoặc dùng Biometric authentication (fingerprint)

**Q: Làm sao để auto-login khi mở app?**

A: Check session trong SplashActivity:
```kotlin
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefManager = PreferenceManager(this)
        if (prefManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
```

---

### 🖼️ Images

**Q: Backend trả về relative path cho images, load thế nào?**

A: Append BASE_URL:
```kotlin
val imageUrl = Constants.BASE_URL + "static/images/" + product.image

Glide.with(context)
    .load(imageUrl)
    .into(imageView)
```

**Q: Ảnh load chậm, làm sao tối ưu?**

A: 
1. Dùng placeholder và cache của Glide:
```kotlin
Glide.with(context)
    .load(imageUrl)
    .placeholder(R.drawable.placeholder)
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .into(imageView)
```

2. Backend nên resize ảnh trước khi trả về

**Q: Làm sao upload ảnh từ Android?**

A: Dùng MultipartBody với Retrofit:
```kotlin
@Multipart
@POST("api/upload")
suspend fun uploadImage(
    @Part image: MultipartBody.Part
): Response<String>
```

---

### 🛒 Shopping Cart

**Q: Lưu cart ở đâu? SharedPreferences hay Room?**

A:
- **SharedPreferences**: Đơn giản, phù hợp cho cart nhỏ (<50 items)
- **Room Database**: Tốt hơn cho cart lớn và cần query phức tạp
- Code ví dụ đã dùng SharedPreferences với Gson

**Q: Cart có cần đồng bộ với server không?**

A: 
- **Không đăng nhập**: Lưu local only
- **Đã đăng nhập**: Nên có API để sync cart với server (chưa có trong backend hiện tại)

**Q: Làm sao để show badge số lượng items trên cart icon?**

A: Dùng `BadgeDrawable`:
```kotlin
val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart)
badge.number = cartManager.getTotalItems()
badge.isVisible = true
```

---

### 📱 UI/UX

**Q: Làm sao để add skeleton loading (shimmer effect)?**

A: Thêm library:
```gradle
implementation 'com.facebook.shimmer:shimmer:0.5.0'
```

**Q: Infinite scroll cho product list?**

A: Implement `RecyclerView.OnScrollListener`:
```kotlin
recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!recyclerView.canScrollVertically(1)) {
            // Load more
        }
    }
})
```

**Q: Search với debounce thế nào?**

A: Dùng Kotlin Flow:
```kotlin
searchView.getQueryTextChangeStateFlow()
    .debounce(300)
    .distinctUntilChanged()
    .onEach { query ->
        viewModel.searchProducts(query)
    }
    .launchIn(lifecycleScope)
```

---

### 🐛 Common Errors

**Q: Lỗi "Unable to resolve host" khi call API?**

A: 
1. Check internet permission trong `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
2. Check backend có đang chạy không
3. Check BASE_URL đúng chưa

**Q: Lỗi "Circular reference" khi serialize JSON?**

A: Backend đã fix với `@JsonIgnoreProperties`, nhưng nếu vẫn lỗi, check Gson configuration

**Q: App crash khi load image từ URL null?**

A: Luôn check null trước khi load:
```kotlin
if (!product.image.isNullOrEmpty()) {
    Glide.with(context).load(imageUrl).into(imageView)
} else {
    imageView.setImageResource(R.drawable.placeholder)
}
```

**Q: NetworkOnMainThreadException?**

A: Luôn dùng coroutines hoặc async tasks cho network calls. Code ví dụ đã dùng `suspend` functions với `withContext(Dispatchers.IO)`

**Q: ViewModelProvider requires a ViewModelProvider.Factory?**

A: Tạo Factory cho ViewModel có parameters:
```kotlin
class ProductViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

---

### 🚀 Performance

**Q: App chạy chậm khi scroll RecyclerView?**

A:
1. Dùng ViewHolder pattern (đã có trong code)
2. Optimize image loading với Glide
3. Tránh complex layouts trong item
4. Dùng `setHasFixedSize(true)` cho RecyclerView

**Q: Làm sao để cache API responses?**

A:
1. Thêm OkHttp cache:
```kotlin
val cacheSize = 10 * 1024 * 1024 // 10 MB
val cache = Cache(context.cacheDir, cacheSize.toLong())

OkHttpClient.Builder()
    .cache(cache)
    .build()
```

2. Hoặc dùng Room database để cache

**Q: Memory leak khi dùng Glide?**

A: Luôn dùng `applicationContext` hoặc clear trong `onDestroy()`:
```kotlin
override fun onDestroy() {
    Glide.with(this).clear(imageView)
    super.onDestroy()
}
```

---

### 📦 Build & Deploy

**Q: Làm sao để generate APK?**

A: 
1. Build > Build Bundle(s) / APK(s) > Build APK(s)
2. Hoặc terminal: `./gradlew assembleRelease`
3. APK file ở `app/build/outputs/apk/release/`

**Q: APK size quá lớn, làm sao giảm?**

A:
1. Enable minification trong `build.gradle`:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
    }
}
```

2. Dùng WebP thay vì PNG/JPG
3. Remove unused resources

**Q: Làm sao để test trên nhiều devices?**

A:
1. Dùng Firebase Test Lab
2. Hoặc tạo nhiều AVD với API levels khác nhau
3. Test trên ít nhất: Small phone, Large phone, Tablet

---

### 🔧 Backend Configuration

**Q: Cần thêm gì ở backend để mobile app hoạt động tốt hơn?**

A: Recommend thêm:
1. **Pagination API**: `/api/products?page=0&size=20`
2. **Search API**: `/api/products/search?q=laptop`
3. **Cart API**: Save cart to server
4. **Image optimization**: Resize images trước khi trả về
5. **Token authentication**: JWT thay vì session (better for mobile)

**Q: CORS issues với mobile app?**

A: Mobile app thường không gặp CORS (CORS chỉ cho browser), nhưng nếu dùng WebView thì cần config CORS ở backend

---

### 📚 Best Practices

**Q: Structure project như thế nào cho maintainable?**

A: Follow Clean Architecture:
```
data/ (entities, repositories, data sources)
domain/ (use cases, business logic)
presentation/ (ViewModels, UI)
di/ (dependency injection)
```

**Q: Nên dùng Fragments hay Activities?**

A: 
- **Single Activity với Multiple Fragments** (recommended)
- Dùng Navigation Component để navigate
- Activities chỉ cho major flows (Login, Main)

**Q: Testing strategy?**

A:
1. Unit tests cho ViewModels và Repositories
2. UI tests cho critical flows (login, checkout)
3. Integration tests cho API calls (với MockWebServer)

---

## 🎓 Learning Resources

### Documentation
- Android Developers: https://developer.android.com
- Kotlin Docs: https://kotlinlang.org/docs
- Retrofit: https://square.github.io/retrofit
- Glide: https://github.com/bumptech/glide

### Tutorials
- Codelabs: https://developer.android.com/codelabs
- Udacity Android Course (Free)
- YouTube: Philipp Lackner, CodingWithMitch

### Code Samples
- Android Samples: https://github.com/android
- Google Samples: https://github.com/googlesamples

---

## 📞 Troubleshooting Guide

### Step-by-step Debug Process

1. **API Not Connecting**
   - [ ] Check backend is running (`http://localhost:8080/api/categories` in browser)
   - [ ] Check BASE_URL correct (10.0.2.2 for emulator)
   - [ ] Check internet permission in manifest
   - [ ] Check network_security_config for HTTP
   - [ ] View Logcat with "OkHttp" filter

2. **UI Not Updating**
   - [ ] Check ViewModel observe in Fragment/Activity
   - [ ] Check LiveData being posted on correct thread
   - [ ] Check adapter.notifyDataSetChanged() called
   - [ ] Use Log.d to trace data flow

3. **Crash on Launch**
   - [ ] Check Stack Trace in Logcat
   - [ ] Look for NullPointerException
   - [ ] Check all required permissions
   - [ ] Verify dependency versions compatible

4. **Images Not Loading**
   - [ ] Check image URL in Logcat
   - [ ] Test URL in browser
   - [ ] Check Glide error callback
   - [ ] Verify BASE_URL + image path correct

5. **Login Not Working**
   - [ ] Check request body in Logcat
   - [ ] Check response code (200 = success)
   - [ ] Verify session ID extracted correctly
   - [ ] Check PreferenceManager saving data

---

## ✨ Extra Features Ideas

Nếu có thời gian, thêm các features này:

1. **Push Notifications** (Firebase Cloud Messaging)
   - Thông báo order confirmed
   - Thông báo sản phẩm mới
   - Promotional notifications

2. **Offline Mode** (Room Database)
   - Cache products offline
   - Queue orders khi offline
   - Sync khi online

3. **Dark Mode**
   - Support Material You
   - Theme toggle in settings

4. **Wishlist/Favorites**
   - Save favorite products
   - Quick add from product list

5. **Product Reviews**
   - Rate & review products
   - View reviews from others

6. **Advanced Search**
   - Filter by price range
   - Filter by category
   - Sort options

7. **Payment Integration**
   - Stripe/PayPal SDK
   - Mock payment for demo

8. **Order Tracking**
   - Real-time order status
   - Delivery tracking map

9. **Social Sharing**
   - Share products
   - Share orders

10. **Analytics**
    - Firebase Analytics
    - Track user behavior
    - A/B testing

---

## 🎯 Performance Checklist

- [ ] Images compressed và cached
- [ ] API responses cached khi phù hợp
- [ ] RecyclerView với proper ViewHolder
- [ ] Lazy loading cho lists
- [ ] Avoid memory leaks (check with LeakCanary)
- [ ] Minimize main thread work
- [ ] Optimize layouts (avoid nested layouts)
- [ ] Use ProGuard/R8 for release
- [ ] Test on low-end devices

---

**🎉 Good luck with your Android development! 🚀**

*Questions? Check the documentation or ask the community!*

---

*Generated: January 2026*

