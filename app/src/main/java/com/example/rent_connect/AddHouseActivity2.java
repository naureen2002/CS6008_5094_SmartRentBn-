package com.example.rent_connect;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddHouseActivity2 extends AppCompatActivity {

    private TextInputEditText locationInput, roomsInput, priceInput, descriptionInput, amenitiesInput, emailInput, phoneInput;
    private ImageView imagePreview, idPreviewImage, detailPreviewImage;
    private MaterialButton uploadImageButton, uploadIdButton, uploadDetailImagesButton, submitButton, selectLocationButton;
    private ProgressBar progressBar;
    private TextView selectedAddressText;

    private Uri houseImageUri, idImageUri, cameraImageUri;
    private boolean isPickingId = false;
    private final List<Uri> detailImageUris = new ArrayList<>();

    // Selected map data
    private double selectedLatitude = 0.0, selectedLongitude = 0.0;
    private String selectedAddress = "";

    private static final int MAP_REQUEST_CODE = 2001;

    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://rent-connect-a779d.firebasestorage.app");

    private final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("houses");

    // Single image picker (house or ID)
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri finalUri = null;
                    if (cameraImageUri != null) finalUri = cameraImageUri;
                    if (result.getData() != null && result.getData().getData() != null)
                        finalUri = result.getData().getData();

                    if (finalUri != null) {
                        if (isPickingId) {
                            idImageUri = finalUri;
                            idPreviewImage.setImageURI(idImageUri);
                            Toast.makeText(this, "✅ IC/Passport selected!", Toast.LENGTH_SHORT).show();
                        } else {
                            houseImageUri = finalUri;
                            imagePreview.setImageURI(houseImageUri);
                            Toast.makeText(this, "✅ House image selected!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    // Multiple property images (up to 5)
    private final ActivityResultLauncher<Intent> detailImagesPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    detailImageUris.clear();

                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        if (count > 5) count = 5;
                        for (int i = 0; i < count; i++) {
                            Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) {}
                            detailImageUris.add(uri);
                        }
                    } else if (result.getData().getData() != null) {
                        Uri singleUri = result.getData().getData();
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    singleUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        detailImageUris.add(singleUri);
                    }

                    if (!detailImageUris.isEmpty()) {
                        detailPreviewImage.setImageURI(detailImageUris.get(0));
                        Toast.makeText(this, detailImageUris.size() + " property images selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_house2);

        // Permissions
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_IMAGES
                }, 1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("List Your Property");
        }

        // UI refs
        locationInput = findViewById(R.id.house_location);
        roomsInput = findViewById(R.id.number_of_rooms);
        priceInput = findViewById(R.id.price);
        descriptionInput = findViewById(R.id.description);
        amenitiesInput = findViewById(R.id.amenities);
        emailInput = findViewById(R.id.email);
        phoneInput = findViewById(R.id.phone_number);
        imagePreview = findViewById(R.id.image_preview);
        idPreviewImage = findViewById(R.id.id_preview_image);
        detailPreviewImage = findViewById(R.id.detail_preview_image);
        uploadImageButton = findViewById(R.id.upload_image_button);
        uploadIdButton = findViewById(R.id.upload_id_button);
        uploadDetailImagesButton = findViewById(R.id.upload_detail_images_button);
        submitButton = findViewById(R.id.submit_button);
        selectLocationButton = findViewById(R.id.select_location_button);
        selectedAddressText = findViewById(R.id.selected_address_text);

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);

        // Listeners
        uploadImageButton.setOnClickListener(v -> { isPickingId = false; showImagePickerOptions(); });
        uploadIdButton.setOnClickListener(v -> { isPickingId = true; showImagePickerOptions(); });
        uploadDetailImagesButton.setOnClickListener(v -> openMultiImagePicker());

        // Map picker
        selectLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) uploadListingToFirebase();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0.0);
            selectedLongitude = data.getDoubleExtra("longitude", 0.0);
            selectedAddress = data.getStringExtra("address");
            if (selectedAddressText != null) {
                selectedAddressText.setText("📍 " + selectedAddress);
            }
        }
    }

    private void showImagePickerOptions() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Option");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(pickIntent);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void openCamera() {
        try {
            File imageFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider", imageFile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerLauncher.launch(cameraIntent);
        } catch (IOException e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir("images");
        return File.createTempFile("IMG_" + timeStamp + "_", ".jpg", storageDir);
    }

    // Multi-image picker for up to 5 images
    private void openMultiImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        detailImagesPicker.launch(Intent.createChooser(intent, "Select up to 5 Property Images"));
    }

    private void uploadListingToFirebase() {
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setView(progressBar)
                .setCancelable(false)
                .create();
        loadingDialog.show();

        if (houseImageUri == null || idImageUri == null) {
            loadingDialog.dismiss();
            Toast.makeText(this, "Please upload both house and IC/Passport images", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";
        String houseId = UUID.randomUUID().toString();

        StorageReference houseRef = storage.getReference().child("house_images/" + houseId + "_main.jpg");

        houseRef.putFile(houseImageUri).addOnSuccessListener(task ->
                houseRef.getDownloadUrl().addOnSuccessListener(houseUri -> {

                    StorageReference idRef = storage.getReference().child("id_images/" + houseId + "_id.jpg");
                    idRef.putFile(idImageUri).addOnSuccessListener(idTask ->
                            idRef.getDownloadUrl().addOnSuccessListener(idUri -> {

                                uploadDetailImages(houseId, detailImageUrls -> {

                                    HashMap<String, Object> houseData = new HashMap<>();
                                    houseData.put("name", locationInput.getText().toString().trim());
                                    houseData.put("description", descriptionInput.getText().toString().trim());
                                    houseData.put("price", priceInput.getText().toString().trim());
                                    houseData.put("imageUrl", houseUri.toString());
                                    houseData.put("details", "Contact: " + emailInput.getText().toString().trim());
                                    houseData.put("amenities", amenitiesInput.getText().toString().trim());
                                    houseData.put("bedrooms", Integer.parseInt(roomsInput.getText().toString().trim()));
                                    houseData.put("bathrooms", 1);

                                    // ✅ Save picked coordinates/address (fallback to Brunei center if none)
                                    double lat = (selectedLatitude == 0.0 && selectedLongitude == 0.0) ? 4.9031 : selectedLatitude;
                                    double lng = (selectedLatitude == 0.0 && selectedLongitude == 0.0) ? 114.9398 : selectedLongitude;
                                    houseData.put("latitude", lat);
                                    houseData.put("longitude", lng);
                                    if (selectedAddress != null && !selectedAddress.isEmpty()) {
                                        houseData.put("address", selectedAddress);
                                    }

                                    houseData.put("ownerId", userId);
                                    houseData.put("idImageUrl", idUri.toString());
                                    houseData.put("detailImages", detailImageUrls);

                                    databaseRef.child(houseId).setValue(houseData)
                                            .addOnSuccessListener(aVoid -> {
                                                loadingDialog.dismiss();
                                                Toast.makeText(this, "🏠 Listing uploaded successfully!", Toast.LENGTH_LONG).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                loadingDialog.dismiss();
                                                Toast.makeText(this, "❌ Failed to save data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                });
                            })
                    ).addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "❌ Failed to upload ID image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                })
        ).addOnFailureListener(e -> {
            loadingDialog.dismiss();
            Toast.makeText(this, "❌ Failed to upload house image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void uploadDetailImages(String houseId, OnDetailImagesUploaded listener) {
        if (detailImageUris.isEmpty()) {
            listener.onUploaded(new ArrayList<>());
            return;
        }

        List<String> uploadedUrls = new ArrayList<>();
        for (Uri uri : detailImageUris) {
            StorageReference imgRef = storage.getReference().child("house_images/" + houseId + "/detail_" + System.currentTimeMillis() + ".jpg");
            imgRef.putFile(uri).continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return imgRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    uploadedUrls.add(task.getResult().toString());
                    if (uploadedUrls.size() == detailImageUris.size()) listener.onUploaded(uploadedUrls);
                }
            });
        }
    }

    private interface OnDetailImagesUploaded {
        void onUploaded(List<String> urls);
    }

    private boolean validateInputs() {
        if (locationInput.getText().toString().trim().isEmpty()) {
            locationInput.setError("Location is required");
            return false;
        }
        if (roomsInput.getText().toString().trim().isEmpty()) {
            roomsInput.setError("Number of rooms is required");
            return false;
        }
        if (priceInput.getText().toString().trim().isEmpty()) {
            priceInput.setError("Price is required");
            return false;
        }
        if (descriptionInput.getText().toString().trim().isEmpty()) {
            descriptionInput.setError("Description is required");
            return false;
        }
        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        if (phoneInput.getText().toString().trim().isEmpty()) {
            phoneInput.setError("Phone number is required");
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
