package com.example.rent_connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BaseActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private volatile boolean isLandlord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // ✅ Load role early to prevent crash
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(current.getUid())
                    .child("role");

            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    isLandlord = role != null && role.equalsIgnoreCase("landlord");
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem addHouseItem = menu.findItem(R.id.action_add_house);

        if (addHouseItem != null) addHouseItem.setVisible(isLandlord);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_contact_us) {
            startActivity(new Intent(this, ContactUsActivity.class));
            return true;
        }

        if (id == R.id.action_about_us) {
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;
        }

        if (id == R.id.action_add_house) {
            startActivity(new Intent(this, AddHouseActivity2.class));
            return true;
        }

        if (id == R.id.action_applications) {

            // ✅ Prevent crash if Firebase is null
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (isLandlord) {
                startActivity(new Intent(this, LandlordDashboardActivity.class));
            } else {
                startActivity(new Intent(this, TenantApplicationsActivity.class));
            }
            return true;
        }

        if (id == R.id.action_logout) {
            auth.signOut();
            Intent i = new Intent(this, PreLoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
