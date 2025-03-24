package com.salesapp.android.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.salesapp.android.R;
import com.salesapp.android.data.model.StoreLocation;
import com.salesapp.android.data.repository.LocationRepository;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {
    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private List<StoreLocation> storeLocations = new ArrayList<>();
    private StoreLocation selectedStore = null;

    // UI elements
    private ProgressBar progressBar;
    private MaterialCardView cardStoreDetails;
    private TextView textViewStoreName;
    private TextView textViewStoreAddress;
    private TextView textViewStoreHours;
    private TextView textViewStorePhone;
    private Button buttonGetDirections;
    private FloatingActionButton fabMyLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Important! Setup OSMDroid configuration before inflating the view
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(requireActivity().getPackageName());

        View view = inflater.inflate(R.layout.fragment_map_osm, container, false);

        // Initialize UI elements
        mapView = view.findViewById(R.id.mapView);
        progressBar = view.findViewById(R.id.progressBar);
        cardStoreDetails = view.findViewById(R.id.cardStoreDetails);
        textViewStoreName = view.findViewById(R.id.textViewStoreName);
        textViewStoreAddress = view.findViewById(R.id.textViewStoreAddress);
        textViewStoreHours = view.findViewById(R.id.textViewStoreHours);
        textViewStorePhone = view.findViewById(R.id.textViewStorePhone);
        buttonGetDirections = view.findViewById(R.id.buttonGetDirections);
        fabMyLocation = view.findViewById(R.id.fabMyLocation);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the map
        setupMap();

        // Set click listener for directions button
        buttonGetDirections.setOnClickListener(v -> {
            if (selectedStore != null) {
                openDirectionsToStore(selectedStore);
            }
        });

        // My location button click listener
        fabMyLocation.setOnClickListener(v -> {
            if (hasLocationPermission()) {
                zoomToCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });

        // Load store locations
        loadStoreLocations();
    }

    private void setupMap() {
        // Basic map setup
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(12.0);

        // Default location (will be updated with real locations)
        GeoPoint startPoint = new GeoPoint(10.82, 106.8);
        mapController.setCenter(startPoint);

        // Add location overlay if permission granted
        if (hasLocationPermission()) {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);
        mapView.invalidate();
    }

    private void zoomToCurrentLocation() {
        if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
            mapController.animateTo(locationOverlay.getMyLocation());
            mapController.setZoom(17.0);
        } else {
            Toast.makeText(requireContext(), "Location not available yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStoreLocations() {
        showLoading(true);

        LocationRepository locationRepository = new LocationRepository();
        locationRepository.getAllStoreLocations(new LocationRepository.LocationCallback<List<StoreLocation>>() {
            @Override
            public void onSuccess(List<StoreLocation> result) {
                storeLocations = result;
                showLoading(false);
                addStoreMarkers();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(requireContext(), "Error loading store locations: " + message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading store locations: " + message);
            }
        });
    }

    private void addStoreMarkers() {
        if (storeLocations.isEmpty()) {
            return;
        }

        // Clear existing markers
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

        // List to calculate bounds
        List<GeoPoint> points = new ArrayList<>();

        for (StoreLocation store : storeLocations) {
            GeoPoint point = new GeoPoint(store.getLatitude(), store.getLongitude());
            points.add(point);

            // Create marker
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(store.getName());
            marker.setSnippet(store.getAddress());

            // Store the store location object with the marker
            marker.setRelatedObject(store);

            // Set marker click listener
            marker.setOnMarkerClickListener((marker1, mapView) -> {
                Object relatedObject = marker1.getRelatedObject();
                if (relatedObject instanceof StoreLocation) {
                    selectedStore = (StoreLocation) relatedObject;
                    showStoreDetails(selectedStore);
                }
                return true;
            });

            mapView.getOverlays().add(marker);
        }

        // Zoom to show all markers
        if (!points.isEmpty()) {
            // Find center point of all locations
            double avgLat = points.stream().mapToDouble(GeoPoint::getLatitude).average().orElse(0);
            double avgLon = points.stream().mapToDouble(GeoPoint::getLongitude).average().orElse(0);

            // Set center and zoom to show all
            mapController.setCenter(new GeoPoint(avgLat, avgLon));
            mapController.setZoom(13.0);
        }

        mapView.invalidate();
    }

    private void showStoreDetails(StoreLocation store) {
        textViewStoreName.setText(store.getName());
        textViewStoreAddress.setText(store.getAddress());
        textViewStoreHours.setText(store.getOpeningHours());
        textViewStorePhone.setText(store.getPhone());

        // Show the card
        cardStoreDetails.setVisibility(View.VISIBLE);
    }

    private void openDirectionsToStore(StoreLocation store) {
        // Create a Uri for map directions
        Uri gmmIntentUri = Uri.parse("geo:" +
                store.getLatitude() + "," + store.getLongitude() +
                "?q=" + Uri.encode(store.getName() + ", " + store.getAddress()));

        // Create an Intent to launch any maps app
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Try to start an activity to handle the intent
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to browser if no map app is available
            Uri browserUri = Uri.parse("https://www.openstreetmap.org/?mlat=" +
                    store.getLatitude() + "&mlon=" + store.getLongitude() +
                    "&zoom=16");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
            startActivity(browserIntent);
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                zoomToCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}