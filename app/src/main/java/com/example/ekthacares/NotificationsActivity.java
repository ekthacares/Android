package com.example.ekthacares;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ekthacares.model.Notification;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private String fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // Retrieve FCM token from Intent or SharedPreferences
        fcmToken = getIntent().getStringExtra("FCM_TOKEN");

        if (fcmToken == null) {
            SharedPreferences sharedPreferences = getSharedPreferences("FCM_PREF", MODE_PRIVATE);
            fcmToken = sharedPreferences.getString("fcmToken", null);
        }

        // Print FCM Token in Logcat
        if (fcmToken != null) {
            Log.d("FCM_TOKEN", "Retrieved FCM Token: " + fcmToken);
            fetchNotifications(fcmToken);
        } else {
            Log.e("FCM_TOKEN", "FCM Token not available");
            Toast.makeText(this, "FCM Token not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchNotifications(String fcmToken) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Notification>> call = apiService.getUserNotifications(fcmToken);

        call.enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.w("FCM_TOKEN", "No notifications found for FCM Token: " + fcmToken);
                    Toast.makeText(NotificationsActivity.this, "No notifications found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.e("FCM_TOKEN", "Failed to fetch notifications for FCM Token: " + fcmToken, t);
                Toast.makeText(NotificationsActivity.this, "Failed to fetch notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
