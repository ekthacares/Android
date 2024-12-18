package com.example.ekthacares.model;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

public class BloodDonation {

    private Long id;
    private Long userId;
    private Long recipientId;
    private LocalDateTime lastDonationDate;
    private String hospitalName;

    // Default constructor
    public BloodDonation() {
    }

    // Constructor to initialize the donation object
    public BloodDonation(Long id, Long userId, Long recipientId, LocalDateTime lastDonationDate, String hospitalName) {
        this.id = id;
        this.userId = userId;
        this.recipientId = recipientId;
        this.lastDonationDate = lastDonationDate;
        this.hospitalName = hospitalName;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public LocalDateTime getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDateTime lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    // Method to get formatted donation date using ThreeTenABP
    public String getFormattedTimestamp() {
        if (lastDonationDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return lastDonationDate.format(formatter);
        }
        return null;
    }

    // Optional: Method to check if the donation object is empty
    public boolean isEmpty() {
        return (id == null || userId == null || lastDonationDate == null || hospitalName == null || hospitalName.isEmpty());
    }
}
