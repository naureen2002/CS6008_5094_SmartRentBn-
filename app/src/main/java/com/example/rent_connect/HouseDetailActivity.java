package com.example.rent_connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HouseDetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView nameTextView, descriptionTextView, priceTextView, detailsTextView, amenitiesTextView;
    private RecyclerView detailImagesRecyclerView;
    private Button applyButton;
    private com.google.android.material.floatingactionbutton.FloatingActionButton mapButton;

    private double latitude;
    private double longitude;
    private String address = "";
    private String landlordUid = "";
    private String houseId = "";
    private String houseName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("House Details");
        }

        initializeUI();
        loadIntentData();
        setupButtonListeners();
    }

    private void initializeUI() {
        imageView = findViewById(R.id.image);
        nameTextView = findViewById(R.id.name);
        descriptionTextView = findViewById(R.id.description);
        priceTextView = findViewById(R.id.price);
        detailsTextView = findViewById(R.id.details);
        amenitiesTextView = findViewById(R.id.amenities);
        detailImagesRecyclerView = findViewById(R.id.detail_images_recycler_view);
        applyButton = findViewById(R.id.apply_button);
        mapButton = findViewById(R.id.map_button);
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "No property data found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        houseId = intent.getStringExtra("houseId");
        houseName = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        String details = intent.getStringExtra("details");
        String amenities = intent.getStringExtra("amenities");
        String imageUrl = intent.getStringExtra("imageUrl");
        List<String> detailImages = intent.getStringArrayListExtra("detailImages");
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);
        address = intent.getStringExtra("address");
        landlordUid = intent.getStringExtra("ownerId");

        nameTextView.setText(houseName != null ? houseName : "Unnamed Property");
        descriptionTextView.setText(description != null ? description : "No description provided");
        priceTextView.setText(price != null ? "Rent: " + price : "Price not available");
        detailsTextView.setText(details != null ? details : "No details provided");
        amenitiesTextView.setText(amenities != null ? amenities : "No amenities listed");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.house1)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.house1);
        }

        if (detailImages != null && !detailImages.isEmpty()) {
            detailImagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            detailImagesRecyclerView.setAdapter(new DetailImagesAdapter(this, detailImages));
        }
    }

    private void setupButtonListeners() {

        // 🌍 Open map
        mapButton.setOnClickListener(v -> {
            if (latitude != 0.0 && longitude != 0.0) {
                Intent intent = new Intent(this, MapDialogActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("address", address);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No location data available.", Toast.LENGTH_SHORT).show();
            }
        });

        // 📝 Apply → Rental form
        applyButton.setOnClickListener(v -> {
            if (houseId == null || houseId.isEmpty()) {
                Toast.makeText(this, "Missing house information. Please go back.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, RentalFormActivity.class);
            intent.putExtra("houseId", houseId);
            intent.putExtra("houseName", houseName);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
