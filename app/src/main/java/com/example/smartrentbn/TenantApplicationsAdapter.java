package com.example.smartrentbn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TenantApplicationsAdapter extends RecyclerView.Adapter<TenantApplicationsAdapter.ViewHolder> {

    private final ArrayList<RentalApplication> list;

    // simple formatter just to show date nicely on screen
    private final SimpleDateFormat formatter =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public TenantApplicationsAdapter(ArrayList<RentalApplication> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // connect the XML layout with the adapter item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant_application, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        // get the application at this position
        RentalApplication app = list.get(pos);

        // show the house name for this application
        h.houseName.setText(app.houseName);

        // make sure status is readable and doesn’t crash the app
        String status = (app.status != null) ? app.status.toLowerCase() : "pending";

        // set the status text so the user knows what’s going on
        switch (status) {
            case "approved":
                h.status.setText("Approved ✅");
                break;
            case "rejected":
                h.status.setText("Rejected ❌");
                break;
            default:
                h.status.setText("Pending ⏳");
                break;
        }

        // show the landlord’s reply (if they replied)
        if (app.replyMessage != null && !app.replyMessage.isEmpty()) {
            h.reply.setText("Message: " + app.replyMessage);
        } else {
            h.reply.setText("Message: (No reply yet)");
        }

        // format the time when the user applied
        long timeVal = (app.timestamp > 0) ? app.timestamp : System.currentTimeMillis();
        h.time.setText(formatter.format(new Date(timeVal)));

        // only allow cancelling if it's still pending
        if (status.equals("pending")) {
            h.btnCancel.setVisibility(View.VISIBLE);
        } else {
            h.btnCancel.setVisibility(View.GONE);
        }

        // cancel button action to delete the record from Firebase
        h.btnCancel.setOnClickListener(v -> {

            FirebaseDatabase.getInstance()
                    .getReference("rental_applications")
                    .child(app.applicationId)
                    .removeValue()
                    .addOnSuccessListener(a -> {

                        Toast.makeText(h.itemView.getContext(),
                                "Application cancelled successfully",
                                Toast.LENGTH_SHORT).show();

                        // remove the cancelled item from the list and update UI
                        list.remove(app);
                        notifyDataSetChanged();

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(h.itemView.getContext(),
                                    "Failed to cancel application",
                                    Toast.LENGTH_SHORT).show());
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // holder class to store the views for each item
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView houseName, status, reply, time;
        Button btnCancel;

        public ViewHolder(@NonNull View v) {
            super(v);

            houseName = v.findViewById(R.id.txtHouseName);
            status = v.findViewById(R.id.txtStatus);
            reply = v.findViewById(R.id.txtReply);
            time = v.findViewById(R.id.txtSubmittedAt);
            btnCancel = v.findViewById(R.id.btnCancelApplication);
        }
    }
}
