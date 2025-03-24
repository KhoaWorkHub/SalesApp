package com.salesapp.android.data.repository;

import android.util.Log;

import com.salesapp.android.data.api.ApiClient;
import com.salesapp.android.data.api.service.ProductService;
import com.salesapp.android.data.callback.ProductCallback;
import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.model.request.ProductRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {
    private ProductService apiService;
    private final String token;

    public ProductRepository(String token) {
        this.token = token;
        // Use authenticated client if token is provided
        if (token != null && !token.isEmpty()) {
            apiService = ApiClient.getAuthClient(token).create(ProductService.class);
        } else {
            apiService = ApiClient.getClient().create(ProductService.class);
        }
    }

    /**
     * Get all products
     */
    public void getAllProducts(ProductCallback<List<Product>> callback) {
        Call<List<Product>> call = apiService.getAllProducts();

        // Add logging to see request details
        Log.d("ProductRepository", "Calling getAllProducts with URL: " + call.request().url());
        Log.d("ProductRepository", "Using token: " + (token != null ? "Yes" : "No"));

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                Log.d("ProductRepository", "getAllProducts response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ProductRepository", "getAllProducts success, received " + response.body().size() + " products");
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        errorBody = "Could not read error body";
                    }
                    Log.e("ProductRepository", "getAllProducts failed: " + response.message() + " - " + errorBody);
                    callback.onError("Failed to fetch products: " + response.message() + " - " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("ProductRepository", "getAllProducts network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get product by ID
     */
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

    /**
     * Get products by category
     */
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

    /**
     * Search products by name
     */
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

    /**
     * Create a new product (Admin only)
     */
    public void createProduct(ProductRequest productRequest, ProductCallback<Product> callback) {
        // Ensure we use the authenticated client for admin operations
        ProductService adminApiService = ApiClient.getAuthClient(token).create(ProductService.class);

        Call<Product> call = adminApiService.createProduct(productRequest);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "Could not parse error response";
                    }
                    callback.onError("Failed to create product: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Update an existing product (Admin only)
     */
    public void updateProduct(long productId, ProductRequest productRequest, ProductCallback<Product> callback) {
        // Ensure we use the authenticated client for admin operations
        ProductService adminApiService = ApiClient.getAuthClient(token).create(ProductService.class);

        Call<Product> call = adminApiService.updateProduct(productId, productRequest);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "Could not parse error response";
                    }
                    callback.onError("Failed to update product: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Delete a product (Admin only)
     */
    public void deleteProduct(long productId, ProductCallback<String> callback) {
        // Ensure we use the authenticated client for admin operations
        ProductService adminApiService = ApiClient.getAuthClient(token).create(ProductService.class);

        Call<Void> call = adminApiService.deleteProduct(productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Product deleted successfully");
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "Could not parse error response";
                    }
                    callback.onError("Failed to delete product: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get all categories
     */
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

    /**
     * Filter products based on criteria (local filtering - not API)
     */
    public List<Product> filterProducts(List<Product> products, String query, Long categoryId,
                                        double minPrice, double maxPrice) {
        Log.d("ProductRepository", "Filtering products - input size: " +
                (products != null ? products.size() : 0));
        Log.d("ProductRepository", "Filter params - query: '" + query +
                "', categoryId: " + categoryId +
                ", price range: " + minPrice + "-" + maxPrice);

        if (products == null) {
            return new ArrayList<>();
        }

        // Debug the products we're trying to filter
        for (Product product : products) {
            Log.d("ProductRepository", "Product before filtering: " +
                    product.getProductId() + " - " +
                    product.getProductName() + ", price: " +
                    product.getPrice() + ", category: " +
                    (product.getCategory() != null ? product.getCategory().getCategoryId() : "null"));
        }

        List<Product> filtered = products.stream()
                .filter(product -> {
                    // Filter by search query
                    boolean matchesQuery = query == null || query.isEmpty() ||
                            (product.getProductName() != null &&
                                    product.getProductName().toLowerCase().contains(query.toLowerCase())) ||
                            (product.getBriefDescription() != null &&
                                    product.getBriefDescription().toLowerCase().contains(query.toLowerCase()));

                    // Filter by category
                    boolean matchesCategory = categoryId == null ||
                            (product.getCategory() != null &&
                                    product.getCategory().getCategoryId().equals(categoryId));

                    // Filter by price
                    boolean matchesPrice = product.getPrice() >= minPrice &&
                            product.getPrice() <= maxPrice;

                    // Log why a product might be filtered out
                    if (!matchesQuery || !matchesCategory || !matchesPrice) {
                        Log.d("ProductRepository", "Product filtered out: " + product.getProductName() +
                                " - matchesQuery: " + matchesQuery +
                                ", matchesCategory: " + matchesCategory +
                                ", matchesPrice: " + matchesPrice);
                    }

                    return matchesQuery && matchesCategory && matchesPrice;
                })
                .collect(Collectors.toList());

        Log.d("ProductRepository", "Filtering result: " + filtered.size() + " products");
        return filtered;
    }

    /**
     * Sort products by price (local sorting - not API)
     */
    public List<Product> sortProductsByPrice(List<Product> products, boolean ascending) {
        if (products == null) {
            return new ArrayList<>();
        }

        List<Product> sortedList = new ArrayList<>(products);
        if (ascending) {
            sortedList.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        } else {
            sortedList.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        }
        return sortedList;
    }
}