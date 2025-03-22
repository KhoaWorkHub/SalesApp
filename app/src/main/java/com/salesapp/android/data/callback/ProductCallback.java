package com.salesapp.android.data.callback;

/**
 * Generic callback interface for product-related operations
 * @param <T> Type of result data
 */
public interface ProductCallback<T> {
    /**
     * Called when operation is successful
     * @param result Result data
     */
    void onSuccess(T result);

    /**
     * Called when operation encounters an error
     * @param message Error message
     */
    void onError(String message);
}