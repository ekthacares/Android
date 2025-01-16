package com.example.ekthacares;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.BloodDonation;

import java.util.List;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    private List<BloodDonation> donationsList;

    public DonationAdapter(List<BloodDonation> donationsList) {
        this.donationsList = donationsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BloodDonation donation = donationsList.get(position);

        // Get formatted last donation date using the method in BloodDonation class
        String lastDonationDate = donation.getFormattedTimestamp();
        holder.lastDonationDateTextView.setText(lastDonationDate != null ? lastDonationDate : "Date Not Available");
        holder.hospitalNameTextView.setText(donation.getHospitalName());

        // Check if recipientId is null, and set default text if it is
        if (donation.getRecipientId() != null) {
            holder.donatedtoTextView.setText(String.valueOf(donation.getRecipientId()));
        } else {
            holder.donatedtoTextView.setText("Login User Added His Own data");
        }

    }

    @Override
    public int getItemCount() {
        return donationsList.size();
    }

        public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView lastDonationDateTextView, hospitalNameTextView, donatedtoTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            lastDonationDateTextView = itemView.findViewById(R.id.tvLastDonationDate);
            hospitalNameTextView = itemView.findViewById(R.id.tvHospitalName);
            donatedtoTextView = itemView.findViewById(R.id.tvRecipientId);
        }
    }
}
