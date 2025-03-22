package com.salesapp.android.data.model.request;

import com.google.gson.annotations.SerializedName;

public class CartItemRequest {
    @SerializedName("productId")
    private Long productId;

    @SerializedName("quantity")
    private int quantity;

    public CartItemRequest(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}