package com.example.ekthacares.model;

public class Confirmation {

    private Long id;
    private Long recipientId;
    private Long loggedInUserId;
    private String hospitalName;
    private boolean confirmed;
    private boolean empty;
    private boolean completed;
    private boolean started;
    private String formattedConfirmedAt;
    private String formattedStoppedAt;
    private String formattedStartedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public String getFormattedConfirmedAt() {
        return formattedConfirmedAt;
    }

    public void setFormattedConfirmedAt(String formattedConfirmedAt) {
        this.formattedConfirmedAt = formattedConfirmedAt;
    }

    public String getFormattedStoppedAt() {
        return formattedStoppedAt;
    }

    public void setFormattedStoppedAt(String formattedStoppedAt) {
        this.formattedStoppedAt = formattedStoppedAt;
    }

    public String getFormattedStartedAt() {
        return formattedStartedAt;
    }

    public void setFormattedStartedAt(String formattedStartedAt) {
        this.formattedStartedAt = formattedStartedAt;
    }
}
