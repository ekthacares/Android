package com.example.ekthacares;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.Constants;
import com.example.ekthacares.R;
import com.example.ekthacares.DonationAdapter;
import com.example.ekthacares.model.BloodDonation;
import com.example.ekthacares.model.DonationResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyDonationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DonationAdapter donationAdapter;
    private TextView noDonationsMessage,totalDonationsCount;
    private Button btnAddDonation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_donations1);

        recyclerView = findViewById(R.id.recyclerViewDonations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noDonationsMessage = findViewById(R.id.noDonationsMessage);
        totalDonationsCount = findViewById(R.id.totalDonationsCount);
        btnAddDonation = findViewById(R.id.btnAddDonation);
        Log.d(TAG, "onCreate: Activity started");

        // Back arrow
        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            // Use OnBackPressedDispatcher to go back to the previous activity
            getOnBackPressedDispatcher().onBackPressed();
        });

        btnAddDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDonationDialog();
            }
        });

        fetchDonations();
    }

    private void fetchDonations() {
        Log.d(TAG, "fetchDonations: Fetching donations...");

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        Log.d(TAG, "fetchDonations: Retrieved JWT Token = " + jwtToken);
        Log.d(TAG, "fetchDonations: Retrieved User ID = " + userId);

        if (jwtToken == null || userId == -1) {
            Toast.makeText(MyDonationsActivity.this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorizationHeader = "Bearer " + jwtToken;
        Log.d(TAG, "fetchDonations: Authorization Header = " + authorizationHeader);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<DonationResponse> call = apiService.getDonations(authorizationHeader, userId);

        call.enqueue(new Callback<DonationResponse>() {
            @Override
            public void onResponse(Call<DonationResponse> call, Response<DonationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BloodDonation> donations = response.body().getDonations();

                    if (donations != null && !donations.isEmpty()) {
                        Log.d(TAG, "onResponse: API call successful. Donations retrieved: " + donations.size());
                        donationAdapter = new DonationAdapter(donations);
                        recyclerView.setAdapter(donationAdapter);

                        totalDonationsCount.setText("Total Donations: " + donations.size());
                        noDonationsMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        btnAddDonation.setVisibility(View.GONE);
                    } else {
                        Log.e(TAG, "onResponse: No donations found.");
                        //Toast.makeText(MyDonationsActivity.this, "No donations available", Toast.LENGTH_SHORT).show();

                        noDonationsMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        totalDonationsCount.setVisibility(View.GONE);
                        btnAddDonation.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "onResponse: Failed to load donations. Response code: " + response.code());
                    Toast.makeText(MyDonationsActivity.this, "Failed to load donations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: API call failed", t);
                Toast.makeText(MyDonationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDonationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_donation);

        TextView txtLastDonationDate = dialog.findViewById(R.id.txtLastDonationDate);
        EditText edtHospitalName = dialog.findViewById(R.id.edtHospitalName);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        txtLastDonationDate.setOnClickListener(v -> {
            // Open DatePickerDialog
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MyDonationsActivity.this,
                    (picker, selectedYear, selectedMonth, selectedDay) -> {
                        // Format the selected date as "yyyy-MM-ddT00:00:00"
                        String formattedDate = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDay, 0, 0)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        txtLastDonationDate.setText(formattedDate); // Display the formatted date
                    },
                    year, month, day
            );

            // Restrict to dates up to today
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            datePickerDialog.show();
        });

        btnSubmit.setOnClickListener(v -> {
            String lastDonationDate = txtLastDonationDate.getText().toString();
            String hospitalName = edtHospitalName.getText().toString();

            if (lastDonationDate.isEmpty() || hospitalName.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            addDonation(lastDonationDate, hospitalName, dialog);
        });

        dialog.show();
    }

    private void addDonation(String lastDonationDate, String hospitalName, Dialog dialog) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken == null || userId == -1) {
            Toast.makeText(MyDonationsActivity.this, "User is not logged in", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        // Convert lastDonationDate (String) to LocalDateTime if it's in ISO format
        LocalDateTime localDateTime = LocalDateTime.parse(lastDonationDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Send the donation details directly as part of the request body
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Create the request with @FormUrlEncoded parameters
        Call<Map<String, Object>> call = apiService.addDonation(userId, lastDonationDate, hospitalName);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MyDonationsActivity.this, "Donation added successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    fetchDonations(); // Refresh donations list
                } else {
                    Toast.makeText(MyDonationsActivity.this, "Failed to add donation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(MyDonationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
