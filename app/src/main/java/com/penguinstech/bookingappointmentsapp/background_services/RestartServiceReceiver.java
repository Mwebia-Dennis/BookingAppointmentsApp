package com.penguinstech.bookingappointmentsapp.background_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("receiver:service", "restarted");
        Intent serviceIntent = new Intent(context, AppointmentListenerService.class);
        serviceIntent.putStringArrayListExtra("companyIds", intent.getStringArrayListExtra("companyIds"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
