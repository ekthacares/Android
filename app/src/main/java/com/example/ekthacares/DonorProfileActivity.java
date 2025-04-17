package com.example.ekthacares;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.ApiResponse;
import com.example.ekthacares.model.BloodDonation;
import com.example.ekthacares.model.DonationResponse;
import com.example.ekthacares.model.SentEmail;
import com.example.ekthacares.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonorProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_profile);


        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (isSessionValid(jwtToken, userId)) {
            fetchUserDetails(jwtToken, userId);
            fetchDonations();
            fetchSentEmails();
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(DonorProfileActivity.this, DonorHomeActivity1.class);
            startActivity(intent);
            finish(); // Optional: removes this activity from the back stack
        });


    }


    private boolean isSessionValid(String jwtToken, Long userId) {
        return jwtToken != null && userId != -1;
    }

    private void fetchUserDetails(String jwtToken, Long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // Get the user object from the responsel
                    User user = response.body();

                    if (user != null) {
                        // Extract the donorName
                        String donorName = user.getDonorName();
                        String bloodtype = user.getBloodGroup();
                        String location = user.getAddress() + ", " + user.getCity();


                        // Display donorName in a TextView (assuming you have a TextView with id donor_name)
                        TextView donorNameTextView = findViewById(R.id.donor_name);
                        TextView bloodtypeTextView = findViewById(R.id.bloodtype);
                        TextView locationTextView = findViewById(R.id.location);

                        donorNameTextView.setText(donorName);
                        bloodtypeTextView.setText(bloodtype);
                        locationTextView.setText(location);

                        LinearLayout layoutCall = findViewById(R.id.layoutCall);
                        layoutCall.setOnClickListener(v -> {
                            String phoneNumber = user.getMobile(); // get the user's phone number
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:+91" + phoneNumber));
                            v.getContext().startActivity(intent);
                        });

                    } else {
                        Toast.makeText(DonorProfileActivity.this, "User data is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DonorProfileActivity.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(DonorProfileActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                Log.e("FetchError", t.getMessage(), t);
            }
        });
    }

    private void fetchDonations() {
        Log.d(TAG, "fetchDonations: Fetching donations...");

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        Log.d(TAG, "fetchDonations: Retrieved JWT Token = " + jwtToken);
        Log.d(TAG, "fetchDonations: Retrieved User ID = " + userId);

        if (jwtToken == null || userId == -1) {
            Toast.makeText(DonorProfileActivity.this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorizationHeader = "Bearer " + jwtToken;
        Log.d(TAG, "fetchDonations: Authorization Header = " + authorizationHeader);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<DonationResponse> call = apiService.getDonations(authorizationHeader, userId);

        call.enqueue(new Callback<DonationResponse>() {
            @Override
            public void onResponse(Call<DonationResponse> call, Response<DonationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BloodDonation> donations = response.body().getDonations();

                    TextView donorStatusTextView = findViewById(R.id.donor_status);
                    TextView donatedcountTextView = findViewById(R.id.donatedcount);

                    if (donations != null && !donations.isEmpty()) {
                        donorStatusTextView.setText("Not Eligible to Donate ");
                        donatedcountTextView.setText(String.valueOf(donations.size()));



                    } else {
                        donorStatusTextView.setText("Eligible to Donate");
                        donatedcountTextView.setText(String.valueOf(donations.size()));
                    }

                } else {
                    Log.e(TAG, "onResponse: Failed to load donations. Response code: " + response.code());
                    Toast.makeText(DonorProfileActivity.this, "Failed to load donations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: API call failed", t);
                Toast.makeText(DonorProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSentEmails() {
        // Get JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken == null || userId == -1) {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Call the API
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<SentEmail>> call = apiService.getSentEmails("Bearer " + jwtToken, userId);

        call.enqueue(new Callback<List<SentEmail>>() {
            @Override
            public void onResponse(Call<List<SentEmail>> call, Response<List<SentEmail>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        // There are emails, update the adapter
                        List<SentEmail> sentEmails = response.body();
                        TextView RequestedCountTextView = findViewById(R.id.RequestedCount);

                        if (sentEmails != null && !sentEmails.isEmpty()) {
                            RequestedCountTextView.setText(String.valueOf(sentEmails.size()));

                        } else {
                            RequestedCountTextView.setText(String.valueOf(sentEmails.size()));
                        }
                    } else {
                        // Handle error or non-successful response
                        Toast.makeText(DonorProfileActivity.this, "Failed to fetch sent emails. No data available.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SentEmail>> call, Throwable t) {
                Log.e("ReceivedRequests", "Error fetching data", t);
                Toast.makeText(DonorProfileActivity.this, "An error occurred while fetching the emails.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DonorProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
