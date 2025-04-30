package com.example.ekthacares;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.Notification;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Get the message from the notification
        String message = notification.getMessage();

        // Regex to match common blood groups (A+, B-, O+, AB+, etc.)
        String bloodGroupRegex = "(A[+-]?|B[+-]?|O[+-]?|AB[+-]?)";

        // Create a pattern and matcher to find the blood group in the message
        Pattern pattern = Pattern.compile(bloodGroupRegex);
        Matcher matcher = pattern.matcher(message);

        // Check if a blood group is found
        if (matcher.find()) {
            // Extract the blood group from the message
            String bloodGroup = matcher.group();

            // Create a SpannableString to apply color formatting
            SpannableString spannableMessage = new SpannableString(message);

            // Find the start and end positions of the blood group in the message
            int start = message.indexOf(bloodGroup);
            int end = start + bloodGroup.length();

            // Apply red color to the blood group part of the message
            spannableMessage.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the formatted message to the TextView
            holder.tvMessage.setText(spannableMessage);
        } else {
            // If no blood group is found, just set the message normally
            holder.tvMessage.setText(message);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage); // Assuming your TextView has this ID
        }
    }
}
