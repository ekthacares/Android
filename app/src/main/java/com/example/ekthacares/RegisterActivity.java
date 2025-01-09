package com.example.ekthacares;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etDonorName, etMobile, etEmailId, etDateOfBirth, etBloodGroup, etAge, etAddress, etCity, etState;
    private RadioGroup rgGender;
    private Button btnSubmit;
    private ProgressDialog progressDialog;

    private boolean isSubmitting = false; // Flag to prevent multiple submissions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etDonorName = findViewById(R.id.etDonorName);
        etMobile = findViewById(R.id.etMobile);
        etEmailId = findViewById(R.id.etEmailId);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etAge = findViewById(R.id.etAge);
        rgGender = findViewById(R.id.rgGender);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Get the mobile number from the intent
        String mobile = getIntent().getStringExtra("mobile");

        // Set the mobile number in the EditText field
        etMobile.setText(mobile);
        etMobile.setEnabled(false); // Set as non-editable
        etMobile.setFocusable(true); // Prevent focusing

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering... Please wait");
        progressDialog.setCancelable(false);

        // Submit button click listener
        btnSubmit.setOnClickListener(v -> {
            if (isSubmitting) return; // Prevent multiple submissions
            isSubmitting = true; // Set flag to prevent further submissions

            String donorname = etDonorName.getText().toString().trim();
            String mobileNum = etMobile.getText().toString().trim();
            String emailid = etEmailId.getText().toString().trim();
            String dateofbirth = etDateOfBirth.getText().toString().trim();
            String bloodgroup = etBloodGroup.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String state = etState.getText().toString().trim();

            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            RadioButton selectedGender = findViewById(selectedGenderId);
            String gender = selectedGender != null ? selectedGender.getText().toString() : "";

            // Validate fields
            if (donorname.isEmpty() || mobileNum.isEmpty() || emailid.isEmpty() || dateofbirth.isEmpty() ||
                    bloodgroup.isEmpty() || age.isEmpty() || gender.isEmpty() || address.isEmpty() ||
                    city.isEmpty() || state.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                isSubmitting = false; // Reset flag on validation failure
            } else {
                registerUser(donorname, mobileNum, emailid, dateofbirth, bloodgroup, Integer.parseInt(age), gender, address, city, state);
            }

            // Reset flag with a small delay (optional, for UI responsiveness)
            new Handler().postDelayed(() -> isSubmitting = false, 500); // Adjust the delay if necessary
        });
    }

    private void registerUser(String donorname, String mobile, String emailid, String dateofbirth, String bloodgroup,
                              int age, String gender, String address, String city, String state) {
        String url = Constants.BASE_URL + "/api/app/register"; // Ensure the endpoint is correct

        // Log mobile number before sending
        Log.d("RegisterActivity", "Mobile number being sent: " + mobile);

        // Create JSON payload
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("donorname", donorname);
            jsonBody.put("mobile", mobile);
            jsonBody.put("emailid", emailid);
            jsonBody.put("dateofbirth", dateofbirth);  // Use formatted date
            jsonBody.put("bloodgroup", bloodgroup);
            jsonBody.put("age", age);
            jsonBody.put("gender", gender);
            jsonBody.put("address", address);
            jsonBody.put("city", city);
            jsonBody.put("state", state);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(RegisterActivity.this, "Error creating JSON data.", Toast.LENGTH_SHORT).show();
            isSubmitting = false; // Reset flag on failure
            return;
        }

        // Show Progress Dialog
        progressDialog.show();
        btnSubmit.setEnabled(false); // Disable the button during the request

        // Create the request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    progressDialog.dismiss();
                    btnSubmit.setEnabled(true); // Re-enable the button after the response
                    isSubmitting = false; // Reset flag after response

                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                            // Redirect to OTP verification screen
                            Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                            intent.putExtra("mobile", mobile);  // Pass mobile number for OTP verification
                            startActivity(intent);
                            finish();  // Close the RegisterActivity
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Error parsing server response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    btnSubmit.setEnabled(true); // Re-enable the button on error
                    isSubmitting = false; // Reset flag on error

                    String errorMessage = "Error occurred. Please try again.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorData = new String(error.networkResponse.data);
                            JSONObject errorJson = new JSONObject(errorData);
                            if (errorJson.has("message")) {
                                errorMessage = errorJson.getString("message");

                                // Check specifically for duplicate mobile number error
                                if (errorMessage.contains("Mobile number already exists")) {
                                    errorMessage = "This mobile number is already registered. Please use a different one.";
                                }
                            }
                        } catch (Exception e) {
                            errorMessage = "Error parsing error response.";
                        }
                    }

                    // Log the error message
                    Log.e("RegisterActivity", "Error: " + errorMessage);
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); // Specify JSON content type
                return headers;
            }
        };

        // Set retry policy for the request
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, // Timeout in milliseconds
                1, // Number of retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
