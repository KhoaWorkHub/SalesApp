package com.salesapp.android.data.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateCartItemRequest {
    @SerializedName("cartItemId")
    private Long cartItemId;

    @SerializedName("quantity")
    private int quantity;

    public UpdateCartItemRequest(Long cartItemId, int quantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}