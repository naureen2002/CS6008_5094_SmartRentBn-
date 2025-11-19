package com.example.smartrentbn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    // all input fields for the sign up page
    private EditText signupName, signupUsername, signupEmail, signupPassword;
    private TextView loginRedirectText;
    private Button signupButton;
    private RadioGroup roleGroup;

    // firebase stuff for authentication & database
    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");

        // connecting UI components to variables
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);
        roleGroup = findViewById(R.id.roleGroup);

        // when user clicks the sign-up button
        signupButton.setOnClickListener(view -> {
            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String username = signupUsername.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            // basic input checking
            if (!validateInputs(name, email, username, password)) return;

            // make sure user selects either tenant or landlord
            int selectedId = roleGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(SignupActivity.this, "Please select Tenant or Landlord", Toast.LENGTH_SHORT).show();
                return;
            }

            // get the selected role text
            RadioButton selectedRoleButton = findViewById(selectedId);
            String role = selectedRoleButton.getText().toString().toLowerCase();

            // create a new firebase account
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            if (firebaseUser != null) {

                                // get unique ID from firebase
                                String uid = firebaseUser.getUid();

                                // store basic user details in database
                                HelperClass helperClass = new HelperClass(name, email, username);
                                reference.child(uid).setValue(helperClass).addOnCompleteListener(databaseTask -> {
                                    if (databaseTask.isSuccessful()) {

                                        // save the role (tenant/landlord) under this user
                                        reference.child(uid).child("role").setValue(role);

                                        // send email verification so account is valid
                                        firebaseUser.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this, "Verification email sent! Check your inbox.", Toast.LENGTH_LONG).show();

                                                // log user out after signing up
                                                auth.signOut();
                                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                finish();

                                            } else {
                                                Toast.makeText(SignupActivity.this, "Failed to send verification: " + verifyTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Database error: " + databaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            // if sign up fails for any reason (duplicate email, weak password, etc.)
                            Toast.makeText(SignupActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // go to login page if user already has an account
        loginRedirectText.setOnClickListener(view ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    // simple input validation for sign up fields
    private boolean validateInputs(String name, String email, String username, String password) {
        if (name.isEmpty()) {
            signupName.setError("Name cannot be empty");
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Invalid email format");
            return false;
        }
        if (username.isEmpty()) {
            signupUsername.setError("Username cannot be empty");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }
}
