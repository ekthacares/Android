package com.example.ekthacares;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.ekthacares.model.BloodSearchResponse;
import com.example.ekthacares.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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



        EditText editTextRequestedDate = dialogView.findViewById(R.id.editTextRequestedDate);
        ViewFlipper viewFlipper = dialogView.findViewById(R.id.viewFlipper);
        Button buttonToCity = dialogView.findViewById(R.id.buttonToCity);
        Button buttonToResults = dialogView.findViewById(R.id.buttonToResults);
        Button buttonSearch = dialogView.findViewById(R.id.buttonSearch);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        EditText editTextCity = dialogView.findViewById(R.id.editTextCity);
        EditText editTextState = dialogView.findViewById(R.id.editTextState);
        EditText editTextHospital = dialogView.findViewById(R.id.editTextHospital);
        CheckBox checkboxConsent = dialogView.findViewById(R.id.checkboxConsent);

        GridLayout bloodGroupGrid = dialogView.findViewById(R.id.bloodGroupGrid);
        final String[] selectedBloodGroup = {""};
        final String[] requestedDate = {""}; // Store selected date


        // Blood group selection
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

        Spinner spinnerOptions = dialogView.findViewById(R.id.spinnerOptions);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Blood Userd For", "Option 1", "Option 2", "Option 3"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(adapter);


        // Disable the button initially
        buttonSearch.setEnabled(false);

        // Optional: Check if it's working
        checkboxConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonSearch.setEnabled(isChecked);
        });

        // Date picker
        editTextRequestedDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            calendar.set(year, month, dayOfMonth);
            long currentDateMillis = calendar.getTimeInMillis();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year1, month1, dayOfMonth1) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year1, month1, dayOfMonth1);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String date = dateFormat.format(selectedDate.getTime());
                        editTextRequestedDate.setText(date);
                        requestedDate[0] = date;
                    },
                    year, month, dayOfMonth);

            datePickerDialog.getDatePicker().setMinDate(currentDateMillis);
            datePickerDialog.show();
        });

        // Move to city/state input
        buttonToCity.setOnClickListener(v -> {
            if (selectedBloodGroup[0].isEmpty()) {
                Toast.makeText(requireContext(), "Please select a blood group", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext();
            }
        });

        // Move to search screen
        buttonToResults.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            String state = editTextState.getText().toString().trim();

            if (city.isEmpty() || state.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both city and state", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext();
            }
        });



        // Search and API call
        buttonSearch.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            String state = editTextState.getText().toString().trim();
            String hospital = editTextHospital.getText().toString().trim();

            if (selectedBloodGroup[0].isEmpty() || city.isEmpty() || state.isEmpty() || requestedDate[0].isEmpty()) {
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

                Call<BloodSearchResponse> call = apiService.searchForBlood(
                        headers,
                        selectedBloodGroup[0],
                        city,
                        state,
                        hospital,
                        requestedDate[0]
                );

                call.enqueue(new Callback<BloodSearchResponse>() {
                    @Override
                    public void onResponse(Call<BloodSearchResponse> call, Response<BloodSearchResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<User> users = response.body().getResults();
                            if (users != null && !users.isEmpty()) {
                                int donorCount = users.size();
                                Intent intent = new Intent(requireContext(), SearchResultsActivity.class);
                                intent.putExtra("donor_list", new ArrayList<>(users));
                                intent.putExtra("blood_group", selectedBloodGroup[0]);
                                intent.putExtra("donor_count", donorCount);
                                startActivity(intent);
                                dismiss();
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
