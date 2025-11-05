package com.example.rent_connect;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapDialogActivity extends AppCompatActivity implements OnMapReadyCallback {

    private double latitude = 0.0;
    private double longitude = 0.0;
    private String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_map);

        // ✅ Transparent background to maintain overlay look
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // ✅ Enable action bar with back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Property Location");
        }

        // ✅ Get location data from HouseDetailActivity
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        address = getIntent().getStringExtra("address");

        // ✅ Load map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_image);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng propertyLocation = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions()
                .position(propertyLocation)
                .title(address != null && !address.isEmpty() ? address : "Property Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(propertyLocation, 15f));

        // ✅ Map UI features
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
    }

    // ✅ Handle back button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
