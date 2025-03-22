package com.salesapp.android.data.api.service;

import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.model.request.ProductRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service interface for Product-related API endpoints
 */
public interface ProductService {
    /**
     * Get all products
     */
    @GET("api/products")
    Call<List<Product>> getAllProducts();

    /**
     * Get product by ID
     */
    @GET("api/products/{id}")
    Call<Product> getProductById(@Path("id") long id);

    /**
     * Get products by category ID
     */
    @GET("api/products/category/{categoryId}")
    Call<List<Product>> getProductsByCategoryId(@Path("categoryId") long categoryId);

    /**
     * Search products by name
     */
    @GET("api/products/search")
    Call<List<Product>> searchProducts(@Query("name") String name);

    /**
     * Create a new product (Admin only)
     */
    @POST("api/products")
    Call<Product> createProduct(@Body ProductRequest productRequest);

    /**
     * Update an existing product (Admin only)
     */
    @PUT("api/products/{id}")
    Call<Product> updateProduct(@Path("id") long id, @Body ProductRequest productRequest);

    /**
     * Delete a product (Admin only)
     */
    @DELETE("api/products/{id}")
    Call<Void> deleteProduct(@Path("id") long id);

    /**
     * Get all categories
     */
    @GET("api/categories")
    Call<List<Category>> getAllCategories();
}