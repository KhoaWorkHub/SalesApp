package com.salesapp.android.utils;

import android.content.Context;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.salesapp.android.R;
import com.salesapp.android.data.model.response.CartResponse;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.CartRepository;

public class BadgeUtils {

    /**
     * Update cart badge in bottom navigation
     */
    public static void updateCartBadge(@NonNull Context context, @NonNull BottomNavigationView bottomNavigationView) {
        // Get token from preferences
        PreferenceManager preferenceManager = new PreferenceManager(context);
        String token = preferenceManager.getToken();

        if (token == null || token.isEmpty()) {
            return;
        }

        // Create cart repository
        CartRepository cartRepository = new CartRepository(token);

        // Get cart
        cartRepository.getCart(new CartRepository.CartCallback<CartResponse>() {
            @Override
            public void onSuccess(CartResponse result) {
                if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                    // Calculate total items in cart
                    int itemCount = result.getTotalItemsCount();

                    // Update badge
                    if (itemCount > 0) {
                        MenuItem menuItem = bottomNavigationView.getMenu().findItem(R.id.nav_cart);
                        if (menuItem != null) {
                            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(menuItem.getItemId());
                            badge.setVisible(true);

                            // Cap at 99+ for better UI
                            if (itemCount > Constants.CART_BADGE_MAX_COUNT) {
                                badge.setNumber(Constants.CART_BADGE_MAX_COUNT);
                                badge.setVerticalOffset(10);
                            } else {
                                badge.setNumber(itemCount);
                            }
                        }
                    } else {
                        clearBadge(bottomNavigationView, R.id.nav_cart);
                    }
                } else {
                    clearBadge(bottomNavigationView, R.id.nav_cart);
                }
            }

            @Override
            public void onError(String message) {
                clearBadge(bottomNavigationView, R.id.nav_cart);
            }
        });
    }

    /**
     * Clear badge for a menu item
     */
    public static void clearBadge(@NonNull BottomNavigationView bottomNavigationView, int itemId) {
        BadgeDrawable badge = bottomNavigationView.getBadge(itemId);
        if (badge != null) {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }
}