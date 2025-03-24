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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etMobile;
    private Button btnSendOtp, btnRegister;
    private TextView tvMessage;
    private boolean isOtpSent = false; // Flag to track OTP sending status
    private Handler handler = new Handler();
    private Runnable otpRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if JWT token is already stored (user is logged in)
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        if (jwtToken != null) {
            startActivity(new Intent(MainActivity.this, DonorHomeActivity1.class));
            finish();
            return; // Exit onCreate if the user is already logged in
        }

        // Initialize UI elements
        etMobile = findViewById(R.id.etMobile);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnRegister = findViewById(R.id.btnRegister);
        tvMessage = findViewById(R.id.tvMessage);

        // Initially hide the Register button
        btnRegister.setVisibility(Button.GONE);

        // Send OTP button click listener
        btnSendOtp.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            if (!TextUtils.isEmpty(mobile) && mobile.length() == 10) {
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

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if user is logged in
                String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
                if (jwtToken != null) {
                    startActivity(new Intent(MainActivity.this, DonorHomeActivity1.class));
                    finish();
                } else {
                    // Refresh UI if user is not logged in
                    etMobile.setText("");
                    tvMessage.setText("");
                    btnSendOtp.setEnabled(true);
                    btnSendOtp.setVisibility(Button.VISIBLE);
                    btnRegister.setVisibility(Button.GONE);
                }
            }
        });
    }

    private void sendOtp(String mobile) {
        if (isOtpSent) return; // Prevent multiple OTP requests

        // Debounce OTP request
        if (otpRunnable != null) handler.removeCallbacks(otpRunnable);
        otpRunnable = () -> {
            isOtpSent = true;
            btnSendOtp.setEnabled(false);

            String url = Constants.BASE_URL + "/api/app login";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> handleOtpSuccess(response, mobile),
                    this::handleOtpError) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("mobile", mobile);
                    return params;
                }
            };

            // Set retry policy to avoid automatic retries
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000, // 5 seconds timeout
                    0,    // No retries
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // Add the request to Volley queue
            Volley.newRequestQueue(this).add(stringRequest);
        };

        handler.postDelayed(otpRunnable, 1000); // Debounce for 1 second
    }

    private void handleOtpSuccess(String response, String mobile) {
        // Store JWT token and mobile number
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.JWT_TOKEN_KEY, response);
        editor.putString("mobile", mobile);
        editor.apply();

        // Move to OTP verification activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, OtpActivity.class);
            intent.putExtra("mobile", mobile);
            startActivity(intent);
            finish();
        }, 1000);

        isOtpSent = false;
        btnSendOtp.setEnabled(true);
    }

    private void handleOtpError(VolleyError error) {
        String errorMessage = "Something went wrong. Please try again.";
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            String errorData = new String(error.networkResponse.data);
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

        // Reset OTP flag and re-enable button
        isOtpSent = false;
        btnSendOtp.setEnabled(true);
    }

    private void showRegisterOption() {
        btnSendOtp.setVisibility(Button.GONE);
        btnRegister.setVisibility(Button.VISIBLE);
    }
}