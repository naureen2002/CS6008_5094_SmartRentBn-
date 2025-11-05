package com.example.rent_connect;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TenantApplicationsActivity extends BaseActivity {

    private RecyclerView recycler;
    private ProgressBar progressBar;
    private TenantApplicationsAdapter adapter;
    private ArrayList<RentalApplication> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_applications);

        setTitle("My Applications");

        recycler = findViewById(R.id.recyclerTenantApps);
        progressBar = findViewById(R.id.progressBarApps);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TenantApplicationsAdapter(list);
        recycler.setAdapter(adapter);

        loadMyApplications();
    }

    private void loadMyApplications() {
        progressBar.setVisibility(View.VISIBLE);

        // ✅ FIX: Null check before using UID
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://rent-connect-a779d-default-rtdb.firebaseio.com/")
                .getReference("rental_applications")
                .orderByChild("tenantUid")
                .equalTo(uid)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                RentalApplication app = snap.getValue(RentalApplication.class);
                                if (app != null) {
                                    list.add(app);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TenantApplicationsActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
