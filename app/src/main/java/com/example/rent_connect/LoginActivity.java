package com.example.rent_connect;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginUsername, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText, forgotPasswordText;
    private FirebaseAuth auth;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        passwordToggle = findViewById(R.id.password_toggle);

        loginButton.setOnClickListener(view -> {
            if (!validateEmail() | !validatePassword()) return;
            loginUser();
        });

        signupRedirectText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        forgotPasswordText.setOnClickListener(view -> {
            String email = loginUsername.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.eye_closed);
            } else {
                loginPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                passwordToggle.setImageResource(R.drawable.eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            loginPassword.setSelection(loginPassword.length());
        });
    }

    private boolean validateEmail() {
        String val = loginUsername.getText().toString().trim();
        if (val.isEmpty()) {
            loginUsername.setError("Email cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
            loginUsername.setError("Invalid email format");
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        String val = loginPassword.getText().toString().trim();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        }
        return true;
    }

    private void loginUser() {
        String userEmail = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            if (!user.isEmailVerified()) {
                                Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                auth.signOut();
                                return;
                            }

                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(user.getUid())
                                    .child("role");

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String role = snapshot.getValue(String.class);
                                    Intent next = new Intent(LoginActivity.this, HouseListActivity.class);
                                    next.putExtra("role", role != null ? role : "tenant");
                                    startActivity(next);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
