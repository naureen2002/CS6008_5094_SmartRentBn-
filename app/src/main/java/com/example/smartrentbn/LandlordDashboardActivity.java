package com.example.smartrentbn;

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

        // setting the action bar title + back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("House Applications");
        }

        // connecting recycler view and adapter
        recyclerView = findViewById(R.id.applicationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter(applicationList);
        recyclerView.setAdapter(adapter);

        // load all rental applications for this landlord
        loadApplications();
    }

    private void loadApplications() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // making sure user is still logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "⚠ Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get landlord UID so we can filter applications
        String landlordUid = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading applications for landlordUid: " + landlordUid);

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("rental_applications");

        // get all applications where "landlordUid" matches current user
        ref.orderByChild("landlordUid").equalTo(landlordUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        applicationList.clear();

                        // looping through each application from Firebase
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            RentalApplication app = ds.getValue(RentalApplication.class);
                            if (app != null) applicationList.add(app);
                        }

                        // refresh the list on screen
                        adapter.notifyDataSetChanged();

                        // showing a message if there are no applications yet
                        if (applicationList.isEmpty()) {
                            Toast.makeText(LandlordDashboardActivity.this, "No applications found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // log & show error if Firebase fails
                        Log.e(TAG, "Firebase error: " + error.getMessage());
                        Toast.makeText(LandlordDashboardActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // back navigation
        finish();
        return true;
    }
}
