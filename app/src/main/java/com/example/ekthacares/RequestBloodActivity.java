package com.example.ekthacares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ekthacares.model.BloodRequest;
import com.example.ekthacares.model.BloodRequestResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestBloodActivity extends AppCompatActivity {

    private EditText etName, etBloodGroup, etMobileNumber, etCity, etState;
    private Button btnSubmitRequest;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_blood);

        // Initialize views
        etName = findViewById(R.id.etName);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);


        // Back arrow
        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            // Use OnBackPressedDispatcher to go back to the previous activity
            getOnBackPressedDispatcher().onBackPressed();
        });

        // Initialize Retrofit service
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Get JWT token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);

        btnSubmitRequest.setOnClickListener(v -> {
            // Create BloodRequest object
            BloodRequest request = new BloodRequest(
                    etName.getText().toString(),
                    etBloodGroup.getText().toString(),
                    etMobileNumber.getText().toString(),
                    etCity.getText().toString(),
                    etState.getText().toString()
            );

            // Send API request
            sendBloodRequest(jwtToken, request);
        });
    }

    private void sendBloodRequest(String jwtToken, BloodRequest request) {
        apiService.requestBlood("Bearer " + jwtToken, request).enqueue(new Callback<BloodRequestResponse>() {
            @Override
            public void onResponse(Call<BloodRequestResponse> call, Response<BloodRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RequestBloodActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RequestBloodActivity.this, DonorHomeActivity1.class));
                    finish();
                } else {
                    Toast.makeText(RequestBloodActivity.this, "Failed to submit request!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BloodRequestResponse> call, Throwable t) {
                Toast.makeText(RequestBloodActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}
