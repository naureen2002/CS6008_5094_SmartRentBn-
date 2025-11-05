package com.example.rent_connect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TenantApplicationsAdapter extends RecyclerView.Adapter<TenantApplicationsAdapter.ViewHolder> {

    private final ArrayList<RentalApplication> list;
    private final SimpleDateFormat formatter =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public TenantApplicationsAdapter(ArrayList<RentalApplication> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        RentalApplication app = list.get(pos);

        h.houseName.setText(app.houseName);

        String status = (app.status != null) ? app.status.toLowerCase() : "pending";
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

        // ✅ Only show REPLY from landlord (not initial message)
        if (app.replyMessage != null && !app.replyMessage.isEmpty()) {
            h.reply.setText("Message: " + app.replyMessage);
        } else {
            h.reply.setText("Message: (No reply yet)");
        }

        long timeVal = (app.timestamp > 0) ? app.timestamp : System.currentTimeMillis();
        h.time.setText(formatter.format(new Date(timeVal)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView houseName, status, reply, time;

        public ViewHolder(@NonNull View v) {
            super(v);
            houseName = v.findViewById(R.id.txtHouseName);
            status = v.findViewById(R.id.txtStatus);
            reply = v.findViewById(R.id.txtReply);
            time = v.findViewById(R.id.txtSubmittedAt);
        }
    }
}
