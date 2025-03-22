package com.salesapp.android.data.api.service;

import com.salesapp.android.data.model.Cart;
import com.salesapp.android.data.model.CartItem;
import com.salesapp.android.data.model.request.CartItemRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartService {
    @GET("api/carts/active")
    Call<Cart> getActiveCart();

    @POST("api/carts")
    Call<Cart> createCart();

    @POST("api/cart-items")
    Call<CartItem> addItemToCart(@Body CartItemRequest cartItemRequest);

    @PUT("api/cart-items/{cartItemId}")
    Call<CartItem> updateCartItem(@Path("cartItemId") Long cartItemId, @Body CartItemRequest cartItemRequest);

    @DELETE("api/cart-items/{cartItemId}")
    Call<Map<String, String>> deleteCartItem(@Path("cartItemId") Long cartItemId);
}