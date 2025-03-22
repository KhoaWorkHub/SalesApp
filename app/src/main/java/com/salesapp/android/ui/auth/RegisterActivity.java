package com.salesapp.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.salesapp.android.R;
import com.salesapp.android.data.model.response.AuthResponse;
import com.salesapp.android.data.repository.AuthRepository;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername, editTextEmail, editTextPassword,
            editTextPhone, editTextAddress;
    private Button buttonRegister;
    private ImageView imageViewBack;
    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonRegister = findViewById(R.id.buttonRegister);
        imageViewBack = findViewById(R.id.imageViewBack);
        progressBar = findViewById(R.id.progressBar);

        // Initialize repository
        authRepository = new AuthRepository();

        // Set click listeners
        buttonRegister.setOnClickListener(v -> registerUser());
        imageViewBack.setOnClickListener(v -> onBackPressed());
    }

    private void registerUser() {
        // Get input values
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        // Show progress
        setLoading(true);

        // Perform registration
        authRepository.register(username, email, password, phone, address,
                new AuthRepository.AuthCallback<AuthResponse>() {
                    @Override
                    public void onSuccess(AuthResponse result) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful. Please login.", Toast.LENGTH_LONG).show();

                        // Navigate back to login
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setEnabled(true);
        }
    }
}