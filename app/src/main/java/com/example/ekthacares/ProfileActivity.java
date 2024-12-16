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
import com.example.ekthacares.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserId, tvDonorName, tvEmail, tvMobile, tvDateOfBirth, tvBloodGroup,
            tvAge, tvGender, tvAddress, tvCity, tvState;
    private ImageView imgEditDonorName, imgEditEmail, imgEditAddress, imgEditCity, imgEditState;

    private User currentUser;

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
        imgEditDonorName = findViewById(R.id.imgEditDonorName);
        imgEditEmail = findViewById(R.id.imgEditEmail);
        imgEditAddress = findViewById(R.id.imgEditAddress);
        imgEditCity = findViewById(R.id.imgEditCity);
        imgEditState = findViewById(R.id.imgEditState);

        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken != null && userId != -1) {
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

    private void fetchUserDetails(String jwtToken, Long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);

        // Print URL and Headers for Debugging
        String url = Constants.BASE_URL + "user/" + userId;
        Log.d("API Request", "URL: " + url);
        Log.d("API Request", "Authorization: Bearer " + jwtToken);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    // Set data to views
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

    private void showEditDialog(String fieldName) {
        EditText editText = new EditText(ProfileActivity.this);
        editText.setHint("Enter new " + fieldName);
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Edit " + fieldName)
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedValue = editText.getText().toString();
                    if (!updatedValue.isEmpty()) {
                        updateUserProfile(updatedValue, fieldName);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Please enter a valid " + fieldName, Toast.LENGTH_SHORT).show();
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

        // Debugging log to check the token
        Log.d("JWT Token", "JWT Token: " + jwtToken);

        if (jwtToken != null && userId != -1) {
            ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

            // Create a copy of the current user
            User updatedUser = new User();
            updatedUser.setId(currentUser.getId());

            // Update only the field that the user modified, preserve others
            switch (fieldName) {
                case "Donor Name":
                    updatedUser.setDonorName(updatedValue);
                    updatedUser.setEmailId(currentUser.getEmailId());
                    updatedUser.setAddress(currentUser.getAddress());
                    updatedUser.setCity(currentUser.getCity());
                    updatedUser.setState(currentUser.getState());
                    updatedUser.setMobile(currentUser.getMobile());
                    updatedUser.setDateOfBirth(currentUser.getDateOfBirth());
                    updatedUser.setBloodGroup(currentUser.getBloodGroup());
                    updatedUser.setAge(Integer.valueOf(String.valueOf(currentUser.getAge())));
                    updatedUser.setGender(currentUser.getGender());
                    updatedUser.setCreatedAt(currentUser.getCreatedAt());
                    updatedUser.setCreatedBy(currentUser.getCreatedBy()); // Preserve createdBy
                    updatedUser.setCreatedByType(currentUser.getCreatedByType()); // Preserve createdByType
                    break;
                case "Email":
                    updatedUser.setEmailId(updatedValue);
                    updatedUser.setDonorName(currentUser.getDonorName());
                    updatedUser.setAddress(currentUser.getAddress());
                    updatedUser.setCity(currentUser.getCity());
                    updatedUser.setState(currentUser.getState());
                    updatedUser.setMobile(currentUser.getMobile());
                    updatedUser.setDateOfBirth(currentUser.getDateOfBirth());
                    updatedUser.setBloodGroup(currentUser.getBloodGroup());
                    updatedUser.setAge(Integer.valueOf(String.valueOf(currentUser.getAge())));
                    updatedUser.setGender(currentUser.getGender());
                    updatedUser.setCreatedAt(currentUser.getCreatedAt());
                    updatedUser.setCreatedBy(currentUser.getCreatedBy()); // Preserve createdBy
                    updatedUser.setCreatedByType(currentUser.getCreatedByType()); // Preserve createdByType
                    break;
                case "Address":
                    updatedUser.setAddress(updatedValue);
                    updatedUser.setDonorName(currentUser.getDonorName());
                    updatedUser.setEmailId(currentUser.getEmailId());
                    updatedUser.setCity(currentUser.getCity());
                    updatedUser.setState(currentUser.getState());
                    updatedUser.setMobile(currentUser.getMobile());
                    updatedUser.setDateOfBirth(currentUser.getDateOfBirth());
                    updatedUser.setBloodGroup(currentUser.getBloodGroup());
                    updatedUser.setAge(Integer.valueOf(String.valueOf(currentUser.getAge())));
                    updatedUser.setGender(currentUser.getGender());
                    updatedUser.setCreatedAt(currentUser.getCreatedAt());
                    updatedUser.setCreatedBy(currentUser.getCreatedBy()); // Preserve createdBy
                    updatedUser.setCreatedByType(currentUser.getCreatedByType()); // Preserve createdByType
                    break;
                case "City":
                    updatedUser.setCity(updatedValue);
                    updatedUser.setDonorName(currentUser.getDonorName());
                    updatedUser.setEmailId(currentUser.getEmailId());
                    updatedUser.setAddress(currentUser.getAddress());
                    updatedUser.setState(currentUser.getState());
                    updatedUser.setMobile(currentUser.getMobile());
                    updatedUser.setDateOfBirth(currentUser.getDateOfBirth());
                    updatedUser.setBloodGroup(currentUser.getBloodGroup());
                    updatedUser.setAge(Integer.valueOf(String.valueOf(currentUser.getAge())));
                    updatedUser.setGender(currentUser.getGender());
                    updatedUser.setCreatedAt(currentUser.getCreatedAt());
                    updatedUser.setCreatedBy(currentUser.getCreatedBy()); // Preserve createdBy
                    updatedUser.setCreatedByType(currentUser.getCreatedByType()); // Preserve createdByType
                    break;
                case "State":
                    updatedUser.setState(updatedValue);
                    updatedUser.setDonorName(currentUser.getDonorName());
                    updatedUser.setEmailId(currentUser.getEmailId());
                    updatedUser.setAddress(currentUser.getAddress());
                    updatedUser.setCity(currentUser.getCity());
                    updatedUser.setMobile(currentUser.getMobile());
                    updatedUser.setDateOfBirth(currentUser.getDateOfBirth());
                    updatedUser.setBloodGroup(currentUser.getBloodGroup());
                    updatedUser.setAge(Integer.valueOf(String.valueOf(currentUser.getAge())));
                    updatedUser.setGender(currentUser.getGender());
                    updatedUser.setCreatedAt(currentUser.getCreatedAt());
                    updatedUser.setCreatedBy(currentUser.getCreatedBy()); // Preserve createdBy
                    updatedUser.setCreatedByType(currentUser.getCreatedByType()); // Preserve createdByType
                    break;
            }

            // Ensure that the JWT token is sent in the header, not inside the body
            Call<String> call = apiService.updateProfile("Bearer " + jwtToken, updatedUser);
            // Debugging log to print the API call request headers and body
            Log.d("UpdatedAPI Request", "Authorization: Bearer " + jwtToken);
            Log.d("API Request", "User ID: " + updatedUser.getId());
            Log.d("API Request", "Updated " + fieldName + ": " + updatedValue);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        // Successfully updated, update the currentUser object and the UI
                        Toast.makeText(ProfileActivity.this, fieldName + " updated successfully.", Toast.LENGTH_SHORT).show();
                        updateUI(fieldName, updatedValue); // Update only the selected field on the UI

                        // Call fetchUserDetails to refresh the data
                        fetchUserDetails(jwtToken, userId);  // Refresh the entire profile with updated data
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update " + fieldName, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "JWT Token is missing or invalid.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(String fieldName, String updatedValue) {
        // Update the UI with the updated value for the selected field
        switch (fieldName) {
            case "Donor Name":
                tvDonorName.setText(updatedValue);
                break;
            case "Email":
                tvEmail.setText(updatedValue);
                break;
            case "Address":
                tvAddress.setText(updatedValue);
                break;
            case "City":
                tvCity.setText(updatedValue);
                break;
            case "State":
                tvState.setText(updatedValue);
                break;
        }

    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}



