package com.salesapp.android.data.repository;

import com.salesapp.android.data.api.ApiClient;
import com.salesapp.android.data.api.ApiService;
import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private ApiService apiService;

    public ProductRepository(String token) {
        // Use authenticated client if token is provided
        if (token != null && !token.isEmpty()) {
            apiService = ApiClient.getAuthClient(token).create(ApiService.class);
        } else {
            apiService = ApiClient.getClient().create(ApiService.class);
        }
    }

    public interface ProductCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getAllProducts(ProductCallback<List<Product>> callback) {
        Call<List<Product>> call = apiService.getAllProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch products: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getProductById(long productId, ProductCallback<Product> callback) {
        Call<Product> call = apiService.getProductById(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch product: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getProductsByCategory(long categoryId, ProductCallback<List<Product>> callback) {
        Call<List<Product>> call = apiService.getProductsByCategoryId(categoryId);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch products by category: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void searchProducts(String query, ProductCallback<List<Product>> callback) {
        Call<List<Product>> call = apiService.searchProducts(query);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to search products: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getAllCategories(ProductCallback<List<Category>> callback) {
        Call<List<Category>> call = apiService.getAllCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch categories: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}