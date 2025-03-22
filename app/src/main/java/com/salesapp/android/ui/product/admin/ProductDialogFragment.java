package com.salesapp.android.ui.product.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.salesapp.android.R;
import com.salesapp.android.data.callback.ProductCallback;
import com.salesapp.android.data.model.Category;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.model.request.ProductRequest;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.data.service.ProductService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog fragment for creating and editing products
 */
public class ProductDialogFragment extends DialogFragment {

    private static final String ARG_PRODUCT = "product";

    private Product product;
    private List<Category> categories = new ArrayList<>();
    private ProductDialogListener listener;
    private ProductService productService;

    // UI components
    private TextInputEditText editTextName;
    private TextInputEditText editTextBriefDesc;
    private TextInputEditText editTextFullDesc;
    private TextInputEditText editTextTechSpecs;
    private TextInputEditText editTextPrice;
    private TextInputEditText editTextImageUrl;
    private Spinner spinnerCategory;
    private Button buttonSave;
    private Button buttonCancel;

    public interface ProductDialogListener {
        void onProductSaved(Product product);
    }

    public static ProductDialogFragment newInstance(Product product) {
        ProductDialogFragment fragment = new ProductDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    public void setProductDialogListener(ProductDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_PRODUCT)) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }

        // Initialize product service
        PreferenceManager preferenceManager = new PreferenceManager(requireContext());
        productService = new ProductService(preferenceManager.getToken());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_product_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        editTextName = view.findViewById(R.id.editTextProductName);
        editTextBriefDesc = view.findViewById(R.id.editTextBriefDescription);
        editTextFullDesc = view.findViewById(R.id.editTextFullDescription);
        editTextTechSpecs = view.findViewById(R.id.editTextTechSpecs);
        editTextPrice = view.findViewById(R.id.editTextPrice);
        editTextImageUrl = view.findViewById(R.id.editTextImageUrl);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        // Set dialog title
        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        textViewTitle.setText(product == null ? "Add New Product" : "Edit Product");

        // Load categories
        loadCategories();

        // Set existing data if editing
        if (product != null) {
            populateProductData();
        }

        // Set button listeners
        buttonSave.setOnClickListener(v -> validateAndSaveProduct());
        buttonCancel.setOnClickListener(v -> dismiss());
    }

    private void loadCategories() {
        productService.getAllCategories(new ProductCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories = result;
                setupCategorySpinner();

                // If editing, select the product's category
                if (product != null && product.getCategory() != null) {
                    selectProductCategory();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), "Error loading categories: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void selectProductCategory() {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getCategoryId().equals(product.getCategory().getCategoryId())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void populateProductData() {
        editTextName.setText(product.getProductName());
        editTextBriefDesc.setText(product.getBriefDescription());
        editTextFullDesc.setText(product.getFullDescription());
        editTextTechSpecs.setText(product.getTechnicalSpecifications());
        editTextPrice.setText(String.valueOf(product.getPrice()));
        editTextImageUrl.setText(product.getImageURL());
    }

    private void validateAndSaveProduct() {
        // Get form data
        String name = editTextName.getText().toString().trim();
        String briefDesc = editTextBriefDesc.getText().toString().trim();
        String fullDesc = editTextFullDesc.getText().toString().trim();
        String techSpecs = editTextTechSpecs.getText().toString().trim();
        String priceText = editTextPrice.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        // Validate form data
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Product name is required");
            return;
        }

        if (TextUtils.isEmpty(priceText)) {
            editTextPrice.setError("Price is required");
            return;
        }

        try {
            BigDecimal price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                editTextPrice.setError("Price must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            editTextPrice.setError("Invalid price format");
            return;
        }

        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create ProductRequest
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        ProductRequest productRequest = new ProductRequest(
                name,
                briefDesc,
                fullDesc,
                techSpecs,
                new BigDecimal(priceText),
                imageUrl,
                selectedCategory.getCategoryId()
        );

        // Save product
        if (product == null) {
            // Create new product
            createProduct(productRequest);
        } else {
            // Update existing product
            updateProduct(product.getProductId(), productRequest);
        }
    }

    private void createProduct(ProductRequest productRequest) {
        productService.createProduct(productRequest, new ProductCallback<Product>() {
            @Override
            public void onSuccess(Product result) {
                if (listener != null) {
                    listener.onProductSaved(result);
                }
                dismiss();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), "Error creating product: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct(long productId, ProductRequest productRequest) {
        productService.updateProduct(productId, productRequest, new ProductCallback<Product>() {
            @Override
            public void onSuccess(Product result) {
                if (listener != null) {
                    listener.onProductSaved(result);
                }
                dismiss();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), "Error updating product: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}