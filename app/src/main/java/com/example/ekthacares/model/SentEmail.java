package com.example.ekthacares.model;

import org.threeten.bp.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SentEmail {
    private Long id;
    private String email;
    private String sentAt; // You may adjust this based on the format returned by your backend
    private String confirmationUrl;
    private Long recipientId;
    private Long loggedInUserId;

    private String hospitalName; // ✅ New field added

    private String bloodgroup; // ✅ New field added

    // Default constructor
    public SentEmail() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }

    public void setConfirmationUrl(String confirmationUrl) {
        this.confirmationUrl = confirmationUrl;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUserId(Long loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public String getHospitalName() { // ✅ Getter for hospitalName
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) { // ✅ Setter for hospitalName
        this.hospitalName = hospitalName;
    }

    public String getBloodGroup() { // ✅ Getter for bloodGroup
        return bloodgroup;
    }

    public void setBloodGroup(String bloodGroup) { // ✅ Setter for bloodGroup
        this.bloodgroup = bloodGroup;
    }

    public String getSentDate() {
        if (sentAt == null || sentAt.isEmpty()) return "Unknown";
        return sentAt.split(" ")[0]; // Extracts "yyyy-MM-dd"
    }

    public String getSentTime() {
        if (sentAt == null || sentAt.isEmpty()) return "Unknown";
        return sentAt.split(" ")[1]; // Extracts "HH:mm:ss.SSSSSS"
    }

    public String getTimeDifference() {
        if (sentAt == null || sentAt.isEmpty()) return "Unknown";

        try {
            // ✅ Correct format for ISO 8601 timestamps returned by your API
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date sentDate = format.parse(sentAt);
            Date currentDate = new Date(); // Current time

            long diffMillis = currentDate.getTime() - sentDate.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + " mins ago";
            } else if (hours < 24) {
                return hours + " hours ago";
            } else {
                return days + " days ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }


    @Override
    public String toString() {
        return "SentEmail{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", sentAt='" + sentAt + '\'' +
                ", confirmationUrl='" + confirmationUrl + '\'' +
                ", recipientId=" + recipientId +
                ", loggedInUserId=" + loggedInUserId +
                ", hospitalName='" + hospitalName + '\'' +
                ", bloodGroup='" + bloodgroup + '\'' + // ✅ Add this line
                '}';
    }
}