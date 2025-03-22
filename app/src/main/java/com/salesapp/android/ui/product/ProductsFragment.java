package com.salesapp.android.ui.product;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.salesapp.android.R;
import com.salesapp.android.data.model.CartItem;
import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.CartRepository;
import com.salesapp.android.data.repository.ProductRepository;
import com.salesapp.android.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private ChipGroup chipGroupCategories;
    private TextInputEditText editTextSearch;
    private FloatingActionButton fabCart;

    private ProductRepository productRepository;
    private PreferenceManager preferenceManager;
    private List<Category> categories = new ArrayList<>();

    // Filter state
    private Long currentCategoryId = null;
    private double minPrice = 0;
    private double maxPrice = 1000;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        progressBar = view.findViewById(R.id.progressBar);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        fabCart = view.findViewById(R.id.fabCart);
        Button buttonFilter = view.findViewById(R.id.buttonFilter);

        // Initialize preferences and repositories
        preferenceManager = new PreferenceManager(requireContext());
        productRepository = new ProductRepository(preferenceManager.getToken());

        // Setup RecyclerView
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productAdapter = new ProductAdapter(requireContext(), this);
        recyclerViewProducts.setAdapter(productAdapter);

        // Setup search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Setup filter button
        buttonFilter.setOnClickListener(v -> showFilterDialog());

        // Setup FAB cart button
        fabCart.setOnClickListener(v -> {
            // Handle cart click
            Toast.makeText(requireContext(), "Cart feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Load data
        loadCategories();
        loadProducts();
    }

    private void loadCategories() {
        productRepository.getAllCategories(new ProductRepository.ProductCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories = result;
                populateCategories();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateCategories() {
        // Clear previous chips except the "All" chip
        int childCount = chipGroupCategories.getChildCount();
        if (childCount > 1) {
            chipGroupCategories.removeViews(1, childCount - 1);
        }

        // Add a chip for each category
        for (Category category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category.getCategoryName());
            chip.setCheckable(true);
            chip.setTag(category.getCategoryId());
            chipGroupCategories.addView(chip);
        }

        // Set chip click listener
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentCategoryId = null;
            } else {
                Chip selectedChip = group.findViewById(checkedId);
                if (selectedChip != null && selectedChip.getTag() instanceof Long) {
                    currentCategoryId = (Long) selectedChip.getTag();
                }
            }
            applyFilters();
        });
    }

    private void loadProducts() {
        showLoading(true);

        productRepository.getAllProducts(new ProductRepository.ProductCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                showLoading(false);

                if (result.isEmpty()) {
                    showEmptyView(true);
                } else {
                    showEmptyView(false);
                    productAdapter.setProducts(result);
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showEmptyView(true);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFilterDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize dialog views
        RangeSlider rangeSliderPrice = dialog.findViewById(R.id.rangeSliderPrice);
        TextView textViewMinPrice = dialog.findViewById(R.id.textViewMinPrice);
        TextView textViewMaxPrice = dialog.findViewById(R.id.textViewMaxPrice);
        RadioGroup radioGroupSort = dialog.findViewById(R.id.radioGroupSort);
        Button buttonReset = dialog.findViewById(R.id.buttonReset);
        Button buttonApply = dialog.findViewById(R.id.buttonApply);

        // Set initial values
        rangeSliderPrice.setValues(Float.valueOf((float) minPrice), Float.valueOf((float) maxPrice));
        textViewMinPrice.setText(String.format(Locale.getDefault(), "$%.0f", minPrice));
        textViewMaxPrice.setText(String.format(Locale.getDefault(), "$%.0f", maxPrice));

        // Setup range slider listener
        rangeSliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            textViewMinPrice.setText(String.format(Locale.getDefault(), "$%.0f", values.get(0)));
            textViewMaxPrice.setText(String.format(Locale.getDefault(), "$%.0f", values.get(1)));
        });

        // Setup button click listeners
        buttonReset.setOnClickListener(v -> {
            rangeSliderPrice.setValues(0f, 1000f);
            radioGroupSort.clearCheck();
        });

        buttonApply.setOnClickListener(v -> {
            // Get selected values
            List<Float> priceValues = rangeSliderPrice.getValues();
            minPrice = priceValues.get(0);
            maxPrice = priceValues.get(1);

            // Apply sorting if selected
            int checkedRadioButtonId = radioGroupSort.getCheckedRadioButtonId();
            if (checkedRadioButtonId == R.id.radioButtonPriceLowToHigh) {
                productAdapter.sortByPrice(true);
            } else if (checkedRadioButtonId == R.id.radioButtonPriceHighToLow) {
                productAdapter.sortByPrice(false);
            }

            // Apply filters
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        productAdapter.filter(searchQuery, currentCategoryId, minPrice, maxPrice);

        if (productAdapter.getFilteredProducts().isEmpty()) {
            showEmptyView(true);
        } else {
            showEmptyView(false);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        textViewEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        // Handle product click (open product details)
        Toast.makeText(requireContext(), "Product: " + product.getProductName(), Toast.LENGTH_SHORT).show();

        // Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
        // intent.putExtra(Constants.EXTRA_PRODUCT, product);
        // startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        // Handle add to cart
        Toast.makeText(requireContext(), product.getProductName() + " added to cart", Toast.LENGTH_SHORT).show();
    }
}