package com.salesapp.android.data.api.service;

import com.salesapp.android.data.model.request.LoginRequest;
import com.salesapp.android.data.model.request.LogoutRequest;
import com.salesapp.android.data.model.request.SignupRequest;
import com.salesapp.android.data.model.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/auth/signin")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("api/auth/signup")
    Call<AuthResponse> register(@Body SignupRequest signupRequest);

    @POST("api/auth/logout")
    Call<AuthResponse> logout(@Body LogoutRequest logoutRequest);
}
