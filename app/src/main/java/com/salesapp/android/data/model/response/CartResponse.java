package com.salesapp.android.data.model.response;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    @SerializedName("cartId")
    private Long cartId;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("status")
    private String status;

    @SerializedName("totalPrice")
    private BigDecimal totalPrice;

    @SerializedName("items")
    private List<CartItemResponse> items;

    @SerializedName("itemCount")
    private int itemCount;

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    // Helper method to get total items count
    public int getTotalItemsCount() {
        if (items == null) {
            return 0;
        }

        int count = 0;
        for (CartItemResponse item : items) {
            count += item.getQuantity();
        }
        return count;
    }
}