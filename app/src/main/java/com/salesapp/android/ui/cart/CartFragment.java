package com.salesapp.android.ui.cart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.salesapp.android.R;
import com.salesapp.android.data.model.response.CartResponse;
import com.salesapp.android.data.model.response.CartItemResponse;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.CartRepository;
import com.salesapp.android.utils.BadgeUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.CartItemListener {
    private static final String TAG = "CartFragment";

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private Button buttonStartShopping;
    private CardView cardViewSummary;
    private MaterialCardView cardViewCheckout;
    private TextView textViewSubtotal, textViewTotal, textViewCheckoutTotal;
    private Button buttonCheckout;

    private CartRepository cartRepository;
    private PreferenceManager preferenceManager;
    private CartResponse currentCart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        buttonStartShopping = view.findViewById(R.id.buttonStartShopping);
        cardViewSummary = view.findViewById(R.id.cardViewSummary);
        cardViewCheckout = view.findViewById(R.id.cardViewCheckout);
        textViewSubtotal = view.findViewById(R.id.textViewSubtotal);
        textViewTotal = view.findViewById(R.id.textViewTotal);
        textViewCheckoutTotal = view.findViewById(R.id.textViewCheckoutTotal);
        buttonCheckout = view.findViewById(R.id.buttonCheckout);

        // Initialize repositories and preferences
        preferenceManager = new PreferenceManager(requireContext());
        cartRepository = new CartRepository(preferenceManager.getToken());

        // Setup RecyclerView
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        cartAdapter = new CartAdapter(requireContext(), this);
        recyclerViewCart.setAdapter(cartAdapter);

        // Setup click listeners
        buttonStartShopping.setOnClickListener(v -> {
            // Navigate to products fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        buttonCheckout.setOnClickListener(v -> {
            // Navigate to checkout
            Toast.makeText(requireContext(), "Checkout functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Load cart data
        loadCart();
    }

    private void loadCart() {
        showLoading(true);

        cartRepository.getCart(new CartRepository.CartCallback<CartResponse>() {
            @Override
            public void onSuccess(CartResponse result) {
                showLoading(false);
                currentCart = result;

                if (result.getItems() == null || result.getItems().isEmpty()) {
                    showEmptyCart(true);
                } else {
                    showEmptyCart(false);
                    updateCartAdapter(result.getItems());
                    updateOrderSummary(result.getTotalPrice());
                }

                // Update cart badge
                updateCartBadge();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showEmptyCart(true);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading cart: " + message);
            }
        });
    }

    private void updateCartAdapter(List<CartItemResponse> items) {
        // Convert CartItemResponse to CartItem for adapter
        List<CartItemResponseWrapper> wrappers = new ArrayList<>();

        for (CartItemResponse item : items) {
            wrappers.add(new CartItemResponseWrapper(item));
        }

        cartAdapter.setCartItems(wrappers);
    }

    private void updateOrderSummary(BigDecimal totalPrice) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        String formattedTotal = currencyFormat.format(totalPrice);

        // Update summary views
        textViewSubtotal.setText(formattedTotal);
        textViewTotal.setText(formattedTotal);
        textViewCheckoutTotal.setText(formattedTotal);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewCart.setVisibility(show ? View.GONE : View.VISIBLE);
        cardViewSummary.setVisibility(show ? View.GONE : View.VISIBLE);
        cardViewCheckout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyCart(boolean isEmpty) {
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        cardViewSummary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        cardViewCheckout.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateCartBadge() {
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
            if (bottomNavigationView != null) {
                BadgeUtils.updateCartBadge(requireContext(), bottomNavigationView);
            }
        }
    }

    @Override
    public void onRemoveItem(CartItemResponseWrapper item) {
        if (item.getCartItemId() != null) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Item")
                    .setMessage("Are you sure you want to remove this item from your cart?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        showLoading(true);
                        cartRepository.removeCartItem(item.getCartItemId(), new CartRepository.CartCallback<CartResponse>() {
                            @Override
                            public void onSuccess(CartResponse result) {
                                showLoading(false);
                                Toast.makeText(requireContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();

                                if (result.getItems() == null || result.getItems().isEmpty()) {
                                    showEmptyCart(true);
                                } else {
                                    updateCartAdapter(result.getItems());
                                    updateOrderSummary(result.getTotalPrice());
                                }

                                // Update cart badge
                                updateCartBadge();
                            }

                            @Override
                            public void onError(String message) {
                                showLoading(false);
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error removing item: " + message);
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public void onUpdateQuantity(CartItemResponseWrapper item, int newQuantity) {
        if (item.getCartItemId() != null) {
            showLoading(true);

            cartRepository.updateCartItem(item.getCartItemId(), newQuantity, new CartRepository.CartCallback<CartResponse>() {
                @Override
                public void onSuccess(CartResponse result) {
                    showLoading(false);
                    updateCartAdapter(result.getItems());
                    updateOrderSummary(result.getTotalPrice());

                    // Update cart badge
                    updateCartBadge();
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating quantity: " + message);
                }
            });
        }
    }

    /**
     * Wrapper class to adapt CartItemResponse to the CartAdapter interface
     */
    public static class CartItemResponseWrapper {
        private CartItemResponse response;

        public CartItemResponseWrapper(CartItemResponse response) {
            this.response = response;
        }

        public Long getCartItemId() {
            return response.getCartItemId();
        }

        public Long getProductId() {
            return response.getProductId();
        }

        public String getProductName() {
            return response.getProductName();
        }

        public String getImageURL() {
            return response.getProductImage();
        }

        public double getPrice() {
            return response.getPrice().doubleValue();
        }

        public int getQuantity() {
            return response.getQuantity();
        }

        public double getSubtotal() {
            return response.getSubtotal().doubleValue();
        }
    }
}