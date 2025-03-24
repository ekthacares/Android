package com.example.ekthacares.model;

import com.google.gson.annotations.SerializedName;

public class Campaign {
    private String title;
    private String message;

    @SerializedName("campaignDate")  // This tells Retrofit to map "campaignDate" to this variable
    private String date;

    @SerializedName("campaignTime")  // This tells Retrofit to map "campaignTime" to this variable
    private String time;

    public Campaign(String title, String message, String date, String time) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.time = time;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}