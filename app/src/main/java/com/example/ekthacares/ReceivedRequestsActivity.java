package com.example.ekthacares;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.SentEmail;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceivedRequestsActivity extends AppCompatActivity {

    private RecyclerView rvReceivedRequests;
    private SentEmailAdapter adapter;
    private List<SentEmail> sentEmailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_requests);

        rvReceivedRequests = findViewById(R.id.rvReceivedRequests);
        rvReceivedRequests.setLayoutManager(new LinearLayoutManager(this));

        sentEmailList = new ArrayList<>();
        adapter = new SentEmailAdapter(this, sentEmailList);
        rvReceivedRequests.setAdapter(adapter);

        // Fetch the sent emails
        fetchSentEmails();
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
                        Log.d("ReceivedRequests", "Fetched emails: " + sentEmails.size());
                        adapter.updateData(sentEmails);
                        // Hide the "No Emails" message
                        findViewById(R.id.noEmailsMessage).setVisibility(View.GONE);
                    } else {
                        // No emails, show the message from the response
                        Toast.makeText(ReceivedRequestsActivity.this, "No Emails received for this user.", Toast.LENGTH_SHORT).show();
                        adapter.updateData(new ArrayList<>()); // Update with an empty list
                        // Show the "No Emails" message on the screen
                        // Log the visibility change for debugging
                        Log.d("ReceivedRequests", "Setting visibility: " + (response.body().isEmpty() ? "VISIBLE" : "GONE"));

                        findViewById(R.id.noEmailsMessage).setVisibility(View.VISIBLE); // Show the message
                        View noEmailsMessage = findViewById(R.id.noEmailsMessage);
                        Log.d("ReceivedRequests", "Visibility of noEmailsMessage: " + noEmailsMessage.getVisibility());
                        Log.d("ReceivedRequests", "Layout width: " + noEmailsMessage.getLayoutParams().width);
                        Log.d("ReceivedRequests", "Layout height: " + noEmailsMessage.getLayoutParams().height);
                    }
                } else {
                    // Handle error or non-successful response
                    Toast.makeText(ReceivedRequestsActivity.this, "Failed to fetch sent emails. No data available.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<List<SentEmail>> call, Throwable t) {
                Log.e("ReceivedRequests", "Error fetching data", t);
                Toast.makeText(ReceivedRequestsActivity.this, "An error occurred while fetching the emails.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

