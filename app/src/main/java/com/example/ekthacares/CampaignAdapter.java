package com.example.ekthacares;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.Campaign;

import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {
    private List<Campaign> campaignList;

    public CampaignAdapter(List<Campaign> campaignList) {
        this.campaignList = campaignList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_campaign, parent, false);
        return new ViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Campaign campaign = campaignList.get(position);
//        holder.titleTextView.setText(campaign.getTitle());
//        holder.messageTextView.setText(campaign.getMessage());
//    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int reversePosition = campaignList.size() - 1 - position;
        Campaign campaign = campaignList.get(reversePosition);
     //   holder.titleTextView.setText(campaign.getTitle());
        holder.messageTextView.setText(campaign.getMessage());
        holder.dateTextView.setText((campaign.getFormattedDate() != null ? campaign.getFormattedDate() : "N/A"));
    //    holder.timeTextView.setText("Time: " + (campaign.getFormattedTime() != null ? campaign.getFormattedTime() : "N/A"));
    }


    @Override
    public int getItemCount() {
        return campaignList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, messageTextView, dateTextView, timeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
          //  titleTextView = itemView.findViewById(R.id.textTitle);
            messageTextView = itemView.findViewById(R.id.textMessage);
            dateTextView = itemView.findViewById(R.id.textDate);   // Add this in your layout
          //  timeTextView = itemView.findViewById(R.id.textTime);   // Add this in your layout
        }
    }
}
