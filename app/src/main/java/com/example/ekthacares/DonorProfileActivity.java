package com.example.ekthacares;

import android.content.Intent;
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
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.ApiResponse;
import com.example.ekthacares.model.BloodDonation;
import com.example.ekthacares.model.DonationResponse;
import com.example.ekthacares.model.SentEmail;
import com.example.ekthacares.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonorProfileActivity extends AppCompatActivity {

    private User user; // User passed from SearchResultsActivity

    private ImageView imgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_profile);

        imgProfile = findViewById(R.id.imgProfile);

        // Receive user from intent
        user = (User) getIntent().getSerializableExtra("donor");

        // Set basic user info from passed object
        if (user != null) {
            TextView donorNameTextView = findViewById(R.id.donor_name);
            TextView bloodtypeTextView = findViewById(R.id.bloodtype);
            TextView locationTextView = findViewById(R.id.location);

            donorNameTextView.setText(user.getDonorName());
            bloodtypeTextView.setText(user.getBloodGroup());
            locationTextView.setText(user.getAddress() + ", " + user.getCity());

            LinearLayout layoutCall = findViewById(R.id.layoutCall);
            layoutCall.setOnClickListener(v -> {
                String phoneNumber = user.getMobile();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+91" + phoneNumber));
                startActivity(intent);
            });

            fetchDonations();
            fetchSentEmails();
            showUserProfile(user);
        } else {
            Toast.makeText(this, "No user data received.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Back arrow
        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            // Use OnBackPressedDispatcher to go back to the previous activity
            getOnBackPressedDispatcher().onBackPressed();
        });


    }

    private void showUserProfile(User user) {
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(user.getProfileImage(), Base64.DEFAULT);
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
            imgProfile.setImageBitmap(fallbackImage); // Fallback image as a circle
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


    private void fetchDonations() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

        if (jwtToken == null || user == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<DonationResponse> call = apiService.getDonations("Bearer " + jwtToken, user.getId());

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
                    Toast.makeText(DonorProfileActivity.this, "Failed to load donations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationResponse> call, Throwable t) {
                Log.e("DonorProfileActivity", "Error fetching donations", t);
                Toast.makeText(DonorProfileActivity.this, "Error loading donations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSentEmails() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

        if (jwtToken == null || user == null) return;

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<SentEmail>> call = apiService.getSentEmails("Bearer " + jwtToken, user.getId());

        call.enqueue(new Callback<List<SentEmail>>() {
            @Override
            public void onResponse(Call<List<SentEmail>> call, Response<List<SentEmail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TextView requestedCountTextView = findViewById(R.id.RequestedCount);
                    requestedCountTextView.setText(String.valueOf(response.body().size()));
                } else {
                    Toast.makeText(DonorProfileActivity.this, "Failed to load sent requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SentEmail>> call, Throwable t) {
                Log.e("DonorProfileActivity", "Error fetching sent emails", t);
                Toast.makeText(DonorProfileActivity.this, "Error loading sent emails", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
