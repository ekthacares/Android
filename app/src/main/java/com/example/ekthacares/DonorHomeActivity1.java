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
import com.example.ekthacares.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DonorHomeActivity1 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private String jwtToken;
    private long userId;

    private TextView tvWelcome , tvUserName ,tvUserEmail;

    private ImageView ivNotifications, ivCampaigns;
    private View notificationDot, notificationDot1;

    private TextView tvCampaignTitle, tvCampaignTitle1, tvCampaignMessage, tvCampaignMessage1;

    private TextView tvCampaignDate, tvCampaignTime, tvCampaignDate1, tvCampaignTime1;

    private CardView cardCampaign, cardCampaign1;

    private MaterialCardView campaignCard2, quickSearchCard ;



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

        GridLayout gridLayout = findViewById(R.id.gridQuickActions);
        gridLayout.setVisibility(View.GONE);
        new Handler().postDelayed(() -> gridLayout.setVisibility(View.VISIBLE), 500);


        // Set Navigation Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set Navigation Item Click Listener
        navigationView.setNavigationItemSelectedListener(this);

        // Handle Logout Menu Click Properly
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            showLogoutDialog();
            return true;
        });

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


        fetchLatestCampaigns();

    // Setup Card Click Listeners
        setupCardListeners();

        // Retrieve and store FCM Token
        fetchFcmToken();

        // Set click listeners to open CampaignActivity
        cardCampaign.setOnClickListener(v -> openCampaignActivity());
        cardCampaign1.setOnClickListener(v -> openCampaignActivity());
        campaignCard2.setOnClickListener(v -> openCampaignActivity());
        quickSearchCard.setOnClickListener(v -> openQuickSearchActivity());



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


    private void setupCardListeners() {
        findViewById(R.id.cardProfile).setOnClickListener(v -> openProfile());
        findViewById(R.id.cardDonations).setOnClickListener(v -> openDonations());
       findViewById(R.id.cardRequests).setOnClickListener(v -> openRequests());
        findViewById(R.id.cardQuickSearch).setOnClickListener(v -> openQuickSearch());
        findViewById(R.id.cardDonorTracking).setOnClickListener(v -> openDonorTracking());
        findViewById(R.id.cardRequestBlood).setOnClickListener(v -> openRequestBlood());
    }


    private void openCampaignActivity() {
        Intent intent = new Intent(DonorHomeActivity1.this, CampaignActivity.class);
        startActivity(intent);
    }

    private void openQuickSearchActivity() {
        Intent intent = new Intent(DonorHomeActivity1.this, QuickSearchActivity.class);
        startActivity(intent);
    }
    private void openProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void openDonations() {
        startActivity(new Intent(this, MyDonationsActivity.class));
    }

    private void openRequests() {
        startActivity(new Intent(this, ReceivedRequestsActivity.class));
    }

    private void openQuickSearch() {
        startActivity(new Intent(this, QuickSearchActivity.class));
    }

    private void openDonorTracking() {
        startActivity(new Intent(this, DonorTrackingActivity.class));
    }

    private void openRequestBlood() {
        startActivity(new Intent(this, RequestBloodActivity.class));
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_update_profile) {
            openProfile();
        }else if (id == R.id.nav_donations) {
            openDonations();
        }else if (id == R.id.nav_received_requests) {
            openRequests();
        }else if (id == R.id.nav_donor_tracking) {
            openDonorTracking();
        }else if (id == R.id.nav_request_blood) {
            openRequestBlood();
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