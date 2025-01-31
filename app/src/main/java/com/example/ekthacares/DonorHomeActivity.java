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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonorHomeActivity extends AppCompatActivity {

    private Button btnLogout, btnProfile, btnMyDonations, btnReceivedRequests, btnQuickSearch, btnDonorTracking, btnRequestBlood;
    private TextView tvWelcomeMessage;
    private String jwtToken;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_home);

        // Initialize views
        ImageView ivLogout = findViewById(R.id.ivLogout);
        btnProfile = findViewById(R.id.btnProfile);
        btnMyDonations = findViewById(R.id.btnMyDonations);
        btnQuickSearch = findViewById(R.id.btnQuickSearch);
        btnReceivedRequests = findViewById(R.id.btnReceivedRequests);
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        btnDonorTracking = findViewById(R.id.btnDonorTracking);
        btnRequestBlood = findViewById(R.id.btnRequestBlood);

        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken != null && userId != -1) {
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        // Retrieve and store FCM Token
        fetchFcmToken();

        // Logout functionality
        ivLogout.setOnClickListener(v -> showLogoutDialog());

        // Profile button functionality
        btnProfile.setOnClickListener(v -> startActivity(new Intent(DonorHomeActivity.this, ProfileActivity.class)));

        // My Donations button functionality
        btnMyDonations.setOnClickListener(v -> startActivity(new Intent(DonorHomeActivity.this, MyDonationsActivity.class)));

        // Received Requests button functionality
        btnReceivedRequests.setOnClickListener(v -> startActivity(new Intent(DonorHomeActivity.this, ReceivedRequestsActivity.class)));

        // Quick Search button functionality
        btnQuickSearch.setOnClickListener(v -> startActivity(new Intent(DonorHomeActivity.this, QuickSearchActivity.class)));

        // Donor Tracking button functionality
        btnDonorTracking.setOnClickListener(v -> startActivity(new Intent(DonorHomeActivity.this, DonorTrackingActivity.class)));

        // Request Blood button functionality
        btnRequestBlood.setOnClickListener(v -> startActivity(new Intent(DonorHomeActivity.this, RequestBloodActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken != null && userId != -1) {
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void fetchUserDetails(String jwtToken, Long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("DonorHomeActivity", "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    User userDetails = response.body();
                    Log.d("DonorHomeActivity", "User Details: " + userDetails);
                    tvWelcomeMessage.setText("Welcome, " + userDetails.getDonorName());
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

    private void fetchFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("FCM", "Fetching FCM token failed", task.getException());
                return;
            }

            // Get the FCM token
            String fcmToken = task.getResult();
            Log.d("FCM", "FCM Token: " + fcmToken);


            // Store FCM Token in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("FCM_TOKEN", fcmToken);
            editor.apply();

            // Send FCM Token to Backend
            sendFcmTokenToServer(fcmToken);

        });
    }

    private void sendFcmTokenToServer(String fcmToken) {
        if (jwtToken == null || userId == -1) {
            Log.e("FCM", "User not logged in, skipping FCM token upload.");
            return;
        }

        // Prepare the request body with fcmToken as a key-value pair in a map
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("fcmToken", fcmToken);

        // Get the API service
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the API call
        Call<Void> call = apiService.updateFcmToken("Bearer " + jwtToken, userId, requestBody);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "FCM Token sent to server successfully");
                } else {
                    Log.e("FCM", "Failed to send FCM Token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Error sending FCM Token", t);
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(DonorHomeActivity.this)
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    redirectToLogin();
                })
                .setNegativeButton("No", null)
                .show();
          }

    private void redirectToLogin() {
        Intent intent = new Intent(DonorHomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
