package com.salesapp.android.data.api;

import com.salesapp.android.data.model.Cart;
import com.salesapp.android.data.model.Category;
//import com.salesapp.android.data.model.Order;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.model.StoreLocation;
import com.salesapp.android.data.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Auth endpoints
    @POST("api/auth/signin")
    Call<Map<String, Object>> login(@Body Map<String, String> loginRequest);

    @POST("api/auth/signup")
    Call<Map<String, Object>> register(@Body Map<String, String> signupRequest);

    // Product endpoints
    @GET("api/products")
    Call<List<Product>> getAllProducts();

    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") long id);

    @GET("api/products/category/{categoryId}")
    Call<List<Product>> getProductsByCategoryId(@Path("categoryId") long categoryId);

    @GET("api/products/search")
    Call<List<Product>> searchProducts(@Query("name") String name);

    // Category endpoints
    @GET("api/categories")
    Call<List<Category>> getAllCategories();

    // Cart endpoints
    @GET("api/carts/user/{userId}/active")
    Call<Cart> getActiveCart(@Path("userId") long userId);

    @POST("api/carts")
    Call<Cart> createCart(@Body Map<String, Object> cartRequest);

    @POST("api/carts/{cartId}/items")
    Call<Map<String, Object>> addItemToCart(@Path("cartId") long cartId, @Body Map<String, Object> cartItemRequest);

    @PUT("api/carts/{cartId}/items/{itemId}")
    Call<Map<String, Object>> updateCartItem(@Path("cartId") long cartId, @Path("itemId") long itemId, @Body Map<String, Object> cartItemRequest);

    @DELETE("api/carts/{cartId}/items/{itemId}")
    Call<Map<String, Object>> removeCartItem(@Path("cartId") long cartId, @Path("itemId") long itemId);

    // Order endpoints
//    @POST("api/orders")
//    Call<Order> createOrder(@Body Map<String, Object> orderRequest);
//
//    @GET("api/orders/user/{userId}")
//    Call<List<Order>> getUserOrders(@Path("userId") long userId);

    // Store location endpoints
    @GET("api/store-locations")
    Call<List<StoreLocation>> getAllStoreLocations();

    // User endpoints
    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") long id);

    @PUT("api/users/{id}")
    Call<User> updateUser(@Path("id") long id, @Body Map<String, Object> userRequest);
}