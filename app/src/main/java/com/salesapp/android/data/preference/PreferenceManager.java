package com.salesapp.android.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.salesapp.android.utils.Constants;

public class PreferenceManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setToken(String token) {
        editor.putString(Constants.KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(Constants.KEY_TOKEN, null);
    }

    public void setUserId(long userId) {
        editor.putLong(Constants.KEY_USER_ID, userId);
        editor.apply();
    }

    public long getUserId() {
        return sharedPreferences.getLong(Constants.KEY_USER_ID, -1);
    }

    public void setUsername(String username) {
        editor.putString(Constants.KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(Constants.KEY_USERNAME, null);
    }

    public void setEmail(String email) {
        editor.putString(Constants.KEY_EMAIL, email);
        editor.apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(Constants.KEY_EMAIL, null);
    }

    public void setRole(String role) {
        editor.putString(Constants.KEY_ROLE, role);
        editor.apply();
    }

    public String getRole() {
        return sharedPreferences.getString(Constants.KEY_ROLE, null);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public void clearPreferences() {
        editor.clear();
        editor.apply();
    }

    public void logout() {
        clearPreferences();
    }

    public void saveUserSession(String token, long userId, String username, String email, String role) {
        setToken(token);
        setUserId(userId);
        setUsername(username);
        setEmail(email);
        setRole(role);
        setLoggedIn(true);
    }
}