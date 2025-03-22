package com.salesapp.android.data.repository;

import com.salesapp.android.data.api.ApiClient;
import com.salesapp.android.data.api.service.AuthService;
import com.salesapp.android.data.model.response.AuthResponse;
import com.salesapp.android.data.model.request.LoginRequest;
import com.salesapp.android.data.model.request.LogoutRequest;
import com.salesapp.android.data.model.request.SignupRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private AuthService authService;

    public AuthRepository() {
        authService = ApiClient.getClient().create(AuthService.class);
    }

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void login(String username, String password, AuthCallback<AuthResponse> callback) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<AuthResponse> call = authService.login(loginRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        // Try to get error message from server
                        AuthResponse errorResponse = new AuthResponse();
                        errorResponse.setMessage("Login failed: " +
                                (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
                        callback.onError(errorResponse.getMessage());
                    } catch (Exception e) {
                        callback.onError("Login failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void register(String username, String email, String password,
                         String phoneNumber, String address, AuthCallback<AuthResponse> callback) {
        SignupRequest signupRequest = new SignupRequest(username, email, password, phoneNumber, address);
        Call<AuthResponse> call = authService.register(signupRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        // Try to get error message from server
                        AuthResponse errorResponse = new AuthResponse();
                        errorResponse.setMessage("Registration failed: " +
                                (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
                        callback.onError(errorResponse.getMessage());
                    } catch (Exception e) {
                        callback.onError("Registration failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void logout(String token, AuthCallback<AuthResponse> callback) {
        LogoutRequest logoutRequest = new LogoutRequest(token);

        // Use the authenticated client to ensure token is included in the headers
        AuthService authServiceWithToken = ApiClient.getAuthClient(token).create(AuthService.class);
        Call<AuthResponse> call = authServiceWithToken.logout(logoutRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    AuthResponse successResponse = new AuthResponse();
                    successResponse.setMessage("Logout successful");
                    callback.onSuccess(successResponse);
                } else {
                    try {
                        // Try to get error message from server
                        AuthResponse errorResponse = new AuthResponse();
                        errorResponse.setMessage("Logout failed: " +
                                (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
                        callback.onError(errorResponse.getMessage());
                    } catch (Exception e) {
                        callback.onError("Logout failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}