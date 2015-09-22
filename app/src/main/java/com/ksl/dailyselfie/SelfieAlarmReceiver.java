package com.ksl.dailyselfie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SelfieAlarmReceiver extends BroadcastReceiver{

    final static String TAG = "DailySelfie";

    @Override
    public void onReceive(Context context, Intent intent) {

        //offload the status bar notification task to a service
        //because we should not hog onto a broadcast receiver for too long due to timeouts
        Intent i = new Intent(context, SelfieNotificationService.class);
        context.startService(i);
    }
}
