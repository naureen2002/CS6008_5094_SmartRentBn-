package com.example.rent_connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HouseListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private HouseAdapter adapter;
    private ArrayList<House> houseList;
    private ArrayList<House> allHouses;
    private BottomNavigationView bottomNavigationView;
    private SearchView searchProperty;
    private EditText filterBudgetInput;
    private Button applyFilterButton;
    private SwipeRefreshLayout swipeRefreshLayout; // ✅ Added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_list);

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout); // ✅ Connect XML
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchProperty = findViewById(R.id.search_property);
        filterBudgetInput = findViewById(R.id.filter_budget_input);
        applyFilterButton = findViewById(R.id.apply_filter_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        houseList = new ArrayList<>();
        allHouses = new ArrayList<>();
        adapter = new HouseAdapter(this, houseList);
        recyclerView.setAdapter(adapter);

        bottomNavigationView.setVisibility(View.GONE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("role");

            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    if (role != null && role.equalsIgnoreCase("landlord")) {
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    }
                    loadAllHouses();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    loadAllHouses();
                }
            });
        } else {
            loadAllHouses();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(HouseListActivity.this, AddHouseActivity2.class));
                return true;
            }
            return false;
        });

        setupSearchView();

        applyFilterButton.setOnClickListener(v -> {
            String budgetText = filterBudgetInput.getText().toString().trim();
            if (budgetText.isEmpty()) {
                adapter = new HouseAdapter(this, allHouses);
                recyclerView.setAdapter(adapter);
                Toast.makeText(this, "Showing all listings", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double maxBudget = Double.parseDouble(budgetText);
                ArrayList<House> filtered = new ArrayList<>();
                for (House house : allHouses) {
                    try {
                        double housePrice = Double.parseDouble(house.getPrice());
                        if (housePrice <= maxBudget) {
                            filtered.add(house);
                        }
                    } catch (NumberFormatException ignored) {}
                }

                adapter = new HouseAdapter(this, filtered);
                recyclerView.setAdapter(adapter);
                Toast.makeText(this, "Filtered: Houses under BND " + maxBudget, Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Pull-to-refresh reloads list
        swipeRefreshLayout.setOnRefreshListener(this::loadAllHouses);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllHouses(); // ✅ Refresh upon returning
    }

    private void loadAllHouses() {
        swipeRefreshLayout.setRefreshing(true); // ✅ show refresh

        houseList.clear();
        allHouses.clear();

        DatabaseReference housesRef = FirebaseDatabase.getInstance().getReference("houses");
        housesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot houseSnapshot : snapshot.getChildren()) {
                    House house = houseSnapshot.getValue(House.class);
                    if (house != null) {
                        house.setHouseId(houseSnapshot.getKey());
                        houseList.add(house);
                        allHouses.add(house);
                    }
                }

                adapter = new HouseAdapter(HouseListActivity.this, houseList);
                recyclerView.setAdapter(adapter);

                swipeRefreshLayout.setRefreshing(false);

                if (houseList.isEmpty()) {
                    Toast.makeText(HouseListActivity.this, "No houses found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(HouseListActivity.this, "Failed to load houses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchView() {
        searchProperty.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterHouses(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterHouses(newText);
                return true;
            }
        });
    }

    private void filterHouses(String query) {
        ArrayList<House> filteredList = new ArrayList<>();
        for (House house : allHouses) {
            if (house.getName() != null && house.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(house);
            } else if (house.getDescription() != null && house.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(house);
            }
        }
        adapter = new HouseAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
    }
}
