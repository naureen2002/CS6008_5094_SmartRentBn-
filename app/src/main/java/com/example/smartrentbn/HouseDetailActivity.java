package com.example.smartrentbn;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class HouseDetailActivity extends AppCompatActivity {

    // all the views I need to show the house details
    private ImageView imageView;
    private TextView nameTextView, descriptionTextView, priceTextView, detailsTextView,
            amenitiesTextView, avgRatingTextView;
    private RecyclerView detailImagesRecyclerView;
    private Button applyButton, submitRatingButton;
    private FloatingActionButton mapButton;
    private RatingBar ratingBar;

    // data passed from previous screen
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

        // setting up the action bar with back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("House Details");
        }

        // just separating UI initialization for cleanliness
        initializeUI();

        // getting the house info from the adapter
        loadIntentData();

        // setting all the click actions
        setupButtonListeners();

        // showing average rating as soon as the page opens
        loadAverageRating();
    }

    private void initializeUI() {
        // linking all the views with XML ids
        imageView = findViewById(R.id.image);
        nameTextView = findViewById(R.id.name);
        descriptionTextView = findViewById(R.id.description);
        priceTextView = findViewById(R.id.price);
        detailsTextView = findViewById(R.id.details);
        amenitiesTextView = findViewById(R.id.amenities);
        detailImagesRecyclerView = findViewById(R.id.detail_images_recycler_view);

        avgRatingTextView = findViewById(R.id.avgRatingText);

        applyButton = findViewById(R.id.apply_button);
        mapButton = findViewById(R.id.map_button);

        ratingBar = findViewById(R.id.ratingBar);
        submitRatingButton = findViewById(R.id.submitRatingBtn);
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "No property data found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // reading all the fields sent via intent
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

        // setting UI values
        nameTextView.setText(houseName != null ? houseName : "Unnamed Property");
        descriptionTextView.setText(description != null ? description : "No description provided");
        priceTextView.setText(price != null ? "Rent: " + price : "Price not available");
        detailsTextView.setText(details != null ? details : "No details provided");
        amenitiesTextView.setText(amenities != null ? amenities : "No amenities listed");

        // loading main image with Glide (no fallback image)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView);
        } else {
            // if no image, leave blank
            imageView.setImageDrawable(null);
        }

        // showing extra property images
        if (detailImages != null && !detailImages.isEmpty()) {
            detailImagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            detailImagesRecyclerView.setAdapter(new DetailImagesAdapter(this, detailImages));
        }
    }

    private void setupButtonListeners() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // checking if this user already rated this house
        FirebaseDatabase.getInstance().getReference("ratings")
                .child(houseId)
                .child(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        float previousRating = snapshot.getValue(Float.class);
                        ratingBar.setRating(previousRating);
                        ratingBar.setIsIndicator(true);
                        submitRatingButton.setEnabled(false);
                        submitRatingButton.setText("Rating Submitted");
                    }
                });

        // map button to open the location dialog
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

        // apply button to go to rental form
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

        // submit rating
        submitRatingButton.setOnClickListener(v -> {
            float ratingValue = ratingBar.getRating();

            if (ratingValue == 0f) {
                Toast.makeText(this, "Please select a rating first.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseDatabase.getInstance().getReference("ratings")
                    .child(houseId)
                    .child(userId)
                    .setValue(ratingValue)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Rating submitted!", Toast.LENGTH_SHORT).show();
                        ratingBar.setIsIndicator(true);
                        submitRatingButton.setEnabled(false);
                        submitRatingButton.setText("Rating Submitted");
                        loadAverageRating();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to submit rating.", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadAverageRating() {
        FirebaseDatabase.getInstance().getReference("ratings")
                .child(houseId)
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            avgRatingTextView.setText("Average Rating: No ratings yet");
                            return;
                        }

                        float total = 0;
                        int count = 0;

                        for (com.google.firebase.database.DataSnapshot ratingSnap : snapshot.getChildren()) {
                            Float value = ratingSnap.getValue(Float.class);
                            if (value != null) {
                                total += value;
                                count++;
                            }
                        }

                        float average = total / count;

                        avgRatingTextView.setText(
                                String.format("Average Rating: %.1f (%d reviews)", average, count)
                        );
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) { }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // back button on top
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
