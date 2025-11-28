package com.example.ekthacares;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.SentEmail;
import com.example.ekthacares.model.User;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SentEmailAdapter extends RecyclerView.Adapter<SentEmailAdapter.ViewHolder> {

    private List<SentEmail> sentEmailList;
    private final Context context;
    private String jwtToken;

    public SentEmailAdapter(Context context, List<SentEmail> sentEmailList) {
        this.context = context;
        this.sentEmailList = sentEmailList;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sent_email, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SentEmail sentEmail = sentEmailList.get(position);

        String hospitalName = sentEmail.getHospitalName();
        String sentAt = sentEmail.getSentAt();
        String bloodGroup = sentEmail.getBloodGroup();
        String formattedDate = formatDate(sentAt);


        String text = "RequestedBy: " + sentEmail.getLoggedInUserId();
        holder.tvLoggedInUserId.setText(text);

        String userIdString = String.valueOf(sentEmail.getLoggedInUserId());

        SpannableString spannableText = new SpannableString(text);
        int startIndex = text.indexOf(userIdString);
        int endIndex = startIndex + userIdString.length();

        spannableText.setSpan(new android.text.style.UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new android.text.style.ForegroundColorSpan(context.getResources().getColor(R.color.user_id_color)),
                startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);



        holder.tvLoggedInUserId.setText(spannableText);
        holder.tvHospitalName.setText("Hospital Name: " + hospitalName); // ✅ Fixed the issue
        holder.tvSentAt.setText("Sent At: " + formattedDate);
        holder.tvBloodGroup.setText("Blood Group: " + bloodGroup);

        String url = sentEmail.getConfirmationUrl();
        if (url != null && !url.isEmpty()) {
            String fullText = "Confirm URL: Please Click here to Accept Donation";

            SpannableString spannableString = new SpannableString(fullText);
            int urlStartIndex = "Confirm URL: ".length();
            int urlEndIndex = fullText.length();

            URLSpan urlSpan = new URLSpan(url) {
                @Override
                public void onClick(View widget) {
                    Log.d("URLSpan", "URL clicked: " + url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                }
            };

            spannableString.setSpan(urlSpan, urlStartIndex, urlEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvConfirmationUrl.setText(spannableString);
            holder.tvConfirmationUrl.setMovementMethod(LinkMovementMethod.getInstance());
            holder.tvConfirmationUrl.setHighlightColor(Color.TRANSPARENT);
        } else {
            holder.tvConfirmationUrl.setText("No confirmation URL available.");
        }

        holder.tvLoggedInUserId.setClickable(true);
        holder.tvLoggedInUserId.setOnClickListener(v -> {
            Long userId = sentEmail.getLoggedInUserId();
            fetchUserDetails(userId);
        });

    }

    @Override
    public int getItemCount() {
        return sentEmailList.size();
    }

    // This is the new updateData method to update the data list and notify the adapter
    public void updateData(List<SentEmail> newData) {
        Log.d("SentEmailAdapter", "Updating data, new size: " + newData.size());

        if (newData == null || newData.isEmpty()) {
            // If the data is null or empty, show the "No Emails received" message
            Toast.makeText(context, "No Emails received for this user.", Toast.LENGTH_SHORT).show();
        } else {
            Collections.reverse(newData); // ✅ Reverse the list to show the latest first
            sentEmailList.clear();
            sentEmailList.addAll(newData);
            notifyDataSetChanged();
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoggedInUserId, tvSentAt, tvConfirmationUrl, tvHospitalName, tvBloodGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLoggedInUserId = itemView.findViewById(R.id.tvLoggedInUserId);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvSentAt = itemView.findViewById(R.id.tvSentAt);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvConfirmationUrl = itemView.findViewById(R.id.tvConfirmationUrl);
        }
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy ");
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateStr;
        }
    }

    private void fetchUserDetails(Long userId) {
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
                Log.e("SentEmailAdapter", "Error fetching user details", t);
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
