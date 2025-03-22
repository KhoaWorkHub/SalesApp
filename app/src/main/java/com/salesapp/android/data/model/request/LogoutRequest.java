package com.salesapp.android.data.model.request;

public class LogoutRequest {
    private String token;

    public LogoutRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}