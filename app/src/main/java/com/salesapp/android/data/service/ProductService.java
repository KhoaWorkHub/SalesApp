package com.salesapp.android.data.service;

import com.salesapp.android.data.callback.ProductCallback;
import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.model.request.ProductRequest;
import com.salesapp.android.data.repository.ProductRepository;

import java.util.List;

/**
 * Service layer for handling product-related business logic
 */
public class ProductService {
    private final ProductRepository productRepository;

    // Constructor with dependency injection
    public ProductService(String token) {
        this.productRepository = new ProductRepository(token);
    }

    /**
     * Get all products
     */
    public void getAllProducts(ProductCallback<List<Product>> callback) {
        productRepository.getAllProducts(callback);
    }

    /**
     * Get product by ID
     */
    public void getProductById(long productId, ProductCallback<Product> callback) {
        productRepository.getProductById(productId, callback);
    }

    /**
     * Get products by category ID
     */
    public void getProductsByCategory(long categoryId, ProductCallback<List<Product>> callback) {
        productRepository.getProductsByCategory(categoryId, callback);
    }

    /**
     * Search products by name
     */
    public void searchProducts(String query, ProductCallback<List<Product>> callback) {
        productRepository.searchProducts(query, callback);
    }

    /**
     * Create a new product (Admin only)
     */
    public void createProduct(ProductRequest productRequest, ProductCallback<Product> callback) {
        productRepository.createProduct(productRequest, callback);
    }

    /**
     * Update an existing product (Admin only)
     */
    public void updateProduct(long productId, ProductRequest productRequest, ProductCallback<Product> callback) {
        productRepository.updateProduct(productId, productRequest, callback);
    }

    /**
     * Delete a product (Admin only)
     */
    public void deleteProduct(long productId, ProductCallback<String> callback) {
        productRepository.deleteProduct(productId, callback);
    }

    /**
     * Get all categories
     */
    public void getAllCategories(ProductCallback<List<Category>> callback) {
        productRepository.getAllCategories(callback);
    }

    /**
     * Filter products based on search query, category, and price range
     */
    public List<Product> filterProducts(List<Product> products, String query, Long categoryId,
                                        double minPrice, double maxPrice) {
        return productRepository.filterProducts(products, query, categoryId, minPrice, maxPrice);
    }

    /**
     * Sort products by price
     */
    public List<Product> sortProductsByPrice(List<Product> products, boolean ascending) {
        return productRepository.sortProductsByPrice(products, ascending);
    }
}