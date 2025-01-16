package com.example.ekthacares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonorHomeActivity extends AppCompatActivity {

    private Button btnLogout, btnProfile, btnMyDonations, btnReceivedRequests, btnQuickSearch, btnDonorTracking ;
    private TextView tvWelcomeMessage; // TextView for welcome message


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_home);

        // Initialize views
        ImageView ivLogout = findViewById(R.id.ivLogout);
        btnProfile = findViewById(R.id.btnProfile);
        btnMyDonations = findViewById(R.id.btnMyDonations);  // Initialize btnMyDonations
        btnQuickSearch = findViewById(R.id.btnQuickSearch);
        btnReceivedRequests = findViewById(R.id.btnReceivedRequests); // Initialize btnReceivedRequests
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);  // Initialize TextView
        btnDonorTracking = findViewById(R.id.btnDonorTracking);

        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        // Log and check if JWT Token and User ID are valid
        if (jwtToken != null && userId != -1) {
            // You can fetch user details if necessary (without displaying them)
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        // Logout functionality
        ivLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(DonorHomeActivity.this)
                    .setMessage("Are you sure you want to log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        // Clear SharedPreferences and redirect to login screen
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        redirectToLogin();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Profile button functionality
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DonorHomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // My Donations button functionality
        btnMyDonations.setOnClickListener(v -> {
            Intent intent = new Intent(DonorHomeActivity.this, MyDonationsActivity.class);
            startActivity(intent);
        });

        // Received Requests button functionality
        btnReceivedRequests.setOnClickListener(v -> {
            Intent intent = new Intent(DonorHomeActivity.this, ReceivedRequestsActivity.class);
            startActivity(intent);
        });

        // Set click listener for Quick Search button
        btnQuickSearch.setOnClickListener(v -> {
            Intent intent = new Intent(DonorHomeActivity.this, QuickSearchActivity.class);
            startActivity(intent);
        });

        // Donor Tracking button functionality
        btnDonorTracking.setOnClickListener(v -> {
            // Open DonorTrackingActivity
            Intent intent = new Intent(DonorHomeActivity.this, DonorTrackingActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        // Log and check if JWT Token and User ID are valid
        if (jwtToken != null && userId != -1) {
            // You can fetch user details if necessary (without displaying them)
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void fetchUserDetails(String jwtToken, Long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the network request
        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // Log the response for debugging (but do not display any details)
                Log.d("DonorHomeActivity", "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    User userDetails = response.body();

                    // Optionally log user details but do not set them to UI
                    Log.d("DonorHomeActivity", "User Details: " + userDetails);

                    // Update the welcome message with donor's name
                    tvWelcomeMessage.setText("Welcome, " + userDetails.getDonorName()); // Assuming getName() returns the donor's name
                } else {
                    Toast.makeText(DonorHomeActivity.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("DonorHomeActivity", "Error fetching user details", t);
                Toast.makeText(DonorHomeActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DonorHomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
