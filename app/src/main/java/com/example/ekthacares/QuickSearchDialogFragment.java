package com.example.ekthacares;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.ekthacares.model.BloodSearchResponse;
import com.example.ekthacares.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuickSearchDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_quick_search_popup, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        ViewFlipper viewFlipper = dialogView.findViewById(R.id.viewFlipper);
        Button buttonToCity = dialogView.findViewById(R.id.buttonToCity);
        Button buttonSearch = dialogView.findViewById(R.id.buttonSearch);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        EditText editTextCity = dialogView.findViewById(R.id.editTextCity);
        EditText editTextState = dialogView.findViewById(R.id.editTextState);
        EditText editTextHospital = dialogView.findViewById(R.id.editTextHospital);

        GridLayout bloodGroupGrid = dialogView.findViewById(R.id.bloodGroupGrid);
        final String[] selectedBloodGroup = {""};

        for (int i = 0; i < bloodGroupGrid.getChildCount(); i++) {
            View child = bloodGroupGrid.getChildAt(i);
            if (child instanceof Button) {
                Button bgButton = (Button) child;

                bgButton.setOnClickListener(v -> {
                    for (int j = 0; j < bloodGroupGrid.getChildCount(); j++) {
                        View btnChild = bloodGroupGrid.getChildAt(j);
                        if (btnChild instanceof Button) {
                            btnChild.setBackgroundResource(android.R.color.transparent);
                            ((Button) btnChild).setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                        }
                    }

                    bgButton.setBackgroundResource(R.drawable.rounded_border);
                    bgButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                    selectedBloodGroup[0] = bgButton.getText().toString();
                    Toast.makeText(getContext(), "Selected: " + selectedBloodGroup[0], Toast.LENGTH_SHORT).show();
                });
            }
        }

        buttonToCity.setOnClickListener(v -> {
            if (selectedBloodGroup[0].isEmpty()) {
                Toast.makeText(requireContext(), "Please select a blood group", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext();
            }
        });

        buttonSearch.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            String state = editTextState.getText().toString().trim();
            String hospital = editTextHospital.getText().toString().trim();

            if (selectedBloodGroup[0].isEmpty() || city.isEmpty() || state.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            Context context = requireContext();
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
            Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

            if (jwtToken != null && userId != -1) {
                ApiService apiService = RetrofitClient.getApiService();
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken);
                headers.put("userId", String.valueOf(userId));

                Call<BloodSearchResponse> call = apiService.searchForBlood(headers, selectedBloodGroup[0], city, state, hospital);
                call.enqueue(new Callback<BloodSearchResponse>() {
                    @Override
                    public void onResponse(Call<BloodSearchResponse> call, Response<BloodSearchResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<User> users = response.body().getResults();
                            if (users != null && !users.isEmpty()) {
                                Intent intent = new Intent(requireContext(), SearchResultsActivity.class);
                                intent.putExtra("donor_list", new ArrayList<>(users));
                                intent.putExtra("blood_group", selectedBloodGroup[0]);
                                startActivity(intent);
                                dismiss(); // Dismiss dialog after success
                            } else {
                                Toast.makeText(requireContext(), "No donors found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Error occurred. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BloodSearchResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed. Check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();
    }
}