package com.example.ekthacares.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Campaign {

    @SerializedName("id")
    private Long id;   // ⭐ REQUIRED to save I AM IN

    private String title;
    private String message;

    @SerializedName("campaignDate")
    private String date;

    @SerializedName("campaignTime")
    private String time;

    public Campaign(Long id, String title, String message, String date, String time) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
        this.time = time;
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public String getMessage() { return message; }

    // ⭐ Proper formatted date
    public String getFormattedDate() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);

        try {
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    // ⭐ Proper formatted time
    public String getFormattedTime() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        try {
            Date parsedTime = inputFormat.parse(time);
            return outputFormat.format(parsedTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }
}
