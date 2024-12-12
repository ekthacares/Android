package com.example.ekthacares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserId, tvDonorName, tvEmail, tvMobile, tvDateOfBirth, tvBloodGroup,
            tvAge, tvGender, tvAddress, tvCity, tvState;
    private ImageView imgProfile;
    private Button btnEditProfile, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        tvUserId = findViewById(R.id.tvUserId);
        tvDonorName = findViewById(R.id.tvDonorName);
        tvEmail = findViewById(R.id.tvEmail);
        tvMobile = findViewById(R.id.tvMobile);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        tvAge = findViewById(R.id.tvAge);
        tvGender = findViewById(R.id.tvGender);
        tvAddress = findViewById(R.id.tvAddress);
        tvCity = findViewById(R.id.tvCity);
        tvState = findViewById(R.id.tvState);
        imgProfile = findViewById(R.id.imgProfile);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBack = findViewById(R.id.btnBack);

        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1); // Retrieving userId as Long directly

        if (jwtToken != null && userId != -1) {
            // Fetch user details from the API if necessary
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        // Back button functionality
        btnBack.setOnClickListener(v -> finish()); // Go back to previous activity

        // Edit profile functionality
        btnEditProfile.setOnClickListener(v -> {
            // Open EditProfileActivity (you can create this activity if needed)
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserDetails(String jwtToken, Long userId) {
        // Get Retrofit instance from RetrofitClient
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the network request
        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User userDetails = response.body();
                    // Display user details
                    tvUserId.setText("User ID: " + userDetails.getId());
                    tvDonorName.setText("Donor Name: " + userDetails.getDonorName());
                    tvEmail.setText("Email: " + userDetails.getEmailId());
                    tvMobile.setText("Mobile: " + userDetails.getMobile());
                    tvDateOfBirth.setText("Date of Birth: " + userDetails.getDateOfBirth());
                    tvBloodGroup.setText("Blood Group: " + userDetails.getBloodGroup());
                    tvAge.setText("Age: " + userDetails.getAge());
                    tvGender.setText("Gender: " + userDetails.getGender());
                    tvAddress.setText("Address: " + userDetails.getAddress());
                    tvCity.setText("City: " + userDetails.getCity());
                    tvState.setText("State: " + userDetails.getState());


                    // Optionally set a profile image (replace with real image if available)
                    // imgProfile.setImageResource(R.drawable.ic_profile_picture);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
