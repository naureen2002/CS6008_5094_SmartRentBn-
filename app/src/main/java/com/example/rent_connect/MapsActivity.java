package com.example.rent_connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rent_connect.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Marker selectedMarker;

    private EditText searchInput;
    private Button searchButton, confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActionBar title/back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pick Property Location");
        }

        // Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // UI overlay
        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        confirmButton = findViewById(R.id.confirm_button);

        searchButton.setOnClickListener(v -> geocodeAndMove(searchInput.getText().toString().trim()));
        confirmButton.setOnClickListener(v -> returnSelectionAndFinish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng bruneiCenter = new LatLng(4.9031, 114.9398);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bruneiCenter, 13f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        Toast.makeText(this, "Tap map to set location. You can also search above.", Toast.LENGTH_LONG).show();

        mMap.setOnMapClickListener(latLng -> {
            placeOrMoveMarker(latLng);
        });
    }

    private void placeOrMoveMarker(LatLng latLng) {
        if (selectedMarker != null) selectedMarker.remove();
        selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
    }

    private void geocodeAndMove(String query) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Type a place to search", Toast.LENGTH_SHORT).show();
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> results = geocoder.getFromLocationName(query, 1);
            if (results != null && !results.isEmpty()) {
                Address a = results.get(0);
                LatLng latLng = new LatLng(a.getLatitude(), a.getLongitude());
                placeOrMoveMarker(latLng);
            } else {
                Toast.makeText(this, "No results. Try a different query.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Geocoder error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void returnSelectionAndFinish() {
        if (selectedMarker == null) {
            Toast.makeText(this, "Please select a location by tapping the map.", Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng pos = selectedMarker.getPosition();
        String address = reverseGeocode(pos.latitude, pos.longitude);

        Intent result = new Intent();
        result.putExtra("latitude", pos.latitude);
        result.putExtra("longitude", pos.longitude);
        result.putExtra("address", address);
        setResult(RESULT_OK, result);
        finish();
    }

    private String reverseGeocode(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) return addresses.get(0).getAddressLine(0);
        } catch (IOException ignored) {}
        return "Unknown Address";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
