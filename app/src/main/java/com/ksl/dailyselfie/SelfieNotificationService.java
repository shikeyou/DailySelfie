package com.ksl.dailyselfie;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class SelfieNotificationService extends IntentService {

    final static String TAG = "DailySelfie";

    private final int NOTIFICATION_ID = 11111111;

    public SelfieNotificationService() {
        super("Selfie Notification Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentTitle("Daily Selfie")
                .setContentText("Time for another selfie")
                .setTicker("Time for another selfie")
                .setAutoCancel(true)
                .setContentIntent(pi);
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(this.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }
}
