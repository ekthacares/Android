package com.example.ekthacares;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class InviteActivity extends AppCompatActivity {

    String appShareLink = "https://play.google.com/store/apps/details?id=com.example.ekthacares"; // Change if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        Button shareBtn = findViewById(R.id.btnShareApp); // Your button ID
        shareBtn.setOnClickListener(v -> shareAppLink());
    }

    private void shareAppLink() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        String body = "Check out this app: " + appShareLink;
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);

        Intent shareIntent = Intent.createChooser(sendIntent, "Share via");
        startActivity(shareIntent);
    }
}
