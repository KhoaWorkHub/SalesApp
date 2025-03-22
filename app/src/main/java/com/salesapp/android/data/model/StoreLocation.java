package com.salesapp.android.data.model;

import java.io.Serializable;

public class StoreLocation implements Serializable {
    private Long locationId;
    private double latitude;
    private double longitude;
    private String address;

    // No-arg constructor for Gson
    public StoreLocation() {
    }

    // Constructor with all fields
    public StoreLocation(Long locationId, double latitude, double longitude, String address) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    // Getters and Setters
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}