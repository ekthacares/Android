package com.example.ekthacares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etMobile;
    private Button btnSendOtp, btnRegister; // Register button
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
        btnRegister = findViewById(R.id.btnRegister); // Register button
        tvMessage = findViewById(R.id.tvMessage);

        // Initially hide the Register button
        btnRegister.setVisibility(Button.GONE);

        // Send OTP button click listener
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

        // Register button click listener
        btnRegister.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            intent.putExtra("mobile", mobile);
            startActivity(intent);
        });

        // Set up the back press callback to refresh the activity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if JWT token is available (user is logged in)
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
                String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

                if (jwtToken != null) {
                    // If logged in, redirect to home
                    Intent intent = new Intent(MainActivity.this, DonorHomeActivity.class);
                    startActivity(intent);
                    finish();  // Close the MainActivity
                } else {
                    // Refresh the UI if the user is not logged in
                    etMobile.setText("");  // Clear the mobile number field
                    tvMessage.setText("");  // Clear any displayed message
                    btnSendOtp.setEnabled(true);  // Re-enable the "Send OTP" button
                    btnSendOtp.setVisibility(Button.VISIBLE);  // Ensure the "Send OTP" button is visible
                    btnRegister.setVisibility(Button.GONE);  // Hide the "Register" button
                }
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
                        }, 1000); // 2 seconds delay before starting next activity
                    }
                },
                error -> {
                    String errorMessage = "Something went wrong. Please try again.";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode; // HTTP status code
                        String errorData = new String(error.networkResponse.data); // Response body
                        errorMessage = "Error Code: " + statusCode + " | Response: " + errorData;

                        try {
                            JSONObject errorJson = new JSONObject(errorData);
                            if (errorJson.has("message")) {
                                errorMessage = errorJson.getString("message");
                                if ("User not found".equals(errorMessage)) {
                                    errorMessage = "Mobile number is not registered. Please register first.";
                                    showRegisterOption();
                                }
                            }
                        } catch (Exception e) {
                            errorMessage = "Unexpected error occurred. Response parsing failed.";
                        }
                    }
                    tvMessage.setText(errorMessage);

                        // Show the Register button if the mobile number is not found
                        showRegisterOption();

                        isOtpSent = false;  // Reset the flag in case of an error
                        btnSendOtp.setEnabled(true);  // Re-enable the button on error

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

    private void showRegisterOption() {
        // Show the Register button if the OTP request failed due to unregistered number
        btnSendOtp.setVisibility(Button.GONE);
        btnRegister.setVisibility(Button.VISIBLE);
    }
 }