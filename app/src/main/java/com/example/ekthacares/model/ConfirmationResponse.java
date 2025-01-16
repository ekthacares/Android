package com.example.ekthacares.model;

import com.example.ekthacares.model.Confirmation;
import com.example.ekthacares.model.User;

import java.util.List;

public class ConfirmationResponse {

    private User user;
    private List<Confirmation> confirmations;
    private String message;

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Confirmation> getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(List<Confirmation> confirmations) {
        this.confirmations = confirmations;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ConfirmationResponse{" +
                "user=" + user +
                ", confirmations=" + confirmations +
                ", message='" + message + '\'' +
                '}';
    }
}
