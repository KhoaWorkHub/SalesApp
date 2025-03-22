package com.salesapp.android.ui.product;

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

    // LiveData for UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Filter state
    private String searchQuery = "";
    private Long categoryId = null;
    private double minPrice = 0;
    private double maxPrice = 10000;

    // Sort state
    private boolean sortAscending = true;

    /**
     * Constructor with dependency injection
     */
    public ProductViewModel(String token) {
        this.productService = new ProductService(token);
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

    /**
     * Load all products from API
     */
    public void loadProducts() {
        isLoading.setValue(true);

        productService.getAllProducts(new ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                products.setValue(result);
                applyFilters(); // Apply any active filters
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
                applyFilters(); // Apply any active filters
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
                applyFilters(); // Apply any active filters
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
                    applyFilters(); // Apply any active filters
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
                    applyFilters(); // Apply any active filters
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
                    applyFilters(); // Apply any active filters
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