package com.example.ekthacares;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.Constants;
import com.example.ekthacares.R;
import com.example.ekthacares.DonationAdapter;
import com.example.ekthacares.model.BloodDonation; // Update the import to match your model class
import com.example.ekthacares.model.DonationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyDonationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DonationAdapter donationAdapter;
    private TextView noDonationsMessage; // Add TextView for "No donations available" message
    private TextView totalDonationsCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_donations);

        recyclerView = findViewById(R.id.recyclerViewDonations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the TextView for "No donations available"
        noDonationsMessage = findViewById(R.id.noDonationsMessage);
        totalDonationsCount = findViewById(R.id.totalDonationsCount);
        Log.d(TAG, "onCreate: Activity started");

        // Fetch donations from the backend
        fetchDonations();
    }

    private void fetchDonations() {
        Log.d(TAG, "fetchDonations: Fetching donations...");

        // Retrieve the token and userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        Log.d(TAG, "fetchDonations: Retrieved JWT Token = " + jwtToken);
        Log.d(TAG, "fetchDonations: Retrieved User ID = " + userId);

        // Check if either the token or userId is missing
        if (jwtToken == null || userId == -1) {
            Toast.makeText(MyDonationsActivity.this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return; // Exit if data is missing
        }

        // Create the Authorization header using the retrieved JWT token
        String authorizationHeader = "Bearer " + jwtToken;
        Log.d(TAG, "fetchDonations: Authorization Header = " + authorizationHeader);

        // Proceed with the API call using the retrieved values
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<DonationResponse> call = apiService.getDonations(authorizationHeader, userId);

        call.enqueue(new Callback<DonationResponse>() {
            @Override
            public void onResponse(Call<DonationResponse> call, Response<DonationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BloodDonation> donations = response.body().getDonations();

                    // Check if donations is null or empty
                    if (donations != null && !donations.isEmpty()) {
                        Log.d(TAG, "onResponse: API call successful. Donations retrieved: " + donations.size());
                        donationAdapter = new DonationAdapter(donations);
                        recyclerView.setAdapter(donationAdapter);

                        // Update the total donations count
                        totalDonationsCount.setText("Total Donations: " + donations.size());

                        // Hide the "No donations available" message and show RecyclerView
                        noDonationsMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        Log.e(TAG, "onResponse: No donations found.");
                        Toast.makeText(MyDonationsActivity.this, "No donations available", Toast.LENGTH_SHORT).show();

                        // Show the "No donations available" message and hide RecyclerView
                        noDonationsMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        // Set total donations count to 0
                        // Set total donations count to 0
                        totalDonationsCount.setText("Total Donations: 0");
                    }
                } else {
                    Log.e(TAG, "onResponse: Failed to load donations. Response code: " + response.code());
                    Toast.makeText(MyDonationsActivity.this, "Failed to load donations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: API call failed", t);
                Toast.makeText(MyDonationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
