package com.salesapp.android.ui.product;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.salesapp.android.data.callback.ProductCallback;
import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.model.request.ProductRequest;
import com.salesapp.android.data.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ViewModel for managing product-related UI state
 */
public class ProductViewModel extends ViewModel {
    private final ProductService productService;

    // LiveData for Products
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> filteredProducts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Product> selectedProduct = new MutableLiveData<>();

    // LiveData for Categories
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Category> selectedCategory = new MutableLiveData<>();

    // LiveData for dynamic price range
    private final MutableLiveData<Double> minimumProductPrice = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> maximumProductPrice = new MutableLiveData<>(100000.0);

    // LiveData for UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Filter state
    private String searchQuery = "";
    private Long categoryId = null;
    private double minPrice = 0;
    private double maxPrice = 100000.0; // Default high value to ensure products aren't filtered out

    // Sort state
    private boolean sortAscending = true;

    /**
     * Constructor with dependency injection
     */
    public ProductViewModel(String token) {
        this.productService = new ProductService(token);

        // Log the initial filter state
        Log.d("ProductViewModel", "Initializing with filters - query: '" + searchQuery +
                "', categoryId: " + categoryId +
                ", price range: " + minPrice + "-" + maxPrice);
    }

    // Getters for LiveData
    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<List<Product>> getFilteredProducts() {
        return filteredProducts;
    }

