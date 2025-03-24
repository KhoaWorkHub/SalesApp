package com.salesapp.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.service.CartNotificationService;

/**
 * Receives boot completed event to start our service
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, checking login status");

            // Only start service if user is logged in
            PreferenceManager preferenceManager = new PreferenceManager(context);
            if (preferenceManager.isLoggedIn()) {
                Log.d(TAG, "User is logged in, starting cart notification service");
                Intent serviceIntent = new Intent(context, CartNotificationService.class);
                context.startService(serviceIntent);
            }
        }
    }
}