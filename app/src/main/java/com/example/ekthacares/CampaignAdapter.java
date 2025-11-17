package com.example.ekthacares;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.Campaign;
import com.example.ekthacares.model.CampaignAttendanceRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {

    private final List<Campaign> campaignList;
    private final Context context;
    private final long userId;

    public CampaignAdapter(Context context, List<Campaign> campaignList) {
        this.context = context;
        this.campaignList = campaignList;

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        this.userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_campaign, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int reversePosition = campaignList.size() - 1 - position;
        Campaign campaign = campaignList.get(reversePosition);

        holder.dateTextView.setText(campaign.getFormattedDate());

        // View Location popup
        holder.viewLocationTextView.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Location Details")
                    .setMessage(campaign.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        });

        // ------- HANDLE CHECK & UNCHECK -------
        holder.checkboxConsent.setOnCheckedChangeListener(null);
        loadAttendanceState(holder, campaign.getId());

    }

    @Override
    public int getItemCount() {
        return campaignList.size();
    }

    // ---------- LOAD IF USER ALREADY RESPONDED ----------
    private void loadAttendanceState(ViewHolder holder, Long campaignId) {

        holder.checkboxConsent.setOnCheckedChangeListener(null); // Prevents old listener firing

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        apiService.checkAttendance(userId, campaignId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {

                boolean isChecked = response.body() != null && response.body();

                // Step 1: Set checkbox state
                holder.checkboxConsent.setChecked(isChecked);

                // Step 2: Add listener AFTER state is set
                holder.checkboxConsent.setOnCheckedChangeListener((buttonView, checked) -> {

                    if (checked) {
                        saveConsentToServer(campaignId);
                    } else {
                        removeConsentFromServer(campaignId);
                    }
                });
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                holder.checkboxConsent.setChecked(false);
            }
        });
    }

    // ---------- SAVE CONSENT ----------
    private void saveConsentToServer(Long campaignId) {

        String message = "I AM IN";
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        CampaignAttendanceRequest request =
                new CampaignAttendanceRequest(userId, campaignId, message, date);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        apiService.saveCampaignAttendance(request).enqueue(new Callback<CampaignAttendanceRequest>() {
            @Override
            public void onResponse(Call<CampaignAttendanceRequest> call, Response<CampaignAttendanceRequest> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Response Saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CampaignAttendanceRequest> call, Throwable t) {
                Toast.makeText(context, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------- REMOVE CONSENT ----------
    private void removeConsentFromServer(Long campaignId) {

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        apiService.deleteCampaignAttendance(userId, campaignId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Response Removed!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Delete Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------- VIEW HOLDER ----------
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, viewLocationTextView;
        CheckBox checkboxConsent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.textDate);
            viewLocationTextView = itemView.findViewById(R.id.viewLocationTextView);
            checkboxConsent = itemView.findViewById(R.id.checkboxConsent);
        }
    }
}
