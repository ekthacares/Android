package com.example.ekthacares;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.gridlayout.widget.GridLayout;

import com.example.ekthacares.model.Campaign;
import com.example.ekthacares.model.SentEmail;
import com.example.ekthacares.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonorHomeActivity1 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;


    private ActionBarDrawerToggle toggle;

    private String jwtToken;
    private long userId;

    private TextView tvWelcome , tvUserName ,tvUserEmail;

    private ImageView ivNotifications, ivCampaigns, UserIcon;
    private View notificationDot, notificationDot1;

    private TextView tvCampaignTitle, tvCampaignTitle1, tvCampaignMessage, tvCampaignMessage1;

    private TextView tvCampaignDate, tvCampaignTime, tvCampaignDate1, tvCampaignTime1;

    private CardView cardCampaign, cardCampaign1;

    private MaterialCardView campaignCard2, quickSearchCard, mydonationscard ;

    private TextView tvHospitalName, tvHospitalName1, tvTime, tvTime1, tvName, tvName1, bloodIdText, bloodIdText1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_home1);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        // Set Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide title

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvWelcome = findViewById(R.id.tvWelcome);
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tvUserName);
        tvUserEmail = headerView.findViewById(R.id.tvUserEmail);
        ivNotifications = findViewById(R.id.ivNotifications);
        notificationDot = findViewById(R.id.notification_dot);
        ivCampaigns = findViewById(R.id.ivCampaigns);
        notificationDot1 = findViewById(R.id.notification_dot1);

        tvCampaignTitle = findViewById(R.id.tvCampaignTitle);
        tvCampaignDate = findViewById(R.id.tvCampaignDate);
        tvCampaignTime = findViewById(R.id.tvCampaignTime);

        tvCampaignTitle1 = findViewById(R.id.tvCampaignTitle1);
        tvCampaignDate1 = findViewById(R.id.tvCampaignDate1);
        tvCampaignTime1 = findViewById(R.id.tvCampaignTime1);
        cardCampaign = findViewById(R.id.campaign_card);
        cardCampaign1 = findViewById(R.id.campaign_card1);
        campaignCard2 = findViewById(R.id.campaign_card2);
        quickSearchCard  = findViewById(R.id.quick_search_card);
        mydonationscard = findViewById(R.id.my_donation);

        // Bind views
        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvTime = findViewById(R.id.tvTime);
        tvHospitalName1 = findViewById(R.id.tvHospitalName1);
        tvTime1 = findViewById(R.id.tvTime1);
        tvName = findViewById(R.id.tvName);
        tvName1 = findViewById(R.id.tvName1);
        bloodIdText = findViewById(R.id.bloodIdText);
        bloodIdText1 = findViewById(R.id.bloodIdText1);
        UserIcon = findViewById(R.id.ivUserIcon);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);


        // Retrieve JWT token and user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken != null && userId != -1) {
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        fetchLatestSentEmails();
        fetchLatestCampaigns();

        // Retrieve and store FCM Token
        fetchFcmToken();

        // Set click listeners to open CampaignActivity
        cardCampaign.setOnClickListener(v -> openCampaignActivity());
        cardCampaign1.setOnClickListener(v -> openCampaignActivity());
        campaignCard2.setOnClickListener(v -> openCampaignActivity());
        quickSearchCard.setOnClickListener(v -> openQuickSearchActivity());
        mydonationscard.setOnClickListener(v -> openMyDonationsActivity());
        tvHospitalName.setOnClickListener(v -> openReceivedRequestsActivity());
        tvTime.setOnClickListener(v -> openReceivedRequestsActivity());
        tvHospitalName1.setOnClickListener(v -> openReceivedRequestsActivity());
        tvTime1.setOnClickListener(v -> openReceivedRequestsActivity());
        tvName.setOnClickListener(v -> openReceivedRequestsActivity());
        tvName1.setOnClickListener(v -> openReceivedRequestsActivity());
        bloodIdText.setOnClickListener(v -> openReceivedRequestsActivity());
        bloodIdText.setOnClickListener(v -> openReceivedRequestsActivity());
        UserIcon.setOnClickListener(v -> openDonorProfileActivity());

        // Notification icon click
        ivNotifications.setOnClickListener(v -> {
            String fcmToken = sharedPreferences.getString("FCM_TOKEN", null);

            if (fcmToken != null) {
                Intent intent = new Intent(DonorHomeActivity1.this, NotificationsActivity.class);
                intent.putExtra("FCM_TOKEN", fcmToken);
                startActivity(intent);

                // Clear unread notification state when the user opens notifications
                clearNotificationDot();
            } else {
                Toast.makeText(DonorHomeActivity1.this, "FCM Token not available", Toast.LENGTH_SHORT).show();
            }
        });

        updateNotificationIcon();

        // Campaign icon click
        ivCampaigns.setOnClickListener(v -> {
            Intent intent = new Intent(DonorHomeActivity1.this, CampaignActivity.class);
            startActivity(intent);

            // Clear unread campaign state when the user opens campaigns
            clearCampaignDot();
        });

        updateCampaignIcon();


    }


    private void openCampaignActivity() {
        Intent intent = new Intent(DonorHomeActivity1.this, CampaignActivity.class);
        startActivity(intent);
    }

    private void openDonorProfileActivity() {
        Intent intent = new Intent(DonorHomeActivity1.this, DonorProfileActivity.class);
        startActivity(intent);
    }

    private void openMyDonationsActivity() {
        Intent intent = new Intent(DonorHomeActivity1.this, MyDonationsActivity.class);
        startActivity(intent);
    }
    private void openQuickSearchActivity() {
      //  Intent intent = new Intent(DonorHomeActivity1.this, QuickSearchActivity1.class);
//startActivity(intent);
        QuickSearchDialogFragment dialog = new QuickSearchDialogFragment();
        dialog.show(getSupportFragmentManager(), "QuickSearchDialog");
    }

    private void openReceivedRequestsActivity() {
        Intent intent = new Intent(DonorHomeActivity1.this, ReceivedRequestsActivity.class);
        startActivity(intent);
    }
    private void updateCampaignIcon() {
        SharedPreferences sharedPreferences = getSharedPreferences("CampaignPrefs", MODE_PRIVATE);
        boolean hasUnreadCampaigns = sharedPreferences.getBoolean("hasUnreadCampaigns", false);
        notificationDot1.setVisibility(hasUnreadCampaigns ? View.VISIBLE : View.GONE);
    }

    private void clearCampaignDot() {
        SharedPreferences sharedPreferences = getSharedPreferences("CampaignPrefs", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("hasUnreadCampaigns", false).apply();
        notificationDot1.setVisibility(View.GONE);
    }

    private void updateNotificationIcon() {
        SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
        boolean hasUnread = prefs.getBoolean("hasUnreadNotifications", false);
        notificationDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    private void clearNotificationDot() {
        SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("hasUnreadNotifications", false).apply();
        notificationDot.setVisibility(View.GONE);
    }

    // Notification receiver
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotificationIcon();
            // Restart DonorHomeActivity1
            Intent reloadIntent = new Intent(DonorHomeActivity1.this, DonorHomeActivity1.class);
            reloadIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(reloadIntent);
            finish(); // Finish current activity to reload it

        }
    };

    // Campaign receiver
    private final BroadcastReceiver campaignReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCampaignIcon();
            fetchLatestCampaigns();
            openCampaignActivity();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        updateNotificationIcon();
        updateCampaignIcon();

        registerReceiver(notificationReceiver, new IntentFilter("com.example.ekthacares.NOTIFICATION_RECEIVED"), Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(campaignReceiver, new IntentFilter("com.example.ekthacares.CAMPAIGN_RECEIVED"), Context.RECEIVER_NOT_EXPORTED);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        if (jwtToken != null && userId != -1) {
            fetchUserDetails(jwtToken, userId);
        } else {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(notificationReceiver);
        unregisterReceiver(campaignReceiver);
    }


    private void fetchUserDetails(String jwtToken, Long userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        Call<User> call = apiService.getUserDetails("Bearer " + jwtToken, userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("DonorHomeActivity1", "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    User userDetails = response.body();
                    Log.d("DonorHomeActivity", "User Details: " + userDetails);

                    // Check if tvWelcome is not null before setting text
                    if (tvWelcome != null) {
                        tvWelcome.setText("Welcome, " + userDetails.getDonorName());
                        tvUserName.setText(userDetails.getDonorName());
                        tvUserEmail.setText(userDetails.getEmailId());
                    }
                } else {
                    Toast.makeText(DonorHomeActivity1.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("DonorHomeActivity", "Error fetching user details", t);
                Toast.makeText(DonorHomeActivity1.this, "An error occurred.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserName(Long userId, TextView textView) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<User> call = apiService.getUserDetails1(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    textView.setText(user.getDonorName()); // Update TextView with the fetched name
                } else {
                    textView.setText("Unknown User"); // Default if not found
                    Log.e("User Fetch", "Failed to fetch user: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                textView.setText("Error fetching user");
                Log.e("User Fetch", "Error fetching user details", t);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_update_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_donations) {
            startActivity(new Intent(this, MyDonationsActivity.class));
        } else if (id == R.id.nav_received_requests) {
            startActivity(new Intent(this, ReceivedRequestsActivity.class));
        } else if (id == R.id.nav_donor_tracking) {
            startActivity(new Intent(this, DonorTrackingActivity.class));
        } else if (id == R.id.nav_request_blood) {
            startActivity(new Intent(this, RequestBloodActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void fetchFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("FCM", "Fetching FCM token failed", task.getException());
                return;
            }

            String fcmToken = task.getResult();
            Log.d("FCM", "FCM Token: " + fcmToken);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("FCM_TOKEN", fcmToken);
            editor.apply();

            sendFcmTokenToServer(fcmToken);
        });
    }

    private void sendFcmTokenToServer(String fcmToken) {
        if (jwtToken == null || userId == -1) {
            Log.e("FCM", "User not logged in, skipping FCM token upload.");
            return;
        }

        // Prepare the request body with fcmToken as a key-value pair in a map
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("fcmToken", fcmToken);

        // Get the API service
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the API call
        Call<Void> call = apiService.updateFcmToken("Bearer " + jwtToken, userId, requestBody);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "FCM Token sent to server successfully");
                } else {
                    Log.e("FCM", "Failed to send FCM Token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Error sending FCM Token", t);
            }
        });
    }

    private void fetchLatestSentEmails() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<SentEmail>> call = apiService.getSentEmails("Bearer " + jwtToken, userId);

        call.enqueue(new Callback<List<SentEmail>>() {
            @Override
            public void onResponse(Call<List<SentEmail>> call, Response<List<SentEmail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response for emails", response.body().toString());  // Debugging log
                    List<SentEmail> sentEmails = response.body();

                    if (!sentEmails.isEmpty()) {
                        int size = sentEmails.size();

                        // Latest email (last in the list)
                        SentEmail latestEmail = sentEmails.get(size - 1);
                        // Set first email details
                        //tvName.setText(String.valueOf(latestEmail.getLoggedInUserId()));
                        fetchUserName(Long.valueOf(String.valueOf(latestEmail.getLoggedInUserId())), tvName);
                        tvHospitalName.setText(latestEmail.getHospitalName());
                        tvTime.setText(latestEmail.getTimeDifference());
                        bloodIdText.setText(latestEmail.getBloodGroup());

                        // Set second email details if available
                        if (sentEmails.size() > 1) {
                            SentEmail secondLatestEmail = sentEmails.get(size - 2);
                            tvHospitalName1.setText(secondLatestEmail.getHospitalName());
                            tvTime1.setText(secondLatestEmail.getTimeDifference());
                            bloodIdText1.setText(latestEmail.getBloodGroup());
                            fetchUserName(Long.valueOf(String.valueOf(latestEmail.getLoggedInUserId())), tvName1);

                        } else {
                            tvHospitalName1.setText("No second email available");
                            tvTime1.setText("");
                            bloodIdText.setText("");
                        }
                    } else {
                        Log.e("SentEmails", "No sent emails found");
                        tvHospitalName.setText("No emails available");
                        tvTime.setText("");
                        bloodIdText.setText("");

                        tvHospitalName1.setText("No emails available");
                        tvTime1.setText("");
                        bloodIdText1.setText("");
                    }
                } else {
                    Log.e("SentEmails", "Failed to fetch emails: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<SentEmail>> call, Throwable t) {
                Log.e("SentEmails", "Error fetching emails", t);
            }
        });
    }

    private void fetchLatestCampaigns() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Campaign>> call = apiService.getLatestCampaigns("Bearer " + jwtToken);

        call.enqueue(new Callback<List<Campaign>>() {
            @Override
            public void onResponse(Call<List<Campaign>> call, Response<List<Campaign>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API Response for cards", response.body().toString());  // Add this
                    List<Campaign> campaigns = response.body();

                    if (!campaigns.isEmpty()) {
                        // Set first campaign details
                        tvCampaignTitle.setText(campaigns.get(0).getTitle().toUpperCase().replace(" ", "\n"));
                        tvCampaignDate.setText(campaigns.get(0).getFormattedDate());
                        tvCampaignTime.setText(campaigns.get(0).getFormattedTime());

                        // Set second campaign details if available
                        if (campaigns.size() > 1) {
                            tvCampaignTitle1.setText(campaigns.get(1).getTitle().toUpperCase().replace(" ", "\n"));
                            tvCampaignDate1.setText(campaigns.get(1).getFormattedDate());
                            tvCampaignTime1.setText(campaigns.get(1).getFormattedTime());
                        } else {
                            tvCampaignTitle1.setText("No second campaign available");
                            tvCampaignMessage1.setText("");
                            tvCampaignDate1.setText("");
                            tvCampaignTime1.setText("");
                        }
                    } else {
                        Log.e("Campaign", "No campaigns found");
                        tvCampaignTitle.setText("No campaigns available");
                        tvCampaignMessage.setText("");
                        tvCampaignDate.setText("");
                        tvCampaignTime.setText("");

                        tvCampaignTitle1.setText("No campaigns available");
                        tvCampaignMessage1.setText("");
                        tvCampaignDate1.setText("");
                        tvCampaignTime1.setText("");
                    }
                } else {
                    Log.e("Campaign", "Failed to fetch campaigns: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Campaign>> call, Throwable t) {
                Log.e("Campaign", "Error fetching campaigns", t);
            }
        });
    }



    private void showLogoutDialog() {
        new AlertDialog.Builder(DonorHomeActivity1.this)
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    redirectToLogin();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DonorHomeActivity1.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}