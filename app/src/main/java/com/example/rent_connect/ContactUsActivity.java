package com.example.rent_connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContactUsActivity extends AppCompatActivity {

    private TextInputLayout emailField;
    private TextInputEditText emailInput;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;

    private DatabaseReference contactRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // ✅ Action bar setup
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Contact Us");
        }

        // ✅ Firebase reference
        contactRef = FirebaseDatabase
                .getInstance("https://rent-connect-a779d-default-rtdb.firebaseio.com/")
                .getReference("contact_messages");

        // ✅ Initialize views
        emailField = findViewById(R.id.email_field);
        emailInput = findViewById(R.id.email_input);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // ✅ Send button listener
        sendButton.setOnClickListener(v -> {
            if (validateEmail() && validateMessage()) {
                saveMessageToFirebase();
            }
        });
    }

    private boolean validateEmail() {
        String emailText = emailInput.getText().toString().trim();
        if (emailText.isEmpty()) {
            emailField.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            emailField.setError("Enter a valid email address");
            return false;
        } else {
            emailField.setError(null);
            return true;
        }
    }

    private boolean validateMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            messageInput.setError("Message cannot be empty");
            return false;
        } else {
            messageInput.setError(null);
            return true;
        }
    }

    private void saveMessageToFirebase() {
        String email = emailInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        // Get logged-in user info (if available)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "guest";

        // Unique message ID
        String messageId = UUID.randomUUID().toString();

        // Data map
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("messageId", messageId);
        contactData.put("userId", userId);
        contactData.put("email", email);
        contactData.put("message", message);
        contactData.put("timestamp", System.currentTimeMillis());

        // Save to Firebase
        contactRef.child(messageId)
                .setValue(contactData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Your message was submitted successfully!", Toast.LENGTH_SHORT).show();

                    // ✅ Redirect to homepage after 1 second
                    emailInput.setText("");
                    messageInput.setText("");

                    Intent intent = new Intent(ContactUsActivity.this, HouseListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Close Contact Us activity
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to submit message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
