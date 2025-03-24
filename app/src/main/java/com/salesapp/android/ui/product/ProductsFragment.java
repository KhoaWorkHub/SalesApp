package com.salesapp.android.ui.product;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.salesapp.android.R;
import com.salesapp.android.data.model.CartItem;
import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.repository.CartRepository;
import com.salesapp.android.data.service.ProductService;
import com.salesapp.android.ui.cart.CartFragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputLayout textInputLayoutSearch;
    private TextInputEditText editTextSearch;
    private ChipGroup chipGroupCategories;
    private TextView textViewEmpty;
    private LottieAnimationView loadingAnimation;
    private FloatingActionButton fabCart;
    private Button buttonFilter;
    private TextView textViewResultCount;

    private ProductViewModel viewModel;
    private PreferenceManager preferenceManager;
    private CartRepository cartRepository;

    // Filter state
    private Long currentCategoryId = null;
    private double minPrice = 0;
    private double maxPrice = 100000.0;
    private String searchQuery = "";
    private boolean isAscendingSort = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products_enhanced, container, false);

        // Initialize views
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        textInputLayoutSearch = view.findViewById(R.id.textInputLayoutSearch);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        loadingAnimation = view.findViewById(R.id.loadingAnimation);
        fabCart = view.findViewById(R.id.fabCart);
        buttonFilter = view.findViewById(R.id.buttonFilter);
        textViewResultCount = view.findViewById(R.id.textViewResultCount);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize preferences
        preferenceManager = new PreferenceManager(requireContext());

        // Initialize cart repository
        cartRepository = new CartRepository(preferenceManager.getToken());

        // Initialize ViewModel
        viewModel = new ProductViewModel(preferenceManager.getToken());

        setupRecyclerView();
        setupSwipeRefresh();
        setupSearchBar();
        setupFilterButton();
        setupCartButton();
        observeViewModel();

        // Load data
        viewModel.loadCategories();
        viewModel.loadProducts();
    }

    private void setupRecyclerView() {
        // Initialize adapter
        productAdapter = new ProductAdapter(requireContext(), this);

        // Setup layout manager
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerViewProducts.setLayoutManager(layoutManager);

        // Add animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_animation_from_bottom);
        recyclerViewProducts.setLayoutAnimation(animation);

        // Set adapter
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.loadProducts();
            viewModel.loadCategories();
        });
    }

    private void setupSearchBar() {
        // Add clear button to search input
        textInputLayoutSearch.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        // Add search text change listener
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();

                // Debounce search queries
                new Handler().removeCallbacksAndMessages(null);
                new Handler().postDelayed(() -> {
                    viewModel.setFilters(searchQuery, currentCategoryId, minPrice, maxPrice);
                }, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFilterButton() {
        buttonFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupCartButton() {
        fabCart.setOnClickListener(v -> {
            // Navigate to cart fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new CartFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void observeViewModel() {
        // Observe products
        viewModel.getFilteredProducts().observe(getViewLifecycleOwner(), products -> {
            Log.d("ProductsFragment", "Received " + (products != null ? products.size() : 0) + " filtered products");

            // Debug what products we're receiving
            if (products != null && !products.isEmpty()) {
                for (Product product : products) {
                    Log.d("ProductsFragment", "Product in UI: " + product.getProductId() +
                            " - " + product.getProductName() + ", price: " + product.getPrice());
                }
            }

            productAdapter.setProducts(products);

            // Update result count
            updateResultCount(products.size());

            // Show empty view if no products
            if (products == null || products.isEmpty()) {
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerViewProducts.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.GONE);
                recyclerViewProducts.setVisibility(View.VISIBLE);

                // Run layout animation
                recyclerViewProducts.scheduleLayoutAnimation();
            }
        });

        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), this::setupCategoryChips);

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d("ProductsFragment", "Loading state changed: " + isLoading);
            if (swipeRefreshLayout.isRefreshing()) {
                if (!isLoading) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                loadingAnimation.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    loadingAnimation.playAnimation();
                } else {
                    loadingAnimation.pauseAnimation();
                }
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e("ProductsFragment", "Error loading products: " + errorMessage);

                // Make error more visible to user
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();

                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG)
                        .setAction("Retry", v -> {
                            viewModel.clearError();
                            viewModel.loadProducts();
                        })
                        .show();
            }
        });

        // Observe price range changes
        viewModel.getMinimumProductPrice().observe(getViewLifecycleOwner(), minProductPrice -> {
            Log.d("ProductsFragment", "Minimum product price updated: " + minProductPrice);
            // Update our local min price if needed
            if (minPrice < minProductPrice) {
                minPrice = minProductPrice;
            }
        });

        viewModel.getMaximumProductPrice().observe(getViewLifecycleOwner(), maxProductPrice -> {
            Log.d("ProductsFragment", "Maximum product price updated: " + maxProductPrice);
            // Update our local max price if needed
            if (maxPrice < maxProductPrice) {
                maxPrice = maxProductPrice;
                // Apply updated price filter
                viewModel.setFilters(searchQuery, currentCategoryId, minPrice, maxPrice);
            }
        });
    }

    private void setupCategoryChips(List<Category> categories) {
        // Clear existing chips
        chipGroupCategories.removeAllViews();

        // Add "All" chip
        Chip allChip = new Chip(requireContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        chipGroupCategories.addView(allChip);

        // Add chips for each category
        for (Category category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category.getCategoryName());
            chip.setCheckable(true);
            chip.setTag(category.getCategoryId());
            chipGroupCategories.addView(chip);
        }

        // Set chip click listener
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                // No chip selected, default to "All"
                currentCategoryId = null;
                allChip.setChecked(true);
                return;
            }

            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip == allChip) {
                currentCategoryId = null;
            } else if (selectedChip != null && selectedChip.getTag() instanceof Long) {
                currentCategoryId = (Long) selectedChip.getTag();
            }

            viewModel.setFilters(searchQuery, currentCategoryId, minPrice, maxPrice);
        });
    }

    private void showFilterDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter_enhanced);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Initialize dialog views
        RangeSlider rangeSliderPrice = dialog.findViewById(R.id.rangeSliderPrice);
        TextView textViewMinPrice = dialog.findViewById(R.id.textViewMinPrice);
        TextView textViewMaxPrice = dialog.findViewById(R.id.textViewMaxPrice);
        RadioGroup radioGroupSort = dialog.findViewById(R.id.radioGroupSort);
        Button buttonReset = dialog.findViewById(R.id.buttonReset);
        Button buttonApply = dialog.findViewById(R.id.buttonApply);

        // Get current price range from ViewModel
        double minProductPrice = viewModel.getMinimumProductPrice().getValue() != null ?
                viewModel.getMinimumProductPrice().getValue() : 0.0;
        double maxProductPrice = viewModel.getMaximumProductPrice().getValue() != null ?
                viewModel.getMaximumProductPrice().getValue() : 100000.0;

        // Set slider range
        rangeSliderPrice.setValueFrom((float) minProductPrice);
        rangeSliderPrice.setValueTo((float) maxProductPrice);

        // Set current filter values
        float currentMinPrice = (float) Math.max(minPrice, minProductPrice);
        float currentMaxPrice = (float) Math.min(maxPrice, maxProductPrice);

        // Set values for the slider
        rangeSliderPrice.setValues(currentMinPrice, currentMaxPrice);

        // Format currency for display
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        textViewMinPrice.setText(currencyFormat.format(currentMinPrice));
        textViewMaxPrice.setText(currencyFormat.format(currentMaxPrice));

        // Set initial sort option
        if (isAscendingSort) {
            radioGroupSort.check(R.id.radioButtonPriceLowToHigh);
        } else {
            radioGroupSort.check(R.id.radioButtonPriceHighToLow);
        }

        // Setup range slider listener
        rangeSliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            textViewMinPrice.setText(currencyFormat.format(values.get(0)));
            textViewMaxPrice.setText(currencyFormat.format(values.get(1)));
        });

        // Setup button click listeners
        buttonReset.setOnClickListener(v -> {
            // Reset to maximum range
            rangeSliderPrice.setValues((float) minProductPrice, (float) maxProductPrice);
            radioGroupSort.check(R.id.radioButtonPriceLowToHigh);

            // Update text displays
            textViewMinPrice.setText(currencyFormat.format(minProductPrice));
            textViewMaxPrice.setText(currencyFormat.format(maxProductPrice));
        });

        buttonApply.setOnClickListener(v -> {
            // Get selected values
            List<Float> priceValues = rangeSliderPrice.getValues();
            minPrice = priceValues.get(0);
            maxPrice = priceValues.get(1);

            // Get sort order
            int checkedRadioButtonId = radioGroupSort.getCheckedRadioButtonId();
            isAscendingSort = checkedRadioButtonId == R.id.radioButtonPriceLowToHigh;

            // Apply filters and sorting
            viewModel.setFilters(searchQuery, currentCategoryId, minPrice, maxPrice);
            viewModel.setSortOrder(isAscendingSort);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateResultCount(int count) {
        if (count == 0) {
            textViewResultCount.setVisibility(View.GONE);
        } else {
            textViewResultCount.setVisibility(View.VISIBLE);
            textViewResultCount.setText(String.format(Locale.getDefault(), "%d product(s) found", count));

            // Animate the count change
            ObjectAnimator.ofFloat(textViewResultCount, "alpha", 0f, 1f)
                    .setDuration(300)
                    .start();
        }
    }

    @Override
    public void onProductClick(Product product) {
        showProductDetailsDialog(product);
    }

    @Override
    public void onAddToCartClick(Product product) {
        // Add to cart animation
        animateAddToCart(product);

        // Actual add to cart logic
        cartRepository.addItemToCart(product.getProductId(), 1, new CartRepository.CartCallback<CartItem>() {
            @Override
            public void onSuccess(CartItem result) {
                Snackbar.make(requireView(), product.getProductName() + " added to cart", Snackbar.LENGTH_SHORT)
                        .setAction("View Cart", v -> {
                            // Navigate to cart fragment
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, new CartFragment())
                                        .addToBackStack(null)
                                        .commit();
                            }
                        })
                        .show();
            }

            @Override
            public void onError(String message) {
                Snackbar.make(requireView(), "Error adding to cart: " + message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void animateAddToCart(Product product) {
        // This is just a placeholder - you would implement a more sophisticated animation
        // like a small image of the product flying to the cart button
        fabCart.setScaleX(0.8f);
        fabCart.setScaleY(0.8f);

        new Handler().postDelayed(() -> {
            fabCart.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        fabCart.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }, 100);
    }

    private void showProductDetailsDialog(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_product_details, null);
        dialog.setContentView(view);

        // Initialize views
        ImageView imageViewProduct = view.findViewById(R.id.imageViewProduct);
        TextView textViewProductName = view.findViewById(R.id.textViewProductName);
        TextView textViewCategory = view.findViewById(R.id.textViewCategory);
        TextView textViewPrice = view.findViewById(R.id.textViewPrice);
        TextView textViewDescription = view.findViewById(R.id.textViewDescription);
        TextView textViewTechnicalSpecs = view.findViewById(R.id.textViewTechnicalSpecs);
        Button buttonAddToCart = view.findViewById(R.id.buttonAddToCart);

        // Load product image
        if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
            Glide.with(requireContext())
                    .load(product.getImageURL())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageViewProduct);
        }

        // Set product details
        textViewProductName.setText(product.getProductName());

        if (product.getCategory() != null) {
            textViewCategory.setText(product.getCategory().getCategoryName());
            textViewCategory.setVisibility(View.VISIBLE);
        } else {
            textViewCategory.setVisibility(View.GONE);
        }

        // Format price with currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        textViewPrice.setText(currencyFormat.format(product.getPrice()));

        // Set description
        if (product.getFullDescription() != null && !product.getFullDescription().isEmpty()) {
            textViewDescription.setText(product.getFullDescription());
        } else if (product.getBriefDescription() != null && !product.getBriefDescription().isEmpty()) {
            textViewDescription.setText(product.getBriefDescription());
        } else {
            textViewDescription.setText("No description available");
        }

        // Set technical specifications
        if (product.getTechnicalSpecifications() != null && !product.getTechnicalSpecifications().isEmpty()) {
            textViewTechnicalSpecs.setText(product.getTechnicalSpecifications());
            textViewTechnicalSpecs.setVisibility(View.VISIBLE);
        } else {
            textViewTechnicalSpecs.setVisibility(View.GONE);
        }

        // Setup add to cart button
        buttonAddToCart.setOnClickListener(v -> {
            onAddToCartClick(product);
            dialog.dismiss();
        });

        dialog.show();
    }
}