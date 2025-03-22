package com.salesapp.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.salesapp.android.MainActivity;
import com.salesapp.android.R;
import com.salesapp.android.data.model.response.AuthResponse;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignUp;
    private ProgressBar progressBar;
    private AuthRepository authRepository;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);

            // Initialize views
            editTextUsername = findViewById(R.id.editTextUsername);
            editTextPassword = findViewById(R.id.editTextPassword);
            buttonLogin = findViewById(R.id.buttonLogin);
            textViewSignUp = findViewById(R.id.textViewSignUp);
            progressBar = findViewById(R.id.progressBar);

            // Initialize repository and preference manager
            authRepository = new AuthRepository();
            preferenceManager = new PreferenceManager(this);

            // Check if user is already logged in
            if (preferenceManager.isLoggedIn()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            // Set click listeners
            buttonLogin.setOnClickListener(v -> loginUser());
            textViewSignUp.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });

        } catch (Exception e) {
            Log.e("LoginActivity", "Error initializing activity", e);
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loginUser() {
        // Get input values
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        // Show progress
        setLoading(true);

        // Perform login
        authRepository.login(username, password, new AuthRepository.AuthCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse result) {
                setLoading(false);

                // Save user session
                preferenceManager.saveUserSession(
                        result.getToken(),
                        result.getId(),
                        result.getUsername(),
                        result.getEmail(),
                        result.getRole()
                );

                // Navigate to main activity
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);
        }
    }
}