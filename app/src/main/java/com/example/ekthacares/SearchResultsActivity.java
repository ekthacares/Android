package com.example.ekthacares;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ekthacares.model.User;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    private LinearLayout layoutSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        layoutSearchResults = findViewById(R.id.layoutSearchResults);


        String bloodGroup = getIntent().getStringExtra("blood_group");
        ArrayList<User> users = (ArrayList<User>) getIntent().getSerializableExtra("donor_list");
        int donorCount = getIntent().getIntExtra("donor_count", 0);


        // Back arrow
        ImageView backArrow = findViewById(R.id.imgBackArrow);
        backArrow.setOnClickListener(v -> {
            // Use OnBackPressedDispatcher to go back to the previous activity
            getOnBackPressedDispatcher().onBackPressed();
        });

        // Show header only once
        TextView resultNameTextView = new TextView(this);
        TextView bloodGroupTextView = findViewById(R.id.result_name);

        if (bloodGroup != null) {
            String resultText = donorCount + " result(s) found for blood type : " +  bloodGroup;
            int start = resultText.indexOf(bloodGroup);
            int end = start + bloodGroup.length();
            SpannableString spannable = new SpannableString(resultText);
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.red)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            bloodGroupTextView.setText(spannable);

        } else {
            resultNameTextView.setText("No Search Results");
        }

        layoutSearchResults.addView(resultNameTextView);

        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                View donorItemView = getLayoutInflater().inflate(R.layout.item_donor_result, layoutSearchResults, false);

                TextView donorNameTextView = donorItemView.findViewById(R.id.textViewDonorName);
                donorNameTextView.setText(user.getDonorName());

                ImageView imageInfo = donorItemView.findViewById(R.id.imageinfo);
                ImageView imageCall = donorItemView.findViewById(R.id.imagecall);

                // Info Click - open profile or show message
                imageInfo.setOnClickListener(v -> {
                    // TODO: Start DonorProfileActivity and pass donor data (example below)
                    Intent intent = new Intent(SearchResultsActivity.this, DonorProfileActivity.class);
                    intent.putExtra("donor", user); // User must implement Serializable or Parcelable
                    startActivity(intent);
                });

                // Call Click - open dialer
                imageCall.setOnClickListener(v -> {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:+91" + user.getMobile()));
                    startActivity(callIntent);
                });

                layoutSearchResults.addView(donorItemView);
            }

        } else {
            TextView noResults = new TextView(this);
            noResults.setText("No donors found.");
            noResults.setTextSize(18);
            noResults.setTextColor(ContextCompat.getColor(this, R.color.black));

            layoutSearchResults.addView(noResults);
        }
    }
}
