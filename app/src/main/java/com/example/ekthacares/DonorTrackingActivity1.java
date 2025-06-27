package com.example.ekthacares;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ekthacares.model.Confirmation;
import com.example.ekthacares.model.ConfirmationResponse;
import com.example.ekthacares.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonorTrackingActivity1 extends AppCompatActivity {

    private ImageView imgBackArrow, imageInfo, imageCall;
    private TextView tvProfileHeading, textViewDonorName, noTrackingInfoMessage;

    private View dottedLine;

    private User user; // Declare class-level User object

    private List<Confirmation> confirmations = new ArrayList<>(); // Define the confirmations list here



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_tracking1);

        imageInfo = findViewById(R.id.imageinfo);
        imageCall = findViewById(R.id.imagecall);
        tvProfileHeading = findViewById(R.id.tvProfileHeading);
        textViewDonorName = findViewById(R.id.textViewDonorName);
        noTrackingInfoMessage = findViewById(R.id.noTrackingInfoMessage);
        dottedLine = findViewById(R.id.dottedLine);

        // Back arrow
        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            // Use OnBackPressedDispatcher to go back to the previous activity
            getOnBackPressedDispatcher().onBackPressed();
        });

        imageInfo.setOnClickListener(v -> {
            if (confirmations != null && !confirmations.isEmpty()) {
                Confirmation confirmation = confirmations.get(0);

                // Inflate the custom layout for the dialog
                LayoutInflater inflater = LayoutInflater.from(DonorTrackingActivity1.this);
                View dialogView = inflater.inflate(R.layout.dialog_confirmation_details, null);

                // Set the data into the views
                ((TextView) dialogView.findViewById(R.id.recipient_text)).setText("Recipient: " + confirmation.getRecipientId());
                ((TextView) dialogView.findViewById(R.id.confirmed_at_text)).setText("Confirmed At: " + confirmation.getFormattedConfirmedAt());
                ((TextView) dialogView.findViewById(R.id.start_time_text)).setText("Donation Start Time: " + confirmation.getFormattedStartedAt());
                ((TextView) dialogView.findViewById(R.id.stop_time_text)).setText("Donation Finished Time: " + confirmation.getFormattedStoppedAt());

                // Build the AlertDialog
                AlertDialog dialog = new AlertDialog.Builder(DonorTrackingActivity1.this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();

                // Set background transparent so rounded corners show
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                dialog.show();

                // Handle OK button (now a TextView)
                TextView okButton = dialogView.findViewById(R.id.ok_button);
                okButton.setOnClickListener(v1 -> dialog.dismiss());

            } else {
                Toast.makeText(DonorTrackingActivity1.this, "No confirmation data available", Toast.LENGTH_SHORT).show();
            }
        });



        imageCall.setOnClickListener(v -> {
            // Make sure the user object is available and contains a mobile number
            if (user != null && user.getMobile() != null && !user.getMobile().isEmpty()) {
                String phone = user.getMobile();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:+91" + phone));
                startActivity(callIntent);
            } else {
                Toast.makeText(DonorTrackingActivity1.this, "Donor contact not available yet", Toast.LENGTH_SHORT).show();
            }
        });

        fetchDonorTrackingData();
    }

    private void fetchDonorTrackingData() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken == null || jwtToken.isEmpty() || userId == -1) {
            Toast.makeText(DonorTrackingActivity1.this, "User not logged in or invalid credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ConfirmationResponse> call = apiService.getDonationTracking("Bearer " + jwtToken, userId);

        call.enqueue(new Callback<ConfirmationResponse>() {
            @Override
            public void onResponse(Call<ConfirmationResponse> call, Response<ConfirmationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConfirmationResponse confirmationResponse = response.body();
                    confirmations = confirmationResponse.getConfirmations(); // Update the confirmations list
                    if (confirmations == null) confirmations = new ArrayList<>();

                    // Only show the message if the list is empty
                    if (confirmations.isEmpty()) {
                        noTrackingInfoMessage.setVisibility(View.VISIBLE);
                        textViewDonorName.setVisibility(View.GONE);
                        imageInfo.setVisibility(View.GONE);
                        imageCall.setVisibility(View.GONE);
                        dottedLine.setVisibility(View.GONE);
                    } else {
                        noTrackingInfoMessage.setVisibility(View.GONE);
                        // Get the recipientId from the first confirmation and fetch user info
                        Long loggedInUserId = confirmations.get(0).getRecipientId();
                        fetchUserName(loggedInUserId, textViewDonorName);
                    }
                } else {
                    Toast.makeText(DonorTrackingActivity1.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConfirmationResponse> call, Throwable t) {
                Log.e("DonorTrackingActivity", "API Call Failed", t);
                Toast.makeText(DonorTrackingActivity1.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserName(Long userId, TextView textView) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<User> call = apiService.getUserDetails1(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    user = response.body(); // Store the User object
                    textView.setText(user.getDonorName()); // Update TextView with the fetched name
                    imageCall.setEnabled(true); // Enable call button if the user is available
                } else {
                    textView.setText("Unknown User"); // Default if not found
                    Log.e("User Fetch", "Failed to fetch user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                textView.setText("Error fetching user");
                Log.e("User Fetch", "Error fetching user details", t);
            }
        });
    }
}
