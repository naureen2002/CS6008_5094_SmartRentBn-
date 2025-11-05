package com.example.rent_connect;

import androidx.annotation.NonNull;

public class RentalApplication {
    public String applicationId;
    public String tenantUid;
    public String tenantName;
    public String tenantEmail;
    public String tenantPhone;
    public String whatsappLink; // ✅ NEW FIELD
    public String houseName;
    public String message;
    public String icImageUrl;
    public String landlordUid;
    public long timestamp;

    // ✅ Status and reply
    public String status; // pending | approved | rejected
    public String replyMessage;

    public RentalApplication() {}

    public RentalApplication(String applicationId, String tenantUid, String tenantName,
                             String tenantEmail, String tenantPhone, String whatsappLink,
                             String houseName, String message, String icImageUrl,
                             String landlordUid, long timestamp,
                             String status, String replyMessage) {

        this.applicationId = applicationId;
        this.tenantUid = tenantUid;
        this.tenantName = tenantName;
        this.tenantEmail = tenantEmail;
        this.tenantPhone = tenantPhone;
        this.whatsappLink = whatsappLink; // ✅ Set properly
        this.houseName = houseName;
        this.message = message;
        this.icImageUrl = icImageUrl;
        this.landlordUid = landlordUid;
        this.timestamp = timestamp;
        this.status = status;
        this.replyMessage = replyMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return (tenantName != null ? tenantName : "Unknown") +
                " applied for " + (houseName != null ? houseName : "Unknown House");
    }
}
