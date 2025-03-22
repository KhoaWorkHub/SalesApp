package com.salesapp.android.data.repository;

import com.salesapp.android.data.api.ApiClient;
import com.salesapp.android.data.api.service.CartService;
import com.salesapp.android.data.model.Cart;
import com.salesapp.android.data.model.CartItem;
import com.salesapp.android.data.model.request.CartItemRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private CartService cartService;

    public CartRepository(String token) {
        // Use authenticated client since cart operations require authentication
        cartService = ApiClient.getAuthClient(token).create(CartService.class);
    }

    public interface CartCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getActiveCart(CartCallback<Cart> callback) {
        Call<Cart> call = cartService.getActiveCart();
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    if (response.code() == 404) {
                        // No active cart found, create a new one
                        createCart(callback);
                    } else {
                        callback.onError("Failed to get active cart: " +
                                (response.errorBody() != null ?
                                        response.errorBody().toString() : "Unknown error"));
                    }
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createCart(CartCallback<Cart> callback) {
        Call<Cart> call = cartService.createCart();
        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create cart: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void addItemToCart(Long productId, int quantity, CartCallback<CartItem> callback) {
        CartItemRequest request = new CartItemRequest(productId, quantity);
        Call<CartItem> call = cartService.addItemToCart(request);
        call.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to add item to cart: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateCartItem(Long cartItemId, int quantity, CartCallback<CartItem> callback) {
        CartItemRequest request = new CartItemRequest(null, quantity);
        Call<CartItem> call = cartService.updateCartItem(cartItemId, request);
        call.enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update cart item: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteCartItem(Long cartItemId, CartCallback<String> callback) {
        Call<Map<String, String>> call = cartService.deleteCartItem(cartItemId);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Item removed from cart");
                } else {
                    callback.onError("Failed to remove item from cart: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}