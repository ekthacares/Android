package com.example.ekthacares;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.ApiResponse;
import com.example.ekthacares.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private TextView tvUserId, tvDonorName, tvEmail, tvMobile, tvDateOfBirth, tvBloodGroup,
            tvAge, tvGender, tvAddress, tvCity, tvState;
    private ImageView imgEditDonorName, imgEditEmail, imgEditAddress, imgEditCity, imgEditState;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
        imgEditDonorName = findViewById(R.id.imgEditDonorName);
        imgEditEmail = findViewById(R.id.imgEditEmail);
        imgEditAddress = findViewById(R.id.imgEditAddress);
        imgEditCity = findViewById(R.id.imgEditCity);
        imgEditState = findViewById(R.id.imgEditState);

        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (isSessionValid(jwtToken, userId)) {
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        imgEditDonorName.setOnClickListener(v -> showEditDialog("Donor Name"));
        imgEditEmail.setOnClickListener(v -> showEditDialog("Email"));
        imgEditAddress.setOnClickListener(v -> showEditDialog("Address"));
        imgEditCity.setOnClickListener(v -> showEditDialog("City"));
        imgEditState.setOnClickListener(v -> showEditDialog("State"));
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
                    currentUser = response.body();
                    updateUI();
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                Log.e("FetchError", t.getMessage(), t);
            }
        });
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e("Response Error", "Error Body: " + errorBody);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        tvUserId.setText(String.valueOf(currentUser.getId()));
        tvDonorName.setText(currentUser.getDonorName());
        tvEmail.setText(currentUser.getEmailId());
        tvMobile.setText(currentUser.getMobile());
        tvDateOfBirth.setText(currentUser.getDateOfBirth());
        tvBloodGroup.setText(currentUser.getBloodGroup());
        tvAge.setText(String.valueOf(currentUser.getAge()));
        tvGender.setText(currentUser.getGender());
        tvAddress.setText(currentUser.getAddress());
        tvCity.setText(currentUser.getCity());
        tvState.setText(currentUser.getState());
    }

    private void showEditDialog(String fieldName) {
        EditText editText = new EditText(EditProfileActivity.this);
        editText.setHint("Enter new " + fieldName);
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Edit " + fieldName)
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedValue = editText.getText().toString();
                    if (!updatedValue.isEmpty()) {
                        updateUserProfile(updatedValue, fieldName);
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Please enter a valid " + fieldName, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void updateUserProfile(String updatedValue, String fieldName) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (isSessionValid(jwtToken, userId)) {
            ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

            // Create a copy of the current user to modify it
            User updatedUser = new User(currentUser);

            // Update the relevant field based on the field name
            switch (fieldName) {
                case "Donor Name": updatedUser.setDonorName(updatedValue); break;
                case "Email": updatedUser.setEmailId(updatedValue); break;
                case "Address": updatedUser.setAddress(updatedValue); break;
                case "City": updatedUser.setCity(updatedValue); break;
                case "State": updatedUser.setState(updatedValue); break;
            }

            // Make API call to update profile with the updatedUser object
            Call<ApiResponse> call = apiService.appupdateProfile("Bearer " + jwtToken, updatedUser);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String message = response.body().getMessage();
                        fetchUserDetails(jwtToken, userId);  // Fetch updated user details
                        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(EditProfileActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateError", t.getMessage(), t);
                }
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "Session invalid. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
