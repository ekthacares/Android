package com.example.ekthacares.model;

public class CampaignAttendanceRequest {

    private Long userId;
    private Long campaignId;
    private String message;
    private String date;
    private boolean active;   // <--- NEW FIELD

    public CampaignAttendanceRequest(Long userId, Long campaignId, String message, String date) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.message = message;
        this.date = date;
        this.active = true;   // <--- ALWAYS true when saving
    }

    // Optional: Add getters (Retrofit sometimes requires them)
    public Long getUserId() { return userId; }
    public Long getCampaignId() { return campaignId; }
    public String getMessage() { return message; }
    public String getDate() { return date; }
    public boolean isActive() { return active; }
}
