package com.example.ekthacares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;
    private TextView tvMessage;
    private String mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvMessage = findViewById(R.id.tvMessage);

        // Retrieve the mobile number passed from MainActivity
        mobile = getIntent().getStringExtra("mobile");

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();

            // Validate OTP input
            if (!TextUtils.isEmpty(otp)) {
                verifyOtp(mobile, otp);
            } else {
                tvMessage.setText("Please enter the OTP.");
            }
        });
    }

    private void verifyOtp(String mobile, String otp) {
        String url = Constants.BASE_URL + "/api/app/validateOtp";  // Ensure the URL is correct

        // Create the POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log the raw response to see what you are getting from the server
                        System.out.println("Server Response: " + response);

                        try {
                            // Check if the response starts with <!DOCTYPE, which indicates HTML error page
                            if (response.startsWith("<!DOCTYPE")) {
                                tvMessage.setText("Error: Unexpected server response.");
                                return;
                            }

                            // Parse the JSON response
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if ("success".equals(status)) {
                                // Extract user details from the response
                                String donorName = jsonObject.getString("donorname");
                                Long userId = jsonObject.getLong("userId");  // Parse userId as Long
                                String sessionToken = jsonObject.getString("sessionToken");
                                String jwtToken = jsonObject.getString("jwtToken");

                                // Save user details in SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong("userId", userId);  // Store userId as Long
                                editor.putString("donorName", donorName);
                                editor.putString("sessionToken", sessionToken);
                                editor.putString(Constants.JWT_TOKEN_KEY, jwtToken);
                                editor.apply();

                                Toast.makeText(OtpActivity.this, message, Toast.LENGTH_SHORT).show();

                                // Navigate to DonorHomeActivity after successful validation
                                new Handler().postDelayed(() -> {
                                    Intent intent = new Intent(OtpActivity.this, DonorHomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }, 2000);
                            } else {
                                tvMessage.setText(message);
                            }
                        } catch (Exception e) {
                            tvMessage.setText("Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Log and display error response
                        if (error.networkResponse != null) {
                            String errorMessage = new String(error.networkResponse.data);
                            tvMessage.setText("Error: " + errorMessage);
                        } else {
                            tvMessage.setText("Error: " + error.getMessage());
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Prepare parameters for the request
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("otp", otp);
                params.put("source", "app"); // To differentiate app requests
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }
}
