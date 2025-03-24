package com.salesapp.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.salesapp.android.utils.CartNotificationManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background service that periodically checks for cart updates
 * and shows notifications when the app is closed
 */
public class CartNotificationService extends Service {
    private static final String TAG = "CartNotificationService";
    private static final int UPDATE_INTERVAL_MINUTES = 15;

    private CartNotificationManager notificationManager;
    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Cart notification service created");

        notificationManager = new CartNotificationManager(this);
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Cart notification service started");

        if (!isRunning) {
            startScheduler();
            isRunning = true;
        }

        // If we get killed, restart the service
        return START_STICKY;
    }

    private void startScheduler() {
        // Run immediately on start
        updateCartNotification();

        // Then schedule periodic updates
        scheduler.scheduleAtFixedRate(
                this::updateCartNotification,
                UPDATE_INTERVAL_MINUTES,
                UPDATE_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );

        Log.d(TAG, "Notification scheduler started");
    }

    private void updateCartNotification() {
        try {
            Log.d(TAG, "Checking for cart updates...");
            notificationManager.updateCartNotification();
        } catch (Exception e) {
            Log.e(TAG, "Error updating cart notification", e);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Cart notification service stopped");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}