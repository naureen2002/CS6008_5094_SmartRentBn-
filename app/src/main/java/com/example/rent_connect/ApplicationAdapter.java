package com.example.rent_connect;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.AppViewHolder> {

    private final List<RentalApplication> list;
    private final SimpleDateFormat fmt =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public ApplicationAdapter(List<RentalApplication> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder h, int pos) {
        RentalApplication app = list.get(pos);

        h.houseName.setText(app.houseName);
        h.tenantName.setText(app.tenantName);
        h.tenantEmail.setText(app.tenantEmail);
        h.tenantPhone.setText(app.tenantPhone);
        h.date.setText(fmt.format(app.timestamp));

        // ✅ Show landlord reply OR default message
        if (app.replyMessage != null && !app.replyMessage.isEmpty()) {
            h.tenantMessage.setText("Message: " + app.replyMessage);
        } else {
            h.tenantMessage.setText("Message: New rental application");
        }

        // ✅ WhatsApp Chat Button
        if (app.whatsappLink != null && !app.whatsappLink.isEmpty()) {
            h.btnWhatsapp.setVisibility(View.VISIBLE);
            h.btnWhatsapp.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(app.whatsappLink));
                h.itemView.getContext().startActivity(intent);
            });
        } else {
            h.btnWhatsapp.setVisibility(View.GONE);
        }

        updateUIState(h, app);

        h.btnApprove.setOnClickListener(v -> {
            String reply = h.landlordMessageInput.getText().toString().trim();
            if (reply.isEmpty()) reply = "I will contact you soon.";
            updateStatus(h, app, "approved", reply);
        });

        h.btnReject.setOnClickListener(v -> {
            String reply = h.landlordMessageInput.getText().toString().trim();
            if (reply.isEmpty()) reply = "Your application was not successful.";
            updateStatus(h, app, "rejected", reply);
        });
    }

    private void updateStatus(AppViewHolder h, RentalApplication app, String status, String reply) {

        FirebaseDatabase.getInstance("https://rent-connect-a779d-default-rtdb.firebaseio.com/")
                .getReference("rental_applications")
                .child(app.applicationId)
                .child("status")
                .setValue(status);

        FirebaseDatabase.getInstance("https://rent-connect-a779d-default-rtdb.firebaseio.com/")
                .getReference("rental_applications")
                .child(app.applicationId)
                .child("replyMessage")
                .setValue(reply)
                .addOnSuccessListener(unused -> {
                    app.status = status;
                    app.replyMessage = reply;
                    updateUIState(h, app);
                    Toast.makeText(h.itemView.getContext(),
                            "Application " + status, Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUIState(AppViewHolder h, RentalApplication app) {
        switch (app.status.toLowerCase()) {
            case "approved":
                h.status.setText("Approved ✅");
                h.status.setTextColor(Color.parseColor("#4CAF50"));
                h.btnApprove.setVisibility(View.GONE);
                h.btnReject.setVisibility(View.GONE);
                h.landlordMessageInput.setEnabled(false);
                break;
            case "rejected":
                h.status.setText("Rejected ❌");
                h.status.setTextColor(Color.parseColor("#F44336"));
                h.btnApprove.setVisibility(View.GONE);
                h.btnReject.setVisibility(View.GONE);
                h.landlordMessageInput.setEnabled(false);
                break;
            default:
                h.status.setText("Pending ⏳");
                h.status.setTextColor(Color.parseColor("#FFC107"));
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView houseName, tenantName, tenantEmail, tenantPhone, tenantMessage, date, status, btnWhatsapp;
        Button btnApprove, btnReject;
        TextInputEditText landlordMessageInput;

        public AppViewHolder(@NonNull View v) {
            super(v);
            houseName = v.findViewById(R.id.houseName);
            tenantName = v.findViewById(R.id.tenantName);
            tenantEmail = v.findViewById(R.id.tenantEmail);
            tenantPhone = v.findViewById(R.id.tenantPhone);
            tenantMessage = v.findViewById(R.id.tenantMessage);
            date = v.findViewById(R.id.applicationDate);
            status = v.findViewById(R.id.textStatusValue);
            btnWhatsapp = v.findViewById(R.id.btnWhatsapp);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
            landlordMessageInput = v.findViewById(R.id.inputLandlordMessage);
        }
    }
}
