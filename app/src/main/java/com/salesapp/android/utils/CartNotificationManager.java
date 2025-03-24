package com.salesapp.android.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.salesapp.android.MainActivity;
import com.salesapp.android.R;
import com.salesapp.android.data.model.response.CartResponse;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.CartRepository;

/**
 * Manages cart notifications, including app icon badge and system notifications
 */
public class CartNotificationManager {
    private static final String TAG = "CartNotificationManager";
    private static final String CHANNEL_ID = "cart_notification_channel";
    private static final int NOTIFICATION_ID = 1001;

    private final Context context;
    private final PreferenceManager preferenceManager;

    public CartNotificationManager(Context context) {
        this.context = context;
        this.preferenceManager = new PreferenceManager(context);
        createNotificationChannel();
    }

    /**
     * Create the notification channel for Android 8.0 and higher
     */
    private void createNotificationChannel() {
        // Only required for API 26+ (Android 8.0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cart Notifications";
            String description = "Shows notifications about your shopping cart";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    /**
     * Update cart notification status based on current cart data
     */
    public void updateCartNotification() {
        // Skip if not logged in
        if (!preferenceManager.isLoggedIn()) {
            clearNotification();
            return;
        }

        // Get cart data
        String token = preferenceManager.getToken();
        CartRepository cartRepository = new CartRepository(token);

        cartRepository.getCart(new CartRepository.CartCallback<CartResponse>() {
            @Override
            public void onSuccess(CartResponse result) {
                if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                    int itemCount = result.getTotalItemsCount();
                    if (itemCount > 0) {
                        showCartNotification(itemCount, result.getTotalPrice().toString());
                    } else {
                        clearNotification();
                    }
                } else {
                    clearNotification();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error fetching cart: " + message);
                clearNotification();
            }
        });
    }

    /**
     * Show cart notification with item count
     */
    private void showCartNotification(int itemCount, String totalPrice) {
        // Create intent for when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("openCart", true);  // Extra to indicate cart should open

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        String contentText = itemCount + " " +
                (itemCount == 1 ? "item" : "items") +
                " in your cart, total: $" + totalPrice;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Shopping Cart")
                .setContentText(contentText)
                .setNumber(itemCount)  // This sets the badge count on supported launchers
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Cart notification shown with " + itemCount + " items");
        } catch (SecurityException e) {
            Log.e(TAG, "No notification permission: " + e.getMessage());
        }
    }

    /**
     * Clear cart notification
     */
    public void clearNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
        Log.d(TAG, "Cart notification cleared");
    }
}