package com.example.smartrentbn;

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

        // making the dialog fullscreen so the map fits nicely
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // enabling back button + setting a simple title for the map page
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Property Location");
        }

        // getting the location details that I passed from the house details page
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        address = getIntent().getStringExtra("address");

        // loading the google map from the XML fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_image);

        // once the map is ready, onMapReady() will be called
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // creating location based on the house coordinates
        LatLng propertyLocation = new LatLng(latitude, longitude);

        // placing a marker on the map so user knows where the house is
        googleMap.addMarker(new MarkerOptions()
                .position(propertyLocation)
                .title(address != null && !address.isEmpty() ? address : "Property Location"));

        // zooming in to the house area when the map loads
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(propertyLocation, 15f));

        // giving zoom controls so user can zoom in/out easily
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // close the map when user clicks the back arrow
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
