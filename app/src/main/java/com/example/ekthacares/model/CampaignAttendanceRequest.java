package com.example.ekthacares.model;

public class CampaignAttendanceRequest {
    private Long userId;
    private Long campaignId;
    private String message;
    private String date;

    public CampaignAttendanceRequest(Long userId, Long campaignId, String message, String date) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.message = message;
        this.date = date;
    }
}
