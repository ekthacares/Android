package com.example.ekthacares.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Campaign {
    private String title;
    private String message;

    @SerializedName("campaignDate")
    private String date;  // Original date from API (assumed format: yyyy-MM-dd)

    @SerializedName("campaignTime")
    private String time;  // Original time from API (assumed format: HH:mm:ss)

    public Campaign(String title, String message, String date, String time) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.time = time;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }

    // Convert date format to "dd MMMM yyyy" (e.g., "25 March 2025")
    public String getFormattedDate() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);

        try {
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date; // Return original if parsing fails
        }
    }

    // Convert time format to "hh:mm a" (e.g., "10:30 AM")
    public String getFormattedTime() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        try {
            Date parsedTime = inputFormat.parse(time);
            return outputFormat.format(parsedTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return time; // Return original if parsing fails
        }
    }
}
