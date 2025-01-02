package com.example.ekthacares;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private boolean isOtpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        etMobile = findViewById(R.id.etMobile);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnRegister = findViewById(R.id.btnRegister); // Register button
        tvMessage = findViewById(R.id.tvMessage);

        // Initially hide the Register button
        btnRegister.setVisibility(Button.GONE);

        // OTP button click listener
        btnSendOtp.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();

            // Validate mobile number format
            if (mobile.length() == 10) {
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
    }

    private void sendOtp(String mobile) {
        if (isOtpSent) {
            return;
        }

        isOtpSent = true;

        String url = Constants.BASE_URL + "/api/app login";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            tvMessage.setText(jsonResponse.getString("message"));

                            // Navigate to OTP verification screen
                            Intent intent = new Intent(MainActivity.this, OtpActivity.class);
                            intent.putExtra("mobile", mobile);
                            startActivity(intent);
                            finish();
                        } else {
                            tvMessage.setText(jsonResponse.getString("message"));
                        }
                    } catch (Exception e) {
                        tvMessage.setText("Unexpected error occurred. Please try again.");
                    } finally {
                        btnSendOtp.setEnabled(true);
                        isOtpSent = false;
                    }
                },
                error -> {
                    String errorMessage = "Something went wrong. Please try again.";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorData = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorData);

                            if (errorJson.has("message")) {
                                errorMessage = errorJson.getString("message");

                                // Check specifically for "User not found"
                                if (errorMessage.equals("User not found")) {
                                    errorMessage = "Mobile number is not registered. Please register first.";
                                    showRegisterOption(); // Show Register button and hide Send OTP button
                                }
                            }
                        } catch (Exception e) {
                            errorMessage = "Unexpected error occurred. Please try again.";
                        }
                    }

                    tvMessage.setText(errorMessage);
                    btnSendOtp.setEnabled(true);
                    isOtpSent = false;
                }) {
            @Override
            public java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("mobile", mobile);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void showRegisterOption() {
        // Hide the Send OTP button and show the Register button
        btnSendOtp.setVisibility(Button.GONE);
        btnRegister.setVisibility(Button.VISIBLE);
    }
}
