package com.example.rent_connect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PreLoginActivity extends AppCompatActivity {
    private Button loginButtonPre, signupButtonPre;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_login);

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        // ✅ Only redirect if Firebase Auth user is fully authenticated
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent houseListIntent = new Intent(PreLoginActivity.this, HouseListActivity.class);
            houseListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(houseListIntent);
            return;
        }

        loginButtonPre = findViewById(R.id.login_button_pre);
        signupButtonPre = findViewById(R.id.signup_button_pre);

        loginButtonPre.setOnClickListener(v -> {
            Intent loginIntent = new Intent(PreLoginActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        });

        signupButtonPre.setOnClickListener(v -> {
            Intent signupIntent = new Intent(PreLoginActivity.this, SignupActivity.class);
            startActivity(signupIntent);
        });
    }
}
