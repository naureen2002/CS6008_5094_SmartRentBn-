package com.example.smartrentbn;

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

    // all the form input fields
    private TextInputEditText fullNameInput, emailInput, phoneNumberInput, inputWhatsapp;
    private TextInputEditText numberOfOccupantsInput, petTypeInput, numPetsInput, rentalDateInput;
    private RadioGroup petsRadioGroup;
    private MaterialButton applyNowButton, uploadIcButton;
    private ImageView icPreviewImage;
    private Uri selectedFileUri;

    private String houseId;
    private String houseName;

    // picking images from gallery
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    icPreviewImage.setImageURI(uri);   // show preview
                    Toast.makeText(this, "IC/Passport selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_form);

        // enabling back button & page title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Rental Application");

        // getting the house info passed from HouseDetail screen
        houseId = getIntent().getStringExtra("houseId");
        houseName = getIntent().getStringExtra("houseName");

        // connecting all UI fields with their IDs
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

        // open date picker when tapping the date field
        rentalDateInput.setOnClickListener(v -> showDatePicker(rentalDateInput));

        // open gallery to pick IC/passport picture
        uploadIcButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // submit button
        applyNowButton.setOnClickListener(v -> {
            if (validateForm()) saveApplicationToFirebase();
        });
    }

    // simple validation just to make sure user filled basic info
    private boolean validateForm() {
        if (TextUtils.isEmpty(fullNameInput.getText())) return false;
        if (TextUtils.isEmpty(emailInput.getText())) return false;

        // WhatsApp link is required since landlord might contact through it
        if (TextUtils.isEmpty(inputWhatsapp.getText())) {
            inputWhatsapp.setError("WhatsApp link required");
            return false;
        }

        // must upload IC/passport for verification
        if (selectedFileUri == null) return false;

        return true;
    }

    // shows a standard date picker dialog for choosing rental date
    private void showDatePicker(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int y, int m, int d) ->
                target.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // saving everything to Firebase: first upload IC picture, then save the whole form
    private void saveApplicationToFirebase() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String tenantUid = (user != null) ? user.getUid() : "anonymous";

        String applicationId = UUID.randomUUID().toString();

        // creating the object to upload
        RentalApplication app = new RentalApplication();
        app.applicationId = applicationId;
        app.tenantUid = tenantUid;
        app.tenantName = fullNameInput.getText().toString();
        app.tenantEmail = emailInput.getText().toString();
        app.tenantPhone = phoneNumberInput.getText().toString();
        app.houseName = houseName;
        app.message = "New rental application";
        app.timestamp = System.currentTimeMillis();
        app.status = "pending";     // new applications always start as pending
        app.replyMessage = "";      // landlord hasn't replied yet
        app.whatsappLink = inputWhatsapp.getText().toString().trim();

        // fetch the landlord’s user ID from the house info stored in Firebase
        FirebaseDatabase.getInstance()
                .getReference("houses")
                .child(houseId)
                .get().addOnSuccessListener(snap -> {

                    app.landlordUid = snap.child("ownerId").getValue(String.class);

                    // path where the IC/passport image will be uploaded
                    StorageReference ref = FirebaseStorage.getInstance()
                            .getReference("id_images/" + applicationId + ".jpg");

                    // upload IC image first
                    ref.putFile(selectedFileUri).addOnSuccessListener(task ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {

                                app.icImageUrl = uri.toString();

                                // finally save the whole application object into database
                                FirebaseDatabase.getInstance()
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
        // back button handling
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
