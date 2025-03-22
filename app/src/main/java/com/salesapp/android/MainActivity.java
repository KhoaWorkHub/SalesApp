package com.salesapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.salesapp.android.data.model.response.AuthResponse;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.AuthRepository;
import com.salesapp.android.ui.auth.LoginActivity;
import com.salesapp.android.ui.product.ProductsFragment;

public class MainActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private AuthRepository authRepository;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_with_fragments);

        // Initialize edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize repositories and preference manager
        preferenceManager = new PreferenceManager(this);
        authRepository = new AuthRepository();

        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Home is selected
                loadFragment(new ProductsFragment());
                return true;
            } else if (itemId == R.id.nav_cart) {
                // Cart is selected
                loadFragment(new com.salesapp.android.ui.cart.CartFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Profile is selected
                showProfileOptions();
                return true;
            }

            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Update cart badge
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update cart badge when activity resumes
        updateCartBadge();
    }

    private void updateCartBadge() {
        // Use BadgeUtils to update the cart badge
        com.salesapp.android.utils.BadgeUtils.updateCartBadge(this, bottomNavigationView);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void showProfileOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_profile_options, null);
        builder.setView(dialogView);

        // Set user info
        TextView textViewUsername = dialogView.findViewById(R.id.textViewUsername);
        TextView textViewEmail = dialogView.findViewById(R.id.textViewEmail);
        Button buttonLogout = dialogView.findViewById(R.id.buttonLogout);

        textViewUsername.setText(preferenceManager.getUsername());
        textViewEmail.setText(preferenceManager.getEmail());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set logout click listener
        buttonLogout.setOnClickListener(v -> {
            dialog.dismiss();
            showLogoutConfirmationDialog();
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        // Show loading indicator
        View loadingOverlay = findViewById(R.id.loadingOverlay);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }

        // Call logout API
        String token = preferenceManager.getToken();
        authRepository.logout(token, new AuthRepository.AuthCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse result) {
                // Hide loading indicator
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(View.GONE);
                }

                // Clear preferences and redirect to login
                preferenceManager.logout();

                // Show success message
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                // Redirect to login
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                // Hide loading indicator
                if (loadingOverlay != null) {
                    loadingOverlay.setVisibility(View.GONE);
                }

                // Show error message
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                // Even if API call fails, clear local data and redirect to login
                preferenceManager.logout();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}