package com.example.rent_connect;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class LandlordDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ApplicationAdapter adapter;
    private final List<RentalApplication> applicationList = new ArrayList<>();
    private static final String TAG = "LandlordDashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("House Applications");
        }

        recyclerView = findViewById(R.id.applicationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter(applicationList);
        recyclerView.setAdapter(adapter);

        loadApplications();
    }

    private void loadApplications() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "⚠ Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String landlordUid = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading applications for landlordUid: " + landlordUid);

        DatabaseReference ref = FirebaseDatabase
                .getInstance("https://rent-connect-a779d-default-rtdb.firebaseio.com/")
                .getReference("rental_applications");

        ref.orderByChild("landlordUid").equalTo(landlordUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        applicationList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            RentalApplication app = ds.getValue(RentalApplication.class);
                            if (app != null) applicationList.add(app);
                        }

                        adapter.notifyDataSetChanged();

                        if (applicationList.isEmpty()) {
                            Toast.makeText(LandlordDashboardActivity.this, "No applications found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase error: " + error.getMessage());
                        Toast.makeText(LandlordDashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
