package com.salesapp.android.data.model;

import java.io.Serializable;
import java.util.List;

public class Cart implements Serializable {
    private Long cartId;
    private User user;
    private double totalPrice;
    private String status;
    private List<CartItem> cartItems;

    // No-arg constructor for Gson
    public Cart() {
    }

    // Constructor with all fields
    public Cart(Long cartId, User user, double totalPrice, String status, List<CartItem> cartItems) {
        this.cartId = cartId;
        this.user = user;
        this.totalPrice = totalPrice;
        this.status = status;
        this.cartItems = cartItems;
    }

    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    // Helper method to get total items count
    public int getTotalItemsCount() {
        if (cartItems == null) {
            return 0;
        }

        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}