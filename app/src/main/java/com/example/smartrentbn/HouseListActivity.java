package com.example.smartrentbn;

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
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_list);

        // linking all the views
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchProperty = findViewById(R.id.search_property);
        filterBudgetInput = findViewById(R.id.filter_budget_input);
        applyFilterButton = findViewById(R.id.apply_filter_button);

        // basic setup for the list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        houseList = new ArrayList<>();
        allHouses = new ArrayList<>();
        adapter = new HouseAdapter(this, houseList);
        recyclerView.setAdapter(adapter);

        // hiding bottom nav by default (only landlord sees it)
        bottomNavigationView.setVisibility(View.GONE);

        // checking the logged in user's role
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference roleRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("role");

            // reading the user's role once when loading page
            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);

                    // if landlord → show bottom nav
                    if (role != null && role.equalsIgnoreCase("landlord")) {
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    }

                    // load the houses after knowing role
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

        // handling the bottom navigation click
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                // taking landlord to add new listing
                startActivity(new Intent(HouseListActivity.this, AddHouseActivity2.class));
                return true;
            }
            return false;
        });

        // search bar actions
        setupSearchView();

        // budget filter button
        applyFilterButton.setOnClickListener(v -> {
            String budgetText = filterBudgetInput.getText().toString().trim();

            // when no budget entered → show everything back
            if (budgetText.isEmpty()) {
                adapter = new HouseAdapter(this, allHouses);
                recyclerView.setAdapter(adapter);
                Toast.makeText(this, "Showing all listings", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // converting user input into a number
                double maxBudget = Double.parseDouble(budgetText);

                // making a list of houses under the given budget
                ArrayList<House> filtered = new ArrayList<>();
                for (House house : allHouses) {
                    try {
                        double housePrice = Double.parseDouble(house.getPrice());
                        if (housePrice <= maxBudget) {
                            filtered.add(house);
                        }
                    } catch (NumberFormatException ignored) {}
                }

                // updating the adapter with filtered results
                adapter = new HouseAdapter(this, filtered);
                recyclerView.setAdapter(adapter);
                Toast.makeText(this, "Filtered: Houses under BND " + maxBudget, Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        // pull-to-refresh reloads data
        swipeRefreshLayout.setOnRefreshListener(this::loadAllHouses);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refreshing the list when returning to this screen
        loadAllHouses();
    }

    private void loadAllHouses() {
        // showing refresh spinner
        swipeRefreshLayout.setRefreshing(true);

        houseList.clear();
        allHouses.clear();

        DatabaseReference housesRef = FirebaseDatabase.getInstance().getReference("houses");

        // loading all houses from Firebase
        housesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // looping through each house in the database
                for (DataSnapshot houseSnapshot : snapshot.getChildren()) {
                    House house = houseSnapshot.getValue(House.class);

                    if (house != null) {
                        // saving the Firebase key as the house ID
                        house.setHouseId(houseSnapshot.getKey());
                        houseList.add(house);
                        allHouses.add(house);
                    }
                }

                // updating the adapter with latest data
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
        // whenever user types something, filter the list live
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

    // filtering houses by name or description
    private void filterHouses(String query) {
        ArrayList<House> filteredList = new ArrayList<>();

        for (House house : allHouses) {
            if (house.getName() != null && house.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(house);
            } else if (house.getDescription() != null && house.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(house);
            }
        }

        // showing filtered results on screen
        adapter = new HouseAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
    }
}
