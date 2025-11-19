package com.example.smartrentbn;

import androidx.annotation.NonNull;

public class RentalApplication {

    // storing all the main details for one rental application
    public String applicationId;
    public String tenantUid;
    public String tenantName;
    public String tenantEmail;
    public String tenantPhone;
    public String whatsappLink;
    public String houseName;
    public String message;
    public String icImageUrl;
    public String landlordUid;
    public long timestamp;

    // status fields so landlord and tenant know the result
    public String status;
    public String replyMessage;

    // empty constructor for Firebase
    public RentalApplication() {}

    // main constructor to easily create an application object
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
        this.whatsappLink = whatsappLink;
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
        // just returning a simple readable string for debugging/logging
        return (tenantName != null ? tenantName : "Unknown") +
                " applied for " + (houseName != null ? houseName : "Unknown House");
    }
}
