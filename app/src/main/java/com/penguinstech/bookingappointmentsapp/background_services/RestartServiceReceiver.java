package com.penguinstech.bookingappointmentsapp.background_services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.penguinstech.bookingappointmentsapp.NotificationsActivity;
import com.penguinstech.bookingappointmentsapp.R;

public class RestartServiceReceiver extends BroadcastReceiver {
    public RestartServiceReceiver() {
//        android.os.Debug.waitForDebugger();
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("receiver:service", "restarted");
        Intent serviceIntent = new Intent(context, AppointmentListenerService.class);
        serviceIntent.putStringArrayListExtra("companyIds", intent.getStringArrayListExtra("companyIds"));
        context.startService(serviceIntent);
    }

}
