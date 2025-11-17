package com.example.ekthacares;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.Campaign;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CampaignActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CampaignAdapter campaignAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchCampaigns();

        // Back arrow
        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            // Use OnBackPressedDispatcher to go back to the previous activity
            getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void fetchCampaigns() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Campaign>> call = apiService.getCampaigns();

        call.enqueue(new Callback<List<Campaign>>() {
            @Override
            public void onResponse(Call<List<Campaign>> call, Response<List<Campaign>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // Set the adapter for RecyclerView
//                    campaignAdapter = new CampaignAdapter(response.body());
                    campaignAdapter = new CampaignAdapter(CampaignActivity.this, response.body());
                    recyclerView.setAdapter(campaignAdapter);
                    TextView totalCampaignCountTextView = findViewById(R.id.campaignCount);
                    String totalCampaignText = "Total Campaigns: " + campaignAdapter.getItemCount();
                    SpannableString spannableString = new SpannableString(totalCampaignText);
                    int start = totalCampaignText.indexOf(String.valueOf(campaignAdapter.getItemCount()));
                    int end = start + String.valueOf(campaignAdapter.getItemCount()).length();
                    spannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    totalCampaignCountTextView.setText(spannableString);

                } else {
                    Toast.makeText(CampaignActivity.this, "Failed to load campaigns", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Campaign>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Error fetching campaigns", t);
                Toast.makeText(CampaignActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
