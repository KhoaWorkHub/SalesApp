package com.salesapp.android.data.model;

import java.io.Serializable;

public class Category implements Serializable {
    private Long categoryId;
    private String categoryName;

    // No-arg constructor for Gson
    public Category() {
    }

    // Constructor with all fields
    public Category(Long categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return categoryName;
    }
}