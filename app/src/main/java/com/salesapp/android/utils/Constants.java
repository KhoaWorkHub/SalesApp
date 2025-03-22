package com.salesapp.android.utils;

public class Constants {
    // API URLs
    public static final String BASE_URL = "http://10.0.2.2:8080/";  // For Android Emulator: 10.0.2.2 points to host machine's localhost

    // For real device testing, use your machine's IP address
    // public static final String BASE_URL = "http://192.168.1.100:8080/";

    // Shared Preferences
    public static final String PREFS_NAME = "SalesAppPrefs";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ROLE = "role";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Intent extras
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_PRODUCT = "product";
    public static final String EXTRA_CART = "cart";
    public static final String EXTRA_ORDER = "order";

    // Cart status
    public static final String CART_STATUS_ACTIVE = "active";
    public static final String CART_STATUS_COMPLETED = "completed";

    // Order status
    public static final String ORDER_STATUS_PROCESSING = "processing";
    public static final String ORDER_STATUS_SHIPPED = "shipped";
    public static final String ORDER_STATUS_DELIVERED = "delivered";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";

    // Payment methods
    public static final String PAYMENT_METHOD_VNPAY = "VNPay";
    public static final String PAYMENT_METHOD_ZALOPAY = "ZaloPay";
    public static final String PAYMENT_METHOD_PAYPAL = "PayPal";
    public static final String PAYMENT_METHOD_COD = "Cash On Delivery";

    // Notification channels
    public static final String NOTIFICATION_CHANNEL_ID = "sales_app_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Sales App Notifications";

    // Others
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int CART_BADGE_MAX_COUNT = 99;
}