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
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SentEmailAdapter extends RecyclerView.Adapter<SentEmailAdapter.ViewHolder> {

    private final List<SentEmail> sentEmailList;
    private final Context context;

    private String jwtToken;

    public SentEmailAdapter(Context context, List<SentEmail> sentEmailList) {
        this.context = context;
        this.sentEmailList = sentEmailList;
        // Initialize the JWT token from SharedPreferences or other methods
        // Retrieve JWT token and user ID from SharedPreferences
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

        // Format the Sent At date
        String sentAt = sentEmail.getSentAt();
        String formattedDate = formatDate(sentAt);  // Method to format the date

        // Create the base text with "RequestedBy: " and the logged-in user ID
        String text = "RequestedBy: " + sentEmail.getLoggedInUserId();

        // Set the full text to the TextView
        holder.tvLoggedInUserId.setText(text);

        // Convert the userId (Long) to String for indexOf
        String userIdString = String.valueOf(sentEmail.getLoggedInUserId());

        // Create a SpannableString
        SpannableString spannableText = new SpannableString(text);

        // Find the start and end index of the user ID part
        int startIndex = text.indexOf(userIdString);
        int endIndex = startIndex + userIdString.length();

        // Apply underline and color for the user ID
        spannableText.setSpan(new android.text.style.UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(
                new android.text.style.ForegroundColorSpan(context.getResources().getColor(R.color.user_id_color)),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );


        // Set the SpannableString to the TextView
        holder.tvLoggedInUserId.setText(spannableText);


        holder.tvSentAt.setText("Sent At: " + formattedDate);

        String url = sentEmail.getConfirmationUrl();
        if (url != null && !url.isEmpty()) {
            // Combine the "Confirm URL: " text with "Click here" as the clickable part
            String fullText = "Confirm URL: Please Click here to start Donation";

            // Create a SpannableString to make "Click here" part clickable
            SpannableString spannableString = new SpannableString(fullText);

            // Define the start and end index for the clickable part (i.e., "Click here")
            int urlStartIndex = "Confirm URL: ".length();
            int urlEndIndex = fullText.length();

            // Create a clickable URLSpan for the "Click here" part
            URLSpan urlSpan = new URLSpan(url) {
                @Override
                public void onClick(View widget) {
                    // Open the URL in a browser when clicked
                    Log.d("URLSpan", "URL clicked: " + url);  // Log to confirm the click is registered
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                }
            };

            // Apply the clickable span to the "Click here" part
            spannableString.setSpan(urlSpan, urlStartIndex, urlEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the spannable string to the TextView
            holder.tvConfirmationUrl.setText(spannableString);

            // Enable the TextView to recognize and handle the clickable link
            holder.tvConfirmationUrl.setMovementMethod(LinkMovementMethod.getInstance());

            // Ensure the TextView doesn't underline the entire text (optional)
            holder.tvConfirmationUrl.setHighlightColor(Color.TRANSPARENT);  // Optional: removes highlighting when clicked
        } else {
            // Handle the case where the URL is null or empty
            holder.tvConfirmationUrl.setText("No confirmation URL available.");
        }

        // Set OnClickListener for showing user details when clicked
        // Ensure the TextView is clickable
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoggedInUserId, tvSentAt, tvConfirmationUrl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLoggedInUserId = itemView.findViewById(R.id.tvLoggedInUserId);
            tvSentAt = itemView.findViewById(R.id.tvSentAt);
            tvConfirmationUrl = itemView.findViewById(R.id.tvConfirmationUrl);
        }
    }

    private String formatDate(String dateStr) {
        try {
            // Adjust the date format according to the format of the input string (sentAt)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // Adjust format as needed
            Date date = inputFormat.parse(dateStr);

            // Format it to your preferred format
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy ");
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateStr;  // Return the original string in case of error
        }
    }

    // Fetch user details by userId
    private void fetchUserDetails(Long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the network request
        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User userDetails = response.body();

                    // Log user data to check
                    Log.d("UserDetails", "User data: " + userDetails.toString());

                    // Show user details in a custom popup or Toast
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

    // Display user details in a popup window
    private void showUserDetailsPopup(User userDetails) {
        // Create a PopupWindow or use a Toast, based on your requirement
        String userInfo = "Name: " + userDetails.getDonorName() + "\n" +
                "Email: " + userDetails.getEmailId() + "\n" +
                "Blood Group: " + userDetails.getBloodGroup();

        Toast.makeText(context, userInfo, Toast.LENGTH_LONG).show();
        // If you prefer a simple alert dialog as a popup instead of a Toast, you can use this:
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("User Details")
                .setMessage(userInfo)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
