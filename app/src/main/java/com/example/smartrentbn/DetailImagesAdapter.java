package com.example.smartrentbn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DetailImagesAdapter extends RecyclerView.Adapter<DetailImagesAdapter.DetailImagesViewHolder> {

    private final Context context;
    private final List<String> detailImages;

    public DetailImagesAdapter(Context context, List<String> detailImages) {
        this.context = context;
        this.detailImages = detailImages;
    }

    @NonNull
    @Override
    public DetailImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.detail_image_item, parent, false);
        return new DetailImagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailImagesViewHolder holder, int position) {

        String imagePath = detailImages.get(position);

        if (imagePath != null && !imagePath.isEmpty()) {

            if (imagePath.startsWith("http")) {
                // Load image from Firebase or URL (NO placeholder)
                Glide.with(context)
                        .load(imagePath)
                        .into(holder.detailImageView);

            } else {
                // Load from drawable
                int resId = context.getResources().getIdentifier(
                        imagePath.replace(".webp", "").replace(".png", ""),
                        "drawable",
                        context.getPackageName()
                );

                if (resId != 0) {
                    holder.detailImageView.setImageResource(resId);
                } else {
                    // If nothing matches, clear the image
                    holder.detailImageView.setImageDrawable(null);
                }
            }
        } else {
            // If empty path, show nothing
            holder.detailImageView.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return detailImages != null ? detailImages.size() : 0;
    }

    public static class DetailImagesViewHolder extends RecyclerView.ViewHolder {

        public final ImageView detailImageView;

        public DetailImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            detailImageView = itemView.findViewById(R.id.detail_image);
        }
    }
}
