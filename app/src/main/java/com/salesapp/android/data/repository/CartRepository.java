package com.salesapp.android.data.repository;

import android.util.Log;

import com.salesapp.android.data.api.ApiClient;
import com.salesapp.android.data.api.service.CartService;
import com.salesapp.android.data.model.request.UpdateCartItemRequest;
import com.salesapp.android.data.model.response.CartResponse;
import com.salesapp.android.data.model.request.CartItemRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private static final String TAG = "CartRepository";
    private CartService cartService;

    public CartRepository(String token) {
        // Use authenticated client since cart operations require authentication
        cartService = ApiClient.getAuthClient(token).create(CartService.class);
    }

    public interface CartCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    /**
     * Get the current user's cart
     */
    public void getCart(CartCallback<CartResponse> callback) {
        Log.d(TAG, "Getting cart");
        Call<CartResponse> call = cartService.getCart();
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Cart fetch successful");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to get cart: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error");
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Add an item to the cart
     */
    public void addItemToCart(Long productId, int quantity, CartCallback<CartResponse> callback) {
        Log.d(TAG, "Adding item to cart - productId: " + productId + ", quantity: " + quantity);
        CartItemRequest request = new CartItemRequest(productId, quantity);
        Call<CartResponse> call = cartService.addToCart(request);
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Item added to cart successfully");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to add item to cart: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error");
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Update cart item quantity
     */
    public void updateCartItem(Long cartItemId, int quantity, CartCallback<CartResponse> callback) {
        Log.d(TAG, "Updating cart item - cartItemId: " + cartItemId + ", quantity: " + quantity);
        UpdateCartItemRequest request = new UpdateCartItemRequest(cartItemId, quantity);
        Call<CartResponse> call = cartService.updateCartItem(request);
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Cart item updated successfully");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to update cart item: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error");
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Remove item from cart
     */
    public void removeCartItem(Long cartItemId, CartCallback<CartResponse> callback) {
        Log.d(TAG, "Removing cart item - cartItemId: " + cartItemId);
        Call<CartResponse> call = cartService.removeCartItem(cartItemId);
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Cart item removed successfully");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to remove cart item: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error");
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Clear the cart
     */
    public void clearCart(CartCallback<CartResponse> callback) {
        Log.d(TAG, "Clearing cart");
        Call<CartResponse> call = cartService.clearCart();
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Cart cleared successfully");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to clear cart: " +
                            (response.errorBody() != null ?
                                    response.errorBody().toString() : "Unknown error");
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }
}