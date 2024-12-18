package com.example.ekthacares.model;

import java.util.List;

public class DonationResponse {
    private List<BloodDonation> donations;

    public List<BloodDonation> getDonations() {
        return donations;
    }

    public void setDonations(List<BloodDonation> donations) {
        this.donations = donations;
    }

    public boolean isEmpty() {
        return donations == null || donations.isEmpty();
    }

}
