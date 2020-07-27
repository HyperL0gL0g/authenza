package com.example.fingerprint_protection;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class notificationService extends FirebaseMessagingService {




    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData()!=null && remoteMessage.getData().size() > 0) {
            Log.d("adad", "Message data payload: " + remoteMessage.getData());
            String notificationData = remoteMessage.getData().get("Nick");
            showNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(),notificationData);
        }

    }

    private void showNotification(String title, String message,String token) {

        Intent intent = new Intent(this, recylerViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Nick",token);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"MyNotifcation")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.fingernoti)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDefaults(Notification.DEFAULT_ALL)
                ;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("MyNotifcation", "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            assert notificationManager != null;
            builder.setChannelId("MyNotifcation");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(123,builder.build());
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
