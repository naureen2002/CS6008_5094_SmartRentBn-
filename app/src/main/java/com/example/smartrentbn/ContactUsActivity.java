package com.example.smartrentbn;

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
        // loading the contact us page layout
        setContentView(R.layout.activity_contact_us);

        // setting up the top bar (title + back button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Contact Us");
        }

        // firebase reference where I will save all contact messages
        contactRef = FirebaseDatabase
                .getInstance()
                .getReference("contact_messages");

        // connecting the UI elements from xml
        emailField = findViewById(R.id.email_field);
        emailInput = findViewById(R.id.email_input);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        // when user clicks the send button
        sendButton.setOnClickListener(v -> {
            // make sure both email + message are valid before saving
            if (validateEmail() && validateMessage()) {
                saveMessageToFirebase();
            }
        });
    }

    private boolean validateEmail() {
        // checking if the email format is correct
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
        // making sure the message box is not empty
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
        // getting the email + message user typed
        String email = emailInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        // if user is logged in, save their uid, otherwise mark as guest
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "guest";

        // create a random id for each message
        String messageId = UUID.randomUUID().toString();

        // preparing everything to save into firebase
        Map<String, Object> contactData = new HashMap<>();
        contactData.put("messageId", messageId);
        contactData.put("userId", userId);
        contactData.put("email", email);
        contactData.put("message", message);
        contactData.put("timestamp", System.currentTimeMillis());

        // pushing the message inside firebase
        contactRef.child(messageId)
                .setValue(contactData)
                .addOnSuccessListener(aVoid -> {
                    // showing a small success message
                    Toast.makeText(this, "Your message was submitted successfully!", Toast.LENGTH_SHORT).show();

                    // clearing the fields after sending
                    emailInput.setText("");
                    messageInput.setText("");

                    // sending user back to home page
                    Intent intent = new Intent(ContactUsActivity.this, HouseListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to submit message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // handling the back button on top
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
