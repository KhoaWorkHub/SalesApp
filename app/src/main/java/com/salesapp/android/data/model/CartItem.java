package com.salesapp.android.data.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private Long cartItemId;
    private Long cartId;
    private Product product;
    private int quantity;
    private double price;

    // No-arg constructor for Gson
    public CartItem() {
    }

    // Constructor with all fields
    public CartItem(Long cartItemId, Long cartId, Product product, int quantity, double price) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Helper method to get total price for this item
    public double getTotalPrice() {
        return price * quantity;
    }
}