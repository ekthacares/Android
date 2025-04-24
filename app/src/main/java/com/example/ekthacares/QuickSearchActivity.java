package com.example.ekthacares;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.BloodSearchResponse;
import com.example.ekthacares.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuickSearchActivity extends AppCompatActivity {

    private EditText editTextBloodGroup, editTextCity, editTextState, editTextHospital, editTextRequestedDate;
    private Button buttonSearch;
    private TextView textViewResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_search);

        // Initialize UI elements
        editTextBloodGroup = findViewById(R.id.editTextBloodGroup);
        editTextCity = findViewById(R.id.editTextCity);
        editTextState = findViewById(R.id.editTextState);
        editTextHospital = findViewById(R.id.editTextHospital);
        editTextRequestedDate = findViewById(R.id.editTextRequestedDate);  // Newly added for requested date input
        buttonSearch = findViewById(R.id.buttonSearch);
        textViewResults = findViewById(R.id.textViewResults);

        // Set onClickListener for the search button
        buttonSearch.setOnClickListener(v -> performSearchForBlood());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        // Check if token and userId are valid
        if (jwtToken != null && userId != -1) {
            Log.d("QuickSearchActivity", "JWT Token: " + jwtToken);
            Log.d("QuickSearchActivity", "User ID: " + userId);
        } else {
            Log.d("QuickSearchActivity", "No valid JWT token or user ID found.");
            Toast.makeText(this, "You need to log in again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearchForBlood() {
        // Retrieve the user input for blood group, city, state, hospital name, and requested date
        String bloodGroup = editTextBloodGroup.getText().toString().trim();
        String city = editTextCity.getText().toString().trim();
        String state = editTextState.getText().toString().trim();
        String hospitalName = editTextHospital.getText().toString().trim();
        String requestedDate = editTextRequestedDate.getText().toString().trim(); // Get requested date input

        // Validate inputs
        if (bloodGroup.isEmpty() || city.isEmpty() || state.isEmpty() || requestedDate.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        textViewResults.setVisibility(View.GONE);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Retrieve JWT token and userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken != null && userId != -1) {
            // Set up headers for the request
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + jwtToken);
            headers.put("userId", String.valueOf(userId)); // Add userId as a header

            // Perform the network request
            ApiService apiService = RetrofitClient.getApiService();
            Call<BloodSearchResponse> call = apiService.searchForBlood(headers, bloodGroup, city, state, hospitalName, requestedDate);

            call.enqueue(new Callback<BloodSearchResponse>() {
                @Override
                public void onResponse(Call<BloodSearchResponse> call, Response<BloodSearchResponse> response) {
                    progressBar.setVisibility(View.GONE); // Hide loading indicator

                    if (response.isSuccessful() && response.body() != null) {
                        BloodSearchResponse bloodSearchResponse = response.body();
                        List<User> users = bloodSearchResponse.getResults();

                        if (users != null && !users.isEmpty()) {
                            // Display the results
                            StringBuilder resultText = new StringBuilder();
                            resultText.append("Found ").append(users.size()).append(" donor(s):\n\n");
                            for (User user : users) {
                                resultText.append("Name: ").append(user.getDonorName())
                                        .append("\nBlood Group: ").append(user.getBloodGroup())
                                        .append("\nEmail: ").append(user.getEmailId())
                                        .append("\nCity: ").append(user.getCity())
                                        .append("\nContact: ").append(user.getMobile())
                                        .append("\n\n");
                            }
                            textViewResults.setVisibility(View.VISIBLE);
                            textViewResults.setText(resultText.toString());
                        } else {
                            textViewResults.setVisibility(View.VISIBLE);
                            textViewResults.setText("No donors found.");
                        }
                    } else {
                        textViewResults.setVisibility(View.VISIBLE);
                        textViewResults.setText("Error in search. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<BloodSearchResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE); // Hide loading indicator
                    Log.e("QuickSearchActivity", "Network request failed: " + t.getMessage());
                    textViewResults.setVisibility(View.VISIBLE);
                    textViewResults.setText("Network request failed. Please check your connection.");
                }
            });
        } else {
            progressBar.setVisibility(View.GONE); // Hide loading indicator
            Toast.makeText(this, "Invalid token or user ID. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }
}
