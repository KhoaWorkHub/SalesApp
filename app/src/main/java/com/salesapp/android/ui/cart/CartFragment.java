package com.salesapp.android.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.salesapp.android.R;
import com.salesapp.android.data.model.Cart;
import com.salesapp.android.data.model.CartItem;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.CartRepository;

import java.util.ArrayList;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.CartItemListener {
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
    private Cart currentCart;

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

        cartRepository.getActiveCart(new CartRepository.CartCallback<Cart>() {
            @Override
            public void onSuccess(Cart result) {
                showLoading(false);
                currentCart = result;

                if (result.getCartItems() == null || result.getCartItems().isEmpty()) {
                    showEmptyCart(true);
                } else {
                    showEmptyCart(false);
                    cartAdapter.setCartItems(result.getCartItems());
                    updateOrderSummary();
                }

                // Update cart badge
                updateCartBadge();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showEmptyCart(true);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderSummary() {
        double total = cartAdapter.calculateTotal();

        // Update summary views
        textViewSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
        textViewTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
        textViewCheckoutTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
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
                com.salesapp.android.utils.BadgeUtils.updateCartBadge(requireContext(), bottomNavigationView);
            }
        }
    }

    @Override
    public void onRemoveItem(CartItem cartItem) {
        if (cartItem.getCartItemId() != null) {
            showLoading(true);

            cartRepository.deleteCartItem(cartItem.getCartItemId(), new CartRepository.CartCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    showLoading(false);
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();

                    // Remove item from adapter
                    ArrayList<CartItem> updatedItems = new ArrayList<>(cartAdapter.getCartItems());
                    updatedItems.remove(cartItem);

                    if (updatedItems.isEmpty()) {
                        showEmptyCart(true);
                    } else {
                        cartAdapter.setCartItems(updatedItems);
                        updateOrderSummary();
                    }

                    // Update cart badge
                    updateCartBadge();
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onUpdateQuantity(CartItem cartItem, int newQuantity) {
        if (cartItem.getCartItemId() != null) {
            showLoading(true);

            cartRepository.updateCartItem(cartItem.getCartItemId(), newQuantity, new CartRepository.CartCallback<CartItem>() {
                @Override
                public void onSuccess(CartItem result) {
                    showLoading(false);

                    // Update item in adapter
                    ArrayList<CartItem> updatedItems = new ArrayList<>(cartAdapter.getCartItems());
                    for (int i = 0; i < updatedItems.size(); i++) {
                        if (updatedItems.get(i).getCartItemId().equals(result.getCartItemId())) {
                            updatedItems.set(i, result);
                            break;
                        }
                    }

                    cartAdapter.setCartItems(updatedItems);
                    updateOrderSummary();

                    // Update cart badge
                    updateCartBadge();
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}