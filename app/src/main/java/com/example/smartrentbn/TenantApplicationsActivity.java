package com.example.smartrentbn;

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

    // list that will store all the applications for this tenant
    private ArrayList<RentalApplication> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_applications);

        // setting the title at the top
        setTitle("My Applications");

        recycler = findViewById(R.id.recyclerTenantApps);
        progressBar = findViewById(R.id.progressBarApps);

        // show applications in a simple vertical list
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TenantApplicationsAdapter(list);
        recycler.setAdapter(adapter);

        // load the tenant’s applications from firebase
        loadMyApplications();
    }

    private void loadMyApplications() {

        // show the loading bar while firebase loads data
        progressBar.setVisibility(View.VISIBLE);

        // if user somehow isn't logged in, we stop and ask them to re-login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // get current user's UID (used to fetch only their applications)
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // read rental applications where tenantUid current user
        FirebaseDatabase.getInstance()
                .getReference("rental_applications")
                .orderByChild("tenantUid")
                .equalTo(uid)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // clear previous list to avoid duplicates
                        list.clear();

                        // if firebase actually has results, loop through them
                        if (snapshot.exists()) {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                RentalApplication app = snap.getValue(RentalApplication.class);
                                if (app != null) {
                                    list.add(app);
                                }
                            }
                        }

                        // update the adapter so the UI refreshes
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // if firebase errors out for some reason
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(TenantApplicationsActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