    public LiveData<Product> getSelectedProduct() {
        return selectedProduct;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Category> getSelectedCategory() {
        return selectedCategory;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Getters for price range
    public LiveData<Double> getMinimumProductPrice() {
        return minimumProductPrice;
    }

    public LiveData<Double> getMaximumProductPrice() {
        return maximumProductPrice;
    }

    // Get current min/max filter prices
    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    /**
     * Load all products from API
     */
    public void loadProducts() {
        isLoading.setValue(true);

        productService.getAllProducts(new ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                Log.d("ProductViewModel", "loadProducts success: received " + result.size() + " products");

                // Update products
                products.setValue(result);

                // Calculate price range from received products
                updatePriceRangeFromProducts(result);

                // Apply filters with the updated price range
                applyFilters();

                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                Log.e("ProductViewModel", "loadProducts error: " + message);
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Calculate and update price range based on available products
     */
    private void updatePriceRangeFromProducts(List<Product> productList) {
        if (productList == null || productList.isEmpty()) {
            // Default values if no products
            minimumProductPrice.setValue(0.0);
            maximumProductPrice.setValue(100000.0);
            return;
        }

        // Find minimum and maximum prices
        double minProductPrice = Double.MAX_VALUE;
        double maxProductPrice = 0.0;

        for (Product product : productList) {
            double price = product.getPrice();
            if (price < minProductPrice) {
                minProductPrice = price;
            }
            if (price > maxProductPrice) {
                maxProductPrice = price;
            }
        }

        // Add buffer and round nicely
        minProductPrice = Math.max(0, Math.floor(minProductPrice * 0.9)); // 10% lower and rounded down
        maxProductPrice = Math.ceil(maxProductPrice * 1.1); // 10% higher and rounded up

        Log.d("ProductViewModel", "Calculated price range: " + minProductPrice + " - " + maxProductPrice);

        // Update LiveData
        minimumProductPrice.setValue(minProductPrice);
        maximumProductPrice.setValue(maxProductPrice);

        // Also update current filter max price if needed
        if (this.maxPrice < maxProductPrice) {
            this.maxPrice = maxProductPrice;
        }
    }

    /**
     * Load product details by ID
     */
    public void loadProductById(long productId) {
        isLoading.setValue(true);

        productService.getProductById(productId, new ProductCallback<Product>() {
            @Override
            public void onSuccess(Product result) {
                selectedProduct.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Load products by category ID
     */
    public void loadProductsByCategory(long categoryId) {
        isLoading.setValue(true);

        productService.getProductsByCategory(categoryId, new ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                products.setValue(result);
                updatePriceRangeFromProducts(result);
                applyFilters();
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Search products by name
     */
    public void searchProducts(String query) {
        isLoading.setValue(true);

        productService.searchProducts(query, new ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                products.setValue(result);
                updatePriceRangeFromProducts(result);
                applyFilters();
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Create a new product (Admin only)
     */
    public void createProduct(ProductRequest productRequest) {
        isLoading.setValue(true);

        productService.createProduct(productRequest, new ProductCallback<Product>() {
            @Override
            public void onSuccess(Product result) {
                // Add new product to the list
                List<Product> currentProducts = products.getValue();
                if (currentProducts != null) {
                    currentProducts.add(result);
                    products.setValue(currentProducts);
                    updatePriceRangeFromProducts(currentProducts);
                    applyFilters();
                }
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Update an existing product (Admin only)
     */
    public void updateProduct(long productId, ProductRequest productRequest) {
        isLoading.setValue(true);

        productService.updateProduct(productId, productRequest, new ProductCallback<Product>() {
            @Override
            public void onSuccess(Product result) {
                // Update product in the list
                List<Product> currentProducts = products.getValue();
                if (currentProducts != null) {
                    for (int i = 0; i < currentProducts.size(); i++) {
                        if (currentProducts.get(i).getProductId().equals(result.getProductId())) {
                            currentProducts.set(i, result);
                            break;
                        }
                    }
                    products.setValue(currentProducts);
                    updatePriceRangeFromProducts(currentProducts);
                    applyFilters();
                }
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Delete a product (Admin only)
     */
    public void deleteProduct(long productId) {
        isLoading.setValue(true);

        productService.deleteProduct(productId, new ProductCallback<String>() {
            @Override
            public void onSuccess(String result) {
                // Remove product from the list
                List<Product> currentProducts = products.getValue();
                if (currentProducts != null) {
                    currentProducts.removeIf(product -> product.getProductId() == productId);
                    products.setValue(currentProducts);
                    updatePriceRangeFromProducts(currentProducts);
                    applyFilters();
                }
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Load all categories
     */
    public void loadCategories() {
        isLoading.setValue(true);

        productService.getAllCategories(new ProductCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Apply filters to the product list
     */
    public void applyFilters() {
        List<Product> currentProducts = products.getValue();
        if (currentProducts == null) {
            return;
        }

        // Log filter parameters
        Log.d("ProductViewModel", "Applying filters - query: '" + searchQuery +
                "', categoryId: " + categoryId +
                ", price range: " + minPrice + "-" + maxPrice);

        // Apply filters
        List<Product> filtered = productService.filterProducts(
                currentProducts, searchQuery, categoryId, minPrice, maxPrice);

        // Apply sorting
        filtered = productService.sortProductsByPrice(filtered, sortAscending);

        // Update filtered products
        filteredProducts.setValue(filtered);
    }

    /**
     * Set filter criteria
     */
    public void setFilters(String searchQuery, Long categoryId, double minPrice, double maxPrice) {
        this.searchQuery = searchQuery;
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        applyFilters();
    }

    /**
     * Set sort order
     */
    public void setSortOrder(boolean ascending) {
        this.sortAscending = ascending;
        applyFilters();
    }

    /**
     * Select a category
     */
    public void selectCategory(Long categoryId) {
        this.categoryId = categoryId;

        // Find the selected category
        if (categoryId != null) {
            List<Category> currentCategories = categories.getValue();
            if (currentCategories != null) {
                for (Category category : currentCategories) {
                    if (category.getCategoryId().equals(categoryId)) {
                        selectedCategory.setValue(category);
                        break;
                    }
                }
            }
        } else {
            selectedCategory.setValue(null);
        }

        applyFilters();
    }

    /**
     * Select a product
     */
    public void selectProduct(Product product) {
        selectedProduct.setValue(product);
    }

    /**
     * Clear selected product
     */
    public void clearSelectedProduct() {
        selectedProduct.setValue(null);
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}