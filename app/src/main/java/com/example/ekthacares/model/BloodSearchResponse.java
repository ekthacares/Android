package com.example.ekthacares.model;

import java.util.List;

public class BloodSearchResponse {

    private List<User> results;
    private String message;

    // Getter and Setter for results
    public List<User> getResults() {
        return results;
    }

    public void setResults(List<User> results) {
        this.results = results;
    }

    // Getter and Setter for message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

