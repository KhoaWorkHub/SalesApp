package com.salesapp.android.data.repository;

import android.util.Log;

import com.salesapp.android.data.api.ApiClient;
import com.salesapp.android.data.api.ApiService;
import com.salesapp.android.data.model.StoreLocation;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for accessing store location data from the API
 */
public class LocationRepository {
    private static final String TAG = "LocationRepository";
    private final ApiService apiService;

    public LocationRepository() {
        // Location endpoints are public, no authentication needed
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    /**
     * Callback interface for location operations
     */
    public interface LocationCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    /**
     * Get all store locations
     */
    public void getAllStoreLocations(final LocationCallback<List<StoreLocation>> callback) {
        Call<List<StoreLocation>> call = apiService.getAllStoreLocations();

        call.enqueue(new Callback<List<StoreLocation>>() {
            @Override
            public void onResponse(Call<List<StoreLocation>> call, Response<List<StoreLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Successfully fetched " + response.body().size() + " store locations");
                } else {
                    String errorMessage = "Failed to get store locations: " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<StoreLocation>> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Get store location by ID
     */
    public void getStoreLocationById(long id, final LocationCallback<StoreLocation> callback) {
        Call<StoreLocation> call = apiService.getStoreLocationById(id);

        call.enqueue(new Callback<StoreLocation>() {
            @Override
            public void onResponse(Call<StoreLocation> call, Response<StoreLocation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Successfully fetched store location ID: " + id);
                } else {
                    String errorMessage = "Failed to get store location: " + response.message();
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<StoreLocation> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }
}