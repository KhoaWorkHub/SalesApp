package com.salesapp.android.ui.product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.salesapp.android.R;
import com.salesapp.android.data.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private final Context context;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(Context context, OnProductClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredProducts.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        this.filteredProducts = new ArrayList<>(products);
        notifyDataSetChanged();
    }

    public void filter(String query, Long categoryId, double minPrice, double maxPrice) {
        filteredProducts.clear();

        for (Product product : products) {
            boolean matchesQuery = query.isEmpty() ||
                    product.getProductName().toLowerCase().contains(query.toLowerCase()) ||
                    (product.getBriefDescription() != null &&
                            product.getBriefDescription().toLowerCase().contains(query.toLowerCase()));

            boolean matchesCategory = categoryId == null || (product.getCategory() != null &&
                    product.getCategory().getCategoryId().equals(categoryId));

            boolean matchesPrice = product.getPrice() >= minPrice && product.getPrice() <= maxPrice;

            if (matchesQuery && matchesCategory && matchesPrice) {
                filteredProducts.add(product);
            }
        }

        notifyDataSetChanged();
    }

    public void sortByPrice(boolean ascending) {
        filteredProducts.sort((p1, p2) -> {
            if (ascending) {
                return Double.compare(p1.getPrice(), p2.getPrice());
            } else {
                return Double.compare(p2.getPrice(), p1.getPrice());
            }
        });
        notifyDataSetChanged();
    }

    public List<Product> getFilteredProducts() {
        return filteredProducts;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewProduct;
        private final TextView textViewProductName;
        private final TextView textViewProductDescription;
        private final TextView textViewProductPrice;
        private final Button buttonAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
        }

        public void bind(Product product) {
            textViewProductName.setText(product.getProductName());
            textViewProductDescription.setText(product.getBriefDescription());
            textViewProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

            // Load image with Glide
            if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageURL())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_launcher_background);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> listener.onProductClick(product));
            buttonAddToCart.setOnClickListener(v -> listener.onAddToCartClick(product));
        }
    }
}