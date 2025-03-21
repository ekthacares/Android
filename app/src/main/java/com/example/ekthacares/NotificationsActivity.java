package com.example.ekthacares;

import static android.content.ContentValues.TAG;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        fetchNotifications();
    }

    private void fetchNotifications() {
        // Retrieve JWT token and userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String jwtToken = sharedPreferences.getString(Constants.JWT_TOKEN_KEY, null);
        Long userId = sharedPreferences.getLong(Constants.USER_ID_KEY, -1);

        Log.d(TAG, "fetchNotifications: Retrieved JWT Token = " + jwtToken);
        Log.d(TAG, "fetchNotifications: Retrieved User ID = " + userId);

        if (jwtToken == null || userId == -1) {
            Toast.makeText(NotificationsActivity.this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorizationHeader = "Bearer " + jwtToken;
        Log.d(TAG, "fetchNotifications: Authorization Header = " + authorizationHeader);

        // Make API call to get notifications by userId
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Notification>> call = apiService.getUserNotificationsByUserId(userId, authorizationHeader);


        call.enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.w("USER_ID", "No notifications found for User ID: " + userId);
                    Toast.makeText(NotificationsActivity.this, "No notifications found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.e("USER_ID", "Failed to fetch notifications for User ID: " + userId, t);
                Toast.makeText(NotificationsActivity.this, "Failed to fetch notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
