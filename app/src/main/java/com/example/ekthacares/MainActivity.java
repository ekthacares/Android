package com.example.ekthacares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private EditText etMobile;
    private Button btnSendOtp;
    private TextView tvMessage;
    private boolean isOtpSent = false; // Flag to check if OTP has been sent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if JWT token is already available
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null); // Check for saved token

        if (jwtToken != null) {
            // Redirect the user to the home screen if already logged in
            Intent intent = new Intent(MainActivity.this, DonorHomeActivity.class);
            startActivity(intent);
            finish();  // Close the MainActivity so the user doesn't return to it
        }

        // Initialize views
        etMobile = findViewById(R.id.etMobile);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        tvMessage = findViewById(R.id.tvMessage);

        btnSendOtp.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();

            // Validate mobile number format
            if (!TextUtils.isEmpty(mobile) && mobile.length() == 10) {
                // Disable the button to prevent multiple clicks
                btnSendOtp.setEnabled(false);
                sendOtp(mobile);
            } else {
                tvMessage.setText("Please enter a valid 10-digit mobile number.");
            }
        });
    }

    private void sendOtp(String mobile) {
        // Prevent sending OTP if it's already in progress
        if (isOtpSent) {
            return;
        }

        isOtpSent = true; // Mark OTP request as in progress

        // Use the BASE_URL from Constants to send OTP
        String url = Constants.BASE_URL + "/api/app login"; // Correct endpoint for POST request

        // Create a StringRequest with POST method
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle successful OTP sending
                        // Assume you receive a JWT token here (modify according to your API response)
                        String jwtToken = response; // Example, replace with actual response handling

                        // Store the JWT token and user ID in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.JWT_TOKEN_KEY, jwtToken);  // Store JWT Token
                        editor.putString("mobile", mobile);  // Store the mobile number if required
                        editor.apply();  // Apply changes

                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(MainActivity.this, OtpActivity.class);
                            intent.putExtra("mobile", mobile);  // Pass mobile to the next activity
                            startActivity(intent);
                            finish();  // Optional: Finish the MainActivity to prevent returning to it
                            isOtpSent = false;  // Reset the flag after activity transition
                            btnSendOtp.setEnabled(true);  // Re-enable the button
                        }, 2000); // 2 seconds delay before starting next activity
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tvMessage.setText("Error: " + error.getMessage());
                        isOtpSent = false;  // Reset the flag in case of an error
                        btnSendOtp.setEnabled(true);  // Re-enable the button on error
                    }
                }) {
            // Override the getParams method to pass the mobile number as POST data
            @Override
            public java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("mobile", mobile); // Add mobile number to the POST parameters
                return params;
            }
        };

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
    }
}
