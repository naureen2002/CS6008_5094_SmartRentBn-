package com.example.rent_connect;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.UUID;

public class RentalFormActivity extends AppCompatActivity {

    private TextInputEditText fullNameInput, emailInput, phoneNumberInput, inputWhatsapp;
    private TextInputEditText numberOfOccupantsInput, petTypeInput, numPetsInput, rentalDateInput;
    private RadioGroup petsRadioGroup;
    private MaterialButton applyNowButton, uploadIcButton;
    private ImageView icPreviewImage;
    private Uri selectedFileUri;

    private String houseId;
    private String houseName;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    icPreviewImage.setImageURI(uri);
                    Toast.makeText(this, "IC/Passport selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_form);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Rental Application");

        houseId = getIntent().getStringExtra("houseId");
        houseName = getIntent().getStringExtra("houseName");

        fullNameInput = findViewById(R.id.full_name);
        emailInput = findViewById(R.id.email);
        phoneNumberInput = findViewById(R.id.phone_number);
        inputWhatsapp = findViewById(R.id.inputWhatsapp);
        numberOfOccupantsInput = findViewById(R.id.number_of_occupants);
        petTypeInput = findViewById(R.id.pet_type);
        numPetsInput = findViewById(R.id.num_pets);
        rentalDateInput = findViewById(R.id.rental_date);
        petsRadioGroup = findViewById(R.id.pets_radio_group);
        applyNowButton = findViewById(R.id.apply_now_button);
        uploadIcButton = findViewById(R.id.upload_ic_button);
        icPreviewImage = findViewById(R.id.ic_preview_image);

        rentalDateInput.setOnClickListener(v -> showDatePicker(rentalDateInput));
        uploadIcButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        applyNowButton.setOnClickListener(v -> {
            if (validateForm()) saveApplicationToFirebase();
        });
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(fullNameInput.getText())) return false;
        if (TextUtils.isEmpty(emailInput.getText())) return false;
        if (TextUtils.isEmpty(inputWhatsapp.getText())) {
            inputWhatsapp.setError("WhatsApp link required");
            return false;
        }
        if (selectedFileUri == null) return false;
        return true;
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int y, int m, int d) ->
                target.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveApplicationToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String tenantUid = (user != null) ? user.getUid() : "anonymous";
        String applicationId = UUID.randomUUID().toString();

        RentalApplication app = new RentalApplication();
        app.applicationId = applicationId;
        app.tenantUid = tenantUid;
        app.tenantName = fullNameInput.getText().toString();
        app.tenantEmail = emailInput.getText().toString();
        app.tenantPhone = phoneNumberInput.getText().toString();
        app.houseName = houseName;
        app.message = "New rental application";
        app.timestamp = System.currentTimeMillis();
        app.status = "pending";
        app.replyMessage = "";
        app.whatsappLink = inputWhatsapp.getText().toString().trim();

        // Get landlord UID from house data
        FirebaseDatabase.getInstance("https://rent-connect-a779d-default-rtdb.firebaseio.com/")
                .getReference("houses")
                .child(houseId)
                .get().addOnSuccessListener(snap -> {

                    app.landlordUid = snap.child("ownerId").getValue(String.class);

                    StorageReference ref = FirebaseStorage.getInstance()
                            .getReference("id_images/" + applicationId + ".jpg");

                    ref.putFile(selectedFileUri).addOnSuccessListener(task ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                app.icImageUrl = uri.toString();

                                FirebaseDatabase.getInstance("https://rent-connect-a779d-default-rtdb.firebaseio.com/")
                                        .getReference("rental_applications")
                                        .child(applicationId)
                                        .setValue(app)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Submitted!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                            }));
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
