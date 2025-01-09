package com.example.ekthacares.model;

import java.util.List;

public class ApiResponse {
    private String status;
    private String message;
    private String mobile;
    private List<SentEmail> data;  // Add this field to hold the list of SentEmail

    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public List<SentEmail> getData() { return data; }
    public void setData(List<SentEmail> data) { this.data = data; }
}
