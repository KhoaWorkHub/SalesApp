package com.salesapp.android.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.salesapp.android.R;
import com.salesapp.android.ui.cart.CartFragment.CartItemResponseWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItemResponseWrapper> cartItems = new ArrayList<>();
    private final Context context;
    private final CartItemListener listener;

    public interface CartItemListener {
        void onRemoveItem(CartItemResponseWrapper cartItem);
        void onUpdateQuantity(CartItemResponseWrapper cartItem, int newQuantity);
    }

    public CartAdapter(Context context, CartItemListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemResponseWrapper cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setCartItems(List<CartItemResponseWrapper> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    public List<CartItemResponseWrapper> getCartItems() {
        return cartItems;
    }

    public double calculateTotal() {
        double total = 0;
        for (CartItemResponseWrapper item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewProduct;
        private final TextView textViewProductName;
        private final TextView textViewProductPrice;
        private final TextView textViewQuantity;
        private final TextView textViewItemTotal;
        private final Button buttonDecrease;
        private final Button buttonIncrease;
        private final ImageButton buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewItemTotal = itemView.findViewById(R.id.textViewItemTotal);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }

        public void bind(CartItemResponseWrapper cartItem) {
            // Set product name
            textViewProductName.setText(cartItem.getProductName());

            // Set product price
            textViewProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", cartItem.getPrice()));

            // Load product image
            if (cartItem.getImageURL() != null && !cartItem.getImageURL().isEmpty()) {
                Glide.with(context)
                        .load(cartItem.getImageURL())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_launcher_background);
            }

            // Set quantity and calculate item total
            textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));

            // Set subtotal
            textViewItemTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", cartItem.getSubtotal()));

            // Set click listeners
            buttonDecrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() - 1;
                if (newQuantity > 0) {
                    listener.onUpdateQuantity(cartItem, newQuantity);
                }
            });

            buttonIncrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() + 1;
                listener.onUpdateQuantity(cartItem, newQuantity);
            });

            buttonRemove.setOnClickListener(v -> {
                listener.onRemoveItem(cartItem);
            });
        }
    }
}