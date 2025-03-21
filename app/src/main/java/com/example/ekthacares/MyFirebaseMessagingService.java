package com.example.ekthacares;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID_NOTIFICATION = "notification_channel";
    private static final String CHANNEL_ID_CAMPAIGN = "campaign_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM Message Received");

        // ✅ Extract Notification Details
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : remoteMessage.getData().get("title");
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type"); // "campaign" or "notification"

        // ✅ Log Type of Notification
        Log.d(TAG, "Notification Type: " + type);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Body: " + body);

        if (title != null && body != null) {
            if ("campaign".equals(type)) {
                updateCampaignBadge(); // Show red dot on ivCampaigns
                sendCampaignNotification(title, body);
            } else {
                updateNotificationBadge(); // Show red dot on ivNotifications
                sendNotification(title, body);
            }
        }
    }

    // ✅ Update Red Dot for Notifications
    private void updateNotificationBadge() {
        SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("hasUnreadNotifications", true);
        editor.apply();

        // Notify DonorHomeActivity to update UI
        Intent intent = new Intent("com.example.ekthacares.NOTIFICATION_RECEIVED");
        sendBroadcast(intent);
    }

    // ✅ Update Red Dot for Campaigns
    private void updateCampaignBadge() {
        SharedPreferences prefs = getSharedPreferences("CampaignPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("hasUnreadCampaigns", true);
        editor.apply();

        // Notify DonorHomeActivity to update UI
        Intent intent = new Intent("com.example.ekthacares.CAMPAIGN_RECEIVED");
        sendBroadcast(intent);
    }

    // ✅ Send Notification
    private void sendNotification(String title, String messageBody) {
        sendCustomNotification(title, messageBody, NotificationsActivity.class, CHANNEL_ID_NOTIFICATION, "Default Notifications", R.drawable.ic_notification);
    }

    // ✅ Send Campaign Notification
    private void sendCampaignNotification(String title, String messageBody) {
        sendCustomNotification(title, messageBody, CampaignActivity.class, CHANNEL_ID_CAMPAIGN, "Campaign Notifications", R.drawable.ic_campaign);
    }

    // ✅ Generic Notification Builder
    private void sendCustomNotification(String title, String messageBody, Class<?> activityClass, String channelId, String channelName, int icon) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // Create Notification Channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for " + channelName);
            notificationManager.createNotificationChannel(channel);
        }

        // Build Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }
}