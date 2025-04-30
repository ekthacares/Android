package com.example.ekthacares;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ekthacares.model.ApiResponse;
import com.example.ekthacares.model.BloodDonation;
import com.example.ekthacares.model.DonationResponse;
import com.example.ekthacares.model.SentEmail;
import com.example.ekthacares.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ViewProfileActivity extends AppCompatActivity {


    private User currentUser;

    private ImageView imgProfile, editProfile, imgLogout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        imgProfile = findViewById(R.id.imgProfile);
        editProfile = findViewById(R.id.editProfile);
        imgLogout = findViewById(R.id.imgLogout);

        // Back arrow
        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            // Use OnBackPressedDispatcher to go back to the previous activity
            getOnBackPressedDispatcher().onBackPressed();
        });

        imgLogout = findViewById(R.id.imgLogout);
        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        editProfile = findViewById(R.id.editProfile);
        editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }
        @Override
        protected void onResume () {
            super.onResume();
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

        }


    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(ViewProfileActivity.this)
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, id) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    redirectToLogin();
                })
                .setNegativeButton("No", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Set your desired colors here
            positive.setTextColor(ContextCompat.getColor(this, R.color.red)); // e.g. green
            negative.setTextColor(ContextCompat.getColor(this, R.color.gray1)); // e.g. red
        });

        dialog.show();
    }


    private void showUserProfile(User currentUser) {
        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(currentUser.getProfileImage(), Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                // Crop the profile image to a circle
                Bitmap circularImage = getCircularBitmap(decodedImage);

                imgProfile.setImageBitmap(circularImage);
            } catch (Exception e) {
                e.printStackTrace();
                Bitmap fallbackImage = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
                Bitmap circularFallbackImage = getCircularBitmap(fallbackImage); // Apply circle cropping to fallback image
                imgProfile.setImageBitmap(circularFallbackImage); // Fallback image as a circle
            }
        } else {
            Bitmap fallbackImage = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
            Bitmap circularFallbackImage = getCircularBitmap(fallbackImage); // Apply circle cropping to fallback image
            imgProfile.setImageBitmap(circularFallbackImage); // Fallback image as a circle
        }
    }


    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int diameter = Math.min(width, height);

        // Create a square bitmap to make the circle
        Bitmap squareBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(squareBitmap);

        // Set the paint for drawing
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // Draw a circle on the canvas
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);

        // Set PorterDuff mode to apply the original bitmap within the circle
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Scale the original bitmap to fit within the circle
        canvas.drawBitmap(bitmap, new Rect(0, 0, width, height), new Rect(0, 0, diameter, diameter), paint);

        return squareBitmap;
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
                    TextView donorNameTextView = findViewById(R.id.donor_name);
                    TextView bloodtypeTextView = findViewById(R.id.bloodtype);
                    TextView locationTextView = findViewById(R.id.location);

                    donorNameTextView.setText(currentUser.getDonorName());
                    bloodtypeTextView.setText(currentUser.getBloodGroup());
                    locationTextView.setText(currentUser.getAddress() + ", " + currentUser.getCity());

                    LinearLayout layoutCall = findViewById(R.id.layoutCall);
                    layoutCall.setOnClickListener(v -> {
                        String phoneNumber = currentUser.getMobile();
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:+91" + phoneNumber));
                        startActivity(intent);
                    });

                    fetchDonations();
                    fetchSentEmails();
                    showUserProfile(currentUser);

                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ViewProfileActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
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


    private void fetchDonations() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

        if (jwtToken == null || currentUser == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<DonationResponse> call = apiService.getDonations("Bearer " + jwtToken, currentUser.getId());

        call.enqueue(new Callback<DonationResponse>() {
            @Override
            public void onResponse(Call<DonationResponse> call, Response<DonationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BloodDonation> donations = response.body().getDonations();

                    TextView donorStatusTextView = findViewById(R.id.donor_status);
                    TextView donatedcountTextView = findViewById(R.id.donatedcount);

                    if (donations != null && !donations.isEmpty()) {
                        donorStatusTextView.setText("Not Eligible to Donate");
                        donatedcountTextView.setText(String.valueOf(donations.size()));
                    } else {
                        donorStatusTextView.setText("Eligible to Donate");
                        donatedcountTextView.setText("0");
                    }
                } else {
                    Toast.makeText(ViewProfileActivity.this, "Failed to load donations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationResponse> call, Throwable t) {
                Log.e("DonorProfileActivity", "Error fetching donations", t);
                Toast.makeText(ViewProfileActivity.this, "Error loading donations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSentEmails() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

        if (jwtToken == null || currentUser == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<SentEmail>> call = apiService.getSentEmails("Bearer " + jwtToken, currentUser.getId());

        call.enqueue(new Callback<List<SentEmail>>() {
            @Override
            public void onResponse(Call<List<SentEmail>> call, Response<List<SentEmail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TextView requestedCountTextView = findViewById(R.id.RequestedCount);
                    requestedCountTextView.setText(String.valueOf(response.body().size()));
                } else {
                    Toast.makeText(ViewProfileActivity.this, "Failed to load sent requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SentEmail>> call, Throwable t) {
                Log.e("DonorProfileActivity", "Error fetching sent emails", t);
                Toast.makeText(ViewProfileActivity.this, "Error loading sent emails", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void redirectToLogin() {
        Intent intent = new Intent(ViewProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
