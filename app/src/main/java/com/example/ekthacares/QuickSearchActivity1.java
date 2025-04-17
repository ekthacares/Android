package com.example.ekthacares;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ekthacares.ApiService;
import com.example.ekthacares.RetrofitClient;
import com.example.ekthacares.model.BloodSearchResponse;
import com.example.ekthacares.model.User;
import com.example.ekthacares.Constants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuickSearchActivity1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showQuickSearchDialog(); // Open the popup directly
    }

    private void showQuickSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_quick_search_popup, null);
        builder.setView(dialogView);
        builder.setCancelable(false); // prevent dismissal by clicking outside

        AlertDialog dialog = builder.create();
        dialog.show();

        ViewFlipper viewFlipper = dialogView.findViewById(R.id.viewFlipper);
        Button buttonToCity = dialogView.findViewById(R.id.buttonToCity);
        Button buttonToState = dialogView.findViewById(R.id.buttonToState);
        Button buttonSearch = dialogView.findViewById(R.id.buttonSearch);
        TextView textViewResults = dialogView.findViewById(R.id.textViewResults);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        EditText editTextCity = dialogView.findViewById(R.id.editTextCity);
        EditText editTextState = dialogView.findViewById(R.id.editTextState);
        EditText editTextHospital = dialogView.findViewById(R.id.editTextHospital);

        final String[] selectedBloodGroup = {""};
        GridLayout bloodGroupGrid = dialogView.findViewById(R.id.bloodGroupGrid);

        for (int i = 0; i < bloodGroupGrid.getChildCount(); i++) {
            View child = bloodGroupGrid.getChildAt(i);
            if (child instanceof Button) {
                Button bgButton = (Button) child;
                bgButton.setOnClickListener(v -> {
                    selectedBloodGroup[0] = bgButton.getText().toString();
                    Toast.makeText(this, "Selected: " + selectedBloodGroup[0], Toast.LENGTH_SHORT).show();
                });
            }
        }

        buttonToCity.setOnClickListener(v -> {
            if (selectedBloodGroup[0].isEmpty()) {
                Toast.makeText(this, "Please select a blood group", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext();
            }
        });

        buttonToState.setOnClickListener(v -> {
            if (editTextCity.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter City", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext();
            }
        });

        buttonSearch.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            String state = editTextState.getText().toString().trim();
            String hospital = editTextHospital.getText().toString().trim();

            if (selectedBloodGroup[0].isEmpty() || city.isEmpty() || state.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            textViewResults.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
            String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
            Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

            if (jwtToken != null && userId != -1) {
                ApiService apiService = RetrofitClient.getApiService();
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken);
                headers.put("userId", String.valueOf(userId));

                Call<BloodSearchResponse> call = apiService.searchForBlood(
                        headers, selectedBloodGroup[0], city, state, hospital);

                call.enqueue(new Callback<BloodSearchResponse>() {
                    @Override
                    public void onResponse(Call<BloodSearchResponse> call, Response<BloodSearchResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<User> users = response.body().getResults();
                            if (users != null && !users.isEmpty()) {
                                StringBuilder result = new StringBuilder("Found " + users.size() + " donor(s):\n\n");
                                for (User user : users) {
                                    result.append("Name: ").append(user.getDonorName())
                                            .append("\nBlood Group: ").append(user.getBloodGroup())
                                            .append("\nEmail: ").append(user.getEmailId())
                                            .append("\nCity: ").append(user.getCity())
                                            .append("\nContact: ").append(user.getMobile())
                                            .append("\n\n");
                                }
                                textViewResults.setText(result.toString());
                            } else {
                                textViewResults.setText("No donors found.");
                            }
                        } else {
                            textViewResults.setText("Error. Try again.");
                        }
                        textViewResults.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<BloodSearchResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        textViewResults.setVisibility(View.VISIBLE);
                        textViewResults.setText("Failed. Check your internet connection.");
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                textViewResults.setVisibility(View.VISIBLE);
                textViewResults.setText("User not logged in.");
            }
        });
    }
}
