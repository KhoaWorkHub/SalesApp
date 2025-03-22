package com.salesapp.android.ui.product;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.salesapp.android.R;
import com.salesapp.android.data.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products = new ArrayList<>();
    private final Context context;
    private final OnProductClickListener listener;
    private int lastPosition = -1;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_enhanced, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);

        // Apply animation to item
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> newProducts) {
        if (newProducts == null) {
            newProducts = new ArrayList<>();
        }

        // Use DiffUtil to calculate the difference and dispatch updates
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProductDiffCallback(this.products, newProducts));

        this.products = new ArrayList<>(newProducts);
        diffResult.dispatchUpdatesTo(this);

        // Reset last animated position
        lastPosition = -1;
    }

    private void setAnimation(View viewToAnimate, int position) {
        // Only animate items appearing for the first time
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    // Override for more efficient view recycling
    @Override
    public long getItemId(int position) {
        return products.get(position).getProductId();
    }

    // Clear animation when recycling views
    @Override
    public void onViewDetachedFromWindow(@NonNull ProductViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView imageViewProduct;
        private final TextView textViewProductName;
        private final TextView textViewProductDescription;
        private final TextView textViewProductPrice;
        private final TextView textViewProductCategory;
        private final Button buttonAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
        }

        public void bind(Product product) {
            // Set product name
            textViewProductName.setText(product.getProductName());

            // Set product description
            if (product.getBriefDescription() != null && !product.getBriefDescription().isEmpty()) {
                textViewProductDescription.setText(product.getBriefDescription());
                textViewProductDescription.setVisibility(View.VISIBLE);
            } else {
                textViewProductDescription.setVisibility(View.GONE);
            }

            // Format price with currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            textViewProductPrice.setText(currencyFormat.format(product.getPrice()));

            // Set category if available
            if (product.getCategory() != null && product.getCategory().getCategoryName() != null) {
                textViewProductCategory.setText(product.getCategory().getCategoryName());
                textViewProductCategory.setVisibility(View.VISIBLE);
            } else {
                textViewProductCategory.setVisibility(View.GONE);
            }

            // Load product image with rounded corners
            RequestOptions requestOptions = new RequestOptions()
                    .transforms(new CenterCrop(), new RoundedCorners(16));

            if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageURL())
                        .apply(requestOptions)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageViewProduct);
            } else {
                // Load a placeholder with the same transformations
                Glide.with(context)
                        .load(R.drawable.ic_launcher_background)
                        .apply(requestOptions)
                        .into(imageViewProduct);
            }

            // Set click listeners
            cardView.setOnClickListener(v -> listener.onProductClick(product));
            buttonAddToCart.setOnClickListener(v -> listener.onAddToCartClick(product));
        }
    }

    /**
     * DiffUtil callback to calculate the difference between old and new list
     */
    private static class ProductDiffCallback extends DiffUtil.Callback {
        private final List<Product> oldList;
        private final List<Product> newList;

        public ProductDiffCallback(List<Product> oldList, List<Product> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getProductId().equals(
                    newList.get(newItemPosition).getProductId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Product oldProduct = oldList.get(oldItemPosition);
            Product newProduct = newList.get(newItemPosition);

            // Compare relevant fields
            boolean samePrice = oldProduct.getPrice() == newProduct.getPrice();
            boolean sameName = oldProduct.getProductName().equals(newProduct.getProductName());
            boolean sameDesc = (oldProduct.getBriefDescription() == null && newProduct.getBriefDescription() == null) ||
                    (oldProduct.getBriefDescription() != null &&
                            oldProduct.getBriefDescription().equals(newProduct.getBriefDescription()));
            boolean sameImage = (oldProduct.getImageURL() == null && newProduct.getImageURL() == null) ||
                    (oldProduct.getImageURL() != null &&
                            oldProduct.getImageURL().equals(newProduct.getImageURL()));

            return samePrice && sameName && sameDesc && sameImage;
        }
    }
}