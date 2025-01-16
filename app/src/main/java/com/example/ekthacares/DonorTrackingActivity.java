package com.example.ekthacares;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.Confirmation;
import com.example.ekthacares.model.ConfirmationResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonorTrackingActivity extends AppCompatActivity {

    private RecyclerView confirmationsRecyclerView;
    private ConfirmationAdapter confirmationAdapter;
    private TextView noTrackingInfoMessage;  // TextView for "No donation tracking information available."

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_tracking);

        // Initialize UI components
        confirmationsRecyclerView = findViewById(R.id.confirmationsRecyclerView);
        noTrackingInfoMessage = findViewById(R.id.noTrackingInfoMessage);  // Add the reference to the TextView

        // Set up RecyclerView
        confirmationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        confirmationAdapter = new ConfirmationAdapter();
        confirmationsRecyclerView.setAdapter(confirmationAdapter);

        // Fetch data from API using SharedPreferences
        fetchDonorTrackingData();
    }

    private void fetchDonorTrackingData() {
        // Retrieve JWT token and userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        // Check if token or userId is null or invalid
        if (jwtToken == null || jwtToken.isEmpty() || userId == -1) {
            Toast.makeText(DonorTrackingActivity.this, "User not logged in or invalid credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ConfirmationResponse> call = apiService.getDonationTracking("Bearer " + jwtToken, userId);

        call.enqueue(new Callback<ConfirmationResponse>() {
            @Override
            public void onResponse(Call<ConfirmationResponse> call, Response<ConfirmationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConfirmationResponse confirmationResponse = response.body();

                    // Ensure the 'confirmations' list is not null before checking it
                    List<Confirmation> confirmations = confirmationResponse.getConfirmations();
                    if (confirmations == null) {
                        confirmations = new ArrayList<>();  // Initialize an empty list if it's null
                    }

                    // Check if the confirmations list is empty and show the message accordingly
                    if (confirmations.isEmpty()) {
                        noTrackingInfoMessage.setVisibility(View.VISIBLE);  // Show the message
                    } else {
                        noTrackingInfoMessage.setVisibility(View.GONE);  // Hide the message
                    }

                    // Update the adapter with data
                    confirmationAdapter.setConfirmations(DonorTrackingActivity.this, confirmations);
                } else {
                    // Handle cases where the response is unsuccessful or empty
                    Toast.makeText(DonorTrackingActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConfirmationResponse> call, Throwable t) {
                Log.e("DonorTrackingActivity", "API Call Failed", t);
                Toast.makeText(DonorTrackingActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
