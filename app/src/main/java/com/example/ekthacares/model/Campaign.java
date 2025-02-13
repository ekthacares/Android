package com.example.ekthacares.model;

public class Campaign {
    private String title;
    private String message;

    public Campaign(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
