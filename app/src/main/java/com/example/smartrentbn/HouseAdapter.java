package com.example.smartrentbn;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.HouseViewHolder> {

    private final Context context;
    private final List<House> houses;

    public HouseAdapter(Context context, List<House> houses) {
        this.context = context;
        this.houses = houses != null ? houses : new ArrayList<>();
    }

    @NonNull
    @Override
    public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.house_item, parent, false);
        return new HouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseViewHolder holder, int position) {

        House house = houses.get(position);

        holder.nameTextView.setText(house.getName());
        holder.descriptionTextView.setText(house.getDescription());
        holder.priceTextView.setText("BND $" + house.getPrice());

        // Load image with Glide (NO fallback image)
        if (house.getImageUrl() != null && !house.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(house.getImageUrl())
                    .into(holder.imageView);
        } else {
            // leave blank if no image
            holder.imageView.setImageDrawable(null);
        }

        // Click event → open house detail screen
        holder.seeMoreButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, HouseDetailActivity.class);

            intent.putExtra("houseId", house.getHouseId());
            intent.putExtra("name", house.getName());
            intent.putExtra("description", house.getDescription());
            intent.putExtra("price", house.getPrice());
            intent.putExtra("ownerId", house.getOwnerId());
            intent.putExtra("imageUrl", house.getImageUrl());
            intent.putExtra("details", house.getDetails());
            intent.putExtra("amenities", house.getAmenities());
            intent.putExtra("latitude", house.getLatitude());
            intent.putExtra("longitude", house.getLongitude());
            intent.putExtra("videoUrl", house.getVideoUrl());

            // Send detail images
            if (house.getDetailImages() != null) {
                intent.putStringArrayListExtra(
                        "detailImages",
                        new ArrayList<>(house.getDetailImages())
                );
            }

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return houses.size();
    }

    public static class HouseViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, descriptionTextView, priceTextView;
        ImageView imageView;
        Button seeMoreButton;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.name);
            descriptionTextView = itemView.findViewById(R.id.description);
            priceTextView = itemView.findViewById(R.id.price);
            imageView = itemView.findViewById(R.id.image);
            seeMoreButton = itemView.findViewById(R.id.see_more);
        }
    }
}
