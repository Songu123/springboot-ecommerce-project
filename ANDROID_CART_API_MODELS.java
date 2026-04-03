/**
 * ANDROID CART API - DATA MODELS
 * Copy these classes into your Android Project
 */

// ============================================
// AUTH MODELS
// ============================================

public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;

    public RegisterRequest(String username, String password, String email, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}

public class AuthResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
}

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private Long id;
    private String email;
    private String username;

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
}

// ============================================
// CART MODELS
// ============================================

public class AddToCartRequest {
    private Long productId;
    private Integer quantity;

    public AddToCartRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
}

public class UpdateCartRequest {
    private Long productId;
    private Integer quantity;

    public UpdateCartRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
}

public class CartItemEntity {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Double price;
    private Product product;
    private String createdAt;
    private String updatedAt;

    // Getters
    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public Double getPrice() { return price; }
    public Product getProduct() { return product; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setPrice(Double price) { this.price = price; }

    public static class Product {
        private Long id;
        private String name;
        private Double price;
        private String image;

        public Long getId() { return id; }
        public String getName() { return name; }
        public Double getPrice() { return price; }
        public String getImage() { return image; }
    }
}

public class CartResponse {
    private boolean success;
    private String message;
    private CartItemEntity cartItem;
    private Long userId;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public CartItemEntity getCartItem() { return cartItem; }
    public Long getUserId() { return userId; }
}

public class CartCountResponse {
    private int count;
    private boolean success;
    private Long userId;

    public int getCount() { return count; }
    public boolean isSuccess() { return success; }
    public Long getUserId() { return userId; }
}

public class CartFullResponse {
    private boolean success;
    private Cart cart;
    private Long userId;

    public boolean isSuccess() { return success; }
    public Cart getCart() { return cart; }
    public Long getUserId() { return userId; }

    public static class Cart {
        private Long id;
        private Long userId;
        private java.util.List<CartItemEntity> items;
        private Integer totalItems;
        private Double totalPrice;
        private String createdAt;
        private String updatedAt;

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public java.util.List<CartItemEntity> getItems() { return items; }
        public Integer getTotalItems() { return totalItems; }
        public Double getTotalPrice() { return totalPrice; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
}

// ============================================
// API SERVICE INTERFACE
// ============================================

import retrofit2.Call;
import retrofit2.http.*;

public interface CartApiService {

    // AUTH ENDPOINTS
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // CART ENDPOINTS
    @POST("cart/add")
    Call<CartResponse> addToCart(@Body AddToCartRequest request);

    @GET("cart/count")
    Call<CartCountResponse> getCartCount();

    @GET("cart/my-cart")
    Call<CartFullResponse> getFullCart();

    @PUT("cart/update")
    Call<CartResponse> updateCartItem(@Body UpdateCartRequest request);

    @DELETE("cart/remove/{productId}")
    Call<CartResponse> removeFromCart(@Path("productId") Long productId);

    @DELETE("cart/clear")
    Call<CartResponse> clearCart();
}

// ============================================
// RETROFIT CLIENT
// ============================================

import com.squareup.okhttp3.OkHttpClient;
import com.squareup.okhttp3.Request;
import com.squareup.okhttp3.Interceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    // For physical device: "http://192.168.x.x:8080/api/"

    private static Retrofit retrofit;
    private static CartApiService apiService;

    public static CartApiService getApiService(String token) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Add Authorization interceptor
            httpClient.addInterceptor(chain -> {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder();

                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                requestBuilder.header("Content-Type", "application/json");

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        apiService = retrofit.create(CartApiService.class);
        return apiService;
    }
}

// ============================================
// SHARED PREFERENCES HELPER
// ============================================

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "ecommerce_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences preferences;

    public TokenManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token, Long userId, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, "");
    }

    public Long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return !getToken().isEmpty();
    }

    public void clearToken() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_EMAIL);
        editor.apply();
    }
}

// ============================================
// EXAMPLE ACTIVITY USAGE
// ============================================

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private CartApiService apiService;
    private TokenManager tokenManager;
    private static final String TAG = "CartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize token manager and API service
        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        apiService = RetrofitClient.getApiService(token);

        // Example: Add product to cart
        addProductToCart(1L, 2);
    }

    private void addProductToCart(Long productId, Integer quantity) {
        AddToCartRequest request = new AddToCartRequest(productId, quantity);

        apiService.addToCart(request).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful()) {
                    CartResponse cartResponse = response.body();
                    if (cartResponse != null && cartResponse.isSuccess()) {
                        showToast("Product added to cart!");
                        Log.d(TAG, "CartItem ID: " + cartResponse.getCartItem().getId());
                    }
                } else {
                    handleError("Failed to add product", response.code());
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                showToast("Error: " + t.getMessage());
                Log.e(TAG, "Error adding to cart", t);
            }
        });
    }

    private void loadCartCount() {
        apiService.getCartCount().enqueue(new Callback<CartCountResponse>() {
            @Override
            public void onResponse(Call<CartCountResponse> call, Response<CartCountResponse> response) {
                if (response.isSuccessful()) {
                    CartCountResponse countResponse = response.body();
                    if (countResponse != null) {
                        int count = countResponse.getCount();
                        // Update UI with cart count
                        Log.d(TAG, "Cart count: " + count);
                    }
                }
            }

            @Override
            public void onFailure(Call<CartCountResponse> call, Throwable t) {
                Log.e(TAG, "Error loading cart count", t);
            }
        });
    }

    private void loadFullCart() {
        apiService.getFullCart().enqueue(new Callback<CartFullResponse>() {
            @Override
            public void onResponse(Call<CartFullResponse> call, Response<CartFullResponse> response) {
                if (response.isSuccessful()) {
                    CartFullResponse fullResponse = response.body();
                    if (fullResponse != null && fullResponse.isSuccess()) {
                        CartFullResponse.Cart cart = fullResponse.getCart();

                        // Update UI with cart items
                        for (CartItemEntity item : cart.getItems()) {
                            Log.d(TAG, "Item: " + item.getProductId() +
                                  " Qty: " + item.getQuantity());
                        }

                        Log.d(TAG, "Total Price: " + cart.getTotalPrice());
                    }
                }
            }

            @Override
            public void onFailure(Call<CartFullResponse> call, Throwable t) {
                Log.e(TAG, "Error loading cart", t);
            }
        });
    }

    private void removeProduct(Long productId) {
        apiService.removeFromCart(productId).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful()) {
                    CartResponse result = response.body();
                    if (result != null && result.isSuccess()) {
                        showToast("Item removed from cart");
                        loadFullCart(); // Reload cart
                    }
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                showToast("Error removing item: " + t.getMessage());
            }
        });
    }

    private void handleError(String message, int code) {
        String errorMsg = message;
        if (code == 401) {
            errorMsg = "Unauthorized - Please login again";
            tokenManager.clearToken();
        } else if (code == 400) {
            errorMsg = "Bad request - Invalid data";
        } else if (code == 404) {
            errorMsg = "Not found";
        }
        showToast(errorMsg);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

