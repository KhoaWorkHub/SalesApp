package com.salesapp.android.ui.product.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.salesapp.android.R;
import com.salesapp.android.data.model.Product;
import com.salesapp.android.data.preference.PreferenceManager;
import com.salesapp.android.ui.product.ProductViewModel;

import java.util.ArrayList;

/**
 * Fragment for admin product management (CRUD operations)
 */
public class AdminProductManagementFragment extends Fragment implements AdminProductAdapter.AdminProductListener {

    private RecyclerView recyclerViewProducts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewEmpty;
    private FloatingActionButton fabAddProduct;
    private AdminProductAdapter productAdapter;
    private ProductViewModel viewModel;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_product_management, container, false);

        // Initialize views
        recyclerViewProducts = view.findViewById(R.id.recyclerViewAdminProducts);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if user is admin
        preferenceManager = new PreferenceManager(requireContext());
        if (!isAdmin()) {
            // Show unauthorized message and navigate back
            Toast.makeText(requireContext(), "Unauthorized access", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }

        // Setup RecyclerView
        productAdapter = new AdminProductAdapter(requireContext(), new ArrayList<>(), this);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewProducts.setAdapter(productAdapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);

        // Setup FloatingActionButton
        fabAddProduct.setOnClickListener(v -> showAddProductDialog());

        // Initialize ViewModel
        viewModel = new ProductViewModel(preferenceManager.getToken());

        // Observe data changes
        observeViewModel();

        // Load data
        loadProducts();
    }

    private boolean isAdmin() {
        String role = preferenceManager.getRole();
        return role != null && role.equals("ADMIN");
    }

    private void loadProducts() {
        viewModel.loadProducts();
    }

    private void observeViewModel() {
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.setProducts(products);

            if (products.isEmpty()) {
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerViewProducts.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.GONE);
                recyclerViewProducts.setVisibility(View.VISIBLE);
            }

            swipeRefreshLayout.setRefreshing(false);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showAddProductDialog() {
        ProductDialogFragment dialogFragment = ProductDialogFragment.newInstance(null);
        dialogFragment.setProductDialogListener(new ProductDialogFragment.ProductDialogListener() {
            @Override
            public void onProductSaved(Product product) {
                loadProducts();
                Snackbar.make(requireView(), "Product added successfully", Snackbar.LENGTH_SHORT).show();
            }
        });
        dialogFragment.show(getChildFragmentManager(), "add_product");
    }

    private void showEditProductDialog(Product product) {
        ProductDialogFragment dialogFragment = ProductDialogFragment.newInstance(product);
        dialogFragment.setProductDialogListener(new ProductDialogFragment.ProductDialogListener() {
            @Override
            public void onProductSaved(Product updatedProduct) {
                loadProducts();
                Snackbar.make(requireView(), "Product updated successfully", Snackbar.LENGTH_SHORT).show();
            }
        });
        dialogFragment.show(getChildFragmentManager(), "edit_product");
    }

    @Override
    public void onEditClick(Product product) {
        showEditProductDialog(product);
    }

    @Override
    public void onDeleteClick(Product product) {
        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getProductName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the product
                    viewModel.deleteProduct(product.getProductId());
                    Snackbar.make(requireView(), "Product deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                // No real undo functionality without backend support
                                Toast.makeText(requireContext(), "Undo is not supported", Toast.LENGTH_SHORT).show();
                            })
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}