package com.example.ekthacares;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

        // Show header only once
        TextView resultNameTextView = new TextView(this);
        TextView bloodGroupTextView = findViewById(R.id.result_name);

        if (bloodGroup != null) {
            String resultText = "Search Results for: " + bloodGroup;
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

                TextView donorNameTextView = (TextView) getLayoutInflater().inflate(R.layout.item_donor_result, layoutSearchResults, false);

                donorNameTextView.setText(user.getDonorName());

                // Apply margin programmatically
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) donorNameTextView.getLayoutParams();
                params.setMargins(0, 0, 0, 8); // Apply margin (left, top, right, bottom)
                donorNameTextView.setLayoutParams(params);


                layoutSearchResults.addView(donorNameTextView);
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
