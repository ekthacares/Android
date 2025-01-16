package com.example.ekthacares;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.Confirmation;
import com.example.ekthacares.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationAdapter extends RecyclerView.Adapter<ConfirmationAdapter.ConfirmationViewHolder> {

    private List<Confirmation> confirmations = new ArrayList<>();  // Ensure the list is never null
    private Context context;
    private String jwtToken;

    // Initialize the adapter with context and confirmation list
    public void setConfirmations(Context context, List<Confirmation> confirmations) {
        this.context = context;  // Ensure that context is passed correctly
        if (confirmations != null) {
            this.confirmations = confirmations;  // Only update if the list is not null
        } else {
            this.confirmations = new ArrayList<>();  // Prevent null
        }

        // Retrieve JWT token from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConfirmationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_confirmation, parent, false);
        return new ConfirmationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfirmationViewHolder holder, int position) {
        Confirmation confirmation = confirmations.get(position);

        // Set confirmation data to the respective views
        holder.hospitalNameTextView.setText("Hospital: " + confirmation.getHospitalName());
        holder.confirmedAtTextView.setText("Confirmed At: " + confirmation.getFormattedConfirmedAt());
        holder.startedAtTextView.setText("Started At: " + confirmation.getFormattedStartedAt());
        holder.stoppedAtTextView.setText("Stopped At: " + confirmation.getFormattedStoppedAt());

        String text = "Donated to: " + confirmation.getLoggedInUserId();
        holder.tvLoggedInUserId.setText(text);
        String userIdString = String.valueOf(confirmation.getLoggedInUserId());

        SpannableString spannableText = new SpannableString(text);
        int startIndex = text.indexOf(userIdString);
        int endIndex = startIndex + userIdString.length();

        spannableText.setSpan(new android.text.style.UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new android.text.style.ForegroundColorSpan(context.getResources().getColor(R.color.user_id_color)),
                startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.tvLoggedInUserId.setText(spannableText);

        // Setup on click listener for logged in user ID TextView (optional functionality)
        holder.tvLoggedInUserId.setClickable(true);
        holder.tvLoggedInUserId.setOnClickListener(v -> {
            Long userId = confirmation.getLoggedInUserId();
            if (userId != null) {
                fetchUserDetails(userId);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Ensure that an empty list returns 0, and not null
        return confirmations != null ? confirmations.size() : 0;
    }

    static class ConfirmationViewHolder extends RecyclerView.ViewHolder {
        TextView hospitalNameTextView;
        TextView confirmedAtTextView;
        TextView startedAtTextView;
        TextView stoppedAtTextView;
        TextView tvLoggedInUserId;

        ConfirmationViewHolder(@NonNull View itemView) {
            super(itemView);
            hospitalNameTextView = itemView.findViewById(R.id.hospitalNameTextView);
            confirmedAtTextView = itemView.findViewById(R.id.confirmedAtTextView);
            startedAtTextView = itemView.findViewById(R.id.startedAtTextView);
            stoppedAtTextView = itemView.findViewById(R.id.stoppedAtTextView);
            tvLoggedInUserId = itemView.findViewById(R.id.tvLoggedInUserId);
        }
    }

    private void fetchUserDetails(Long userId) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            Toast.makeText(context, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User userDetails = response.body();
                    showUserDetailsPopup(userDetails);
                } else {
                    Toast.makeText(context, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("ConfirmationAdapter", "Error fetching user details", t);
                Toast.makeText(context, "An error occurred while fetching user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDetailsPopup(User userDetails) {
        String userInfo = "Name: " + userDetails.getDonorName() + "\n" +
                "Mobile: " + userDetails.getMobile() + "\n" +
                "Email: " + userDetails.getEmailId() + "\n" +
                "Blood Group: " + userDetails.getBloodGroup();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("User Details")
                .setMessage(userInfo)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
