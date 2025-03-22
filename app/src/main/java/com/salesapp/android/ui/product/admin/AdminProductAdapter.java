package com.salesapp.android.ui.product.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.salesapp.android.R;
import com.salesapp.android.data.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    private List<Product> products;
    private final Context context;
    private final AdminProductListener listener;

    public interface AdminProductListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public AdminProductAdapter(Context context, List<Product> products, AdminProductListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products == null ? 0 : products.size();
    }

    public void setProducts(List<Product> newProducts) {
        if (newProducts == null) {
            newProducts = new ArrayList<>();
        }

        // Use DiffUtil to calculate the difference and dispatch updates
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProductDiffCallback(this.products, newProducts));

        this.products = new ArrayList<>(newProducts);
        diffResult.dispatchUpdatesTo(this);
    }

    class AdminProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewProduct;
        private final TextView textViewProductName;
        private final TextView textViewProductPrice;
        private final TextView textViewProductCategory;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        public AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(Product product) {
            // Set product name
            textViewProductName.setText(product.getProductName());

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

            // Load product image
            if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageURL())
                        .apply(new RequestOptions().centerCrop())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_launcher_background);
            }

            // Set click listeners
            buttonEdit.setOnClickListener(v -> listener.onEditClick(product));
            buttonDelete.setOnClickListener(v -> listener.onDeleteClick(product));
        }
    }

    private static class ProductDiffCallback extends DiffUtil.Callback {
        private final List<Product> oldList;
        private final List<Product> newList;

        public ProductDiffCallback(List<Product> oldList, List<Product> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
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

            return samePrice && sameName;
        }
    }
}