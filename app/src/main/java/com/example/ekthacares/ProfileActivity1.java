package com.example.ekthacares;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.ApiResponse;
import com.example.ekthacares.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity1 extends AppCompatActivity {

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private TextView tvUserId, tvDonorName, tvEmail, tvMobile, tvDateOfBirth, tvBloodGroup,
            tvAge, tvGender, tvAddress, tvCity, tvState, Donorname ;
    private ImageView imgEditDonorName, imgEditEmail, imgEditAddress, imgEditCity, imgEditState, imgProfile;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile1);

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
        Donorname = findViewById(R.id.donor_name);
        imgProfile= findViewById(R.id.imgProfile);

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

        imgProfile.setOnClickListener(view -> {
            String[] options = {"Take Photo", "Choose from Gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Option")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraLauncher.launch(takePicture);
                        } else {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryLauncher.launch(pickPhoto);
                        }
                    });
            builder.show();
        });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            String base64Image = convertBitmapToBase64(photo);
                            imgProfile.setImageBitmap(photo);
                            saveImageToDatabase(base64Image);
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri imageUri = data.getData();
                            Bitmap photo = convertUriToBitmap(imageUri);
                            String base64Image = convertBitmapToBase64(photo);  // Convert to base64
                            imgProfile.setImageURI(imageUri);
                            saveImageToDatabase(base64Image);  // Save base64 string to the database
                        }
                    }
                });


    }

    private Bitmap convertUriToBitmap(Uri imageUri) {
        try {
            // Open the input stream from the URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            // Decode the InputStream to a Bitmap
            return BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
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
                Toast.makeText(ProfileActivity1.this, "An error occurred.", Toast.LENGTH_SHORT).show();
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
        Donorname.setText(currentUser.getDonorName());
    }

    private void showEditDialog(String fieldName) {
        EditText editText = new EditText(ProfileActivity1.this);
        editText.setHint("Enter new " + fieldName);
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity1.this);
        builder.setTitle("Edit " + fieldName)
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedValue = editText.getText().toString();
                    if (!updatedValue.isEmpty()) {
                        updateUserProfile(updatedValue, fieldName);
                    } else {
                        Toast.makeText(ProfileActivity1.this, "Please enter a valid " + fieldName, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ProfileActivity1.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity1.this, "Update failed.", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateError", t.getMessage(), t);
                }
            });
        } else {
            Toast.makeText(ProfileActivity1.this, "Session invalid. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }


    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity1.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);  // Adjust the quality if needed
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveImageToDatabase(String base64Image) {
        User user = getUser();  // Get user object
        user.setProfileImage(base64Image);  // Assuming you have a method to set the profile image (base64 string)
        updateUserInDatabase(user);  // Method to update the user in the database
        Log.d("ProfileActivity1", "Image saved directly in DB");
    }

    private void updateUserInDatabase(User user) {
        // Assuming you have a Retrofit instance and a service for your API
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);


        // Call the update user endpoint
        // Make sure to include the JWT token in the Authorization header
        Call<User> call = apiService.updateUser(user.getId(), user);


        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // If the response is not null and is a String
                    try {
                        // Check if the body is a plain text response
                        String responseBody = response.errorBody() != null ? response.errorBody().string() : response.body().toString();

                        // Log the response body (if it's a message)
                        Log.d("ProfileActivity", "Response: " + responseBody);

                        // Check for successful message
                        if (responseBody.contains("User updated successfully")) {
                            Log.d("ProfileActivity", "User updated successfully.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle failure (e.g., response code not 200)
                    Log.d("ProfileActivity", "Failed to update user: " + response.code());
                }
            }


            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Handle network failure or other errors
                Log.d("ProfileActivity", "Error: " + t.getMessage());
            }
        });
    }


    private User getUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

        // Assuming you have a method to fetch user details via an API
        if (userId != -1 && jwtToken != null) {
            fetchUserDetails(jwtToken, userId);
        }

        // You may want to return a User object here if available
        return currentUser; // currentUser is populated when fetchUserDetails() is successful
    }



}
