package com.penguinstech.bookingappointmentsapp.background_services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
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
        context.bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //retrieve an instance of the service here from the IBinder returned
                //from the onBind method to communicate with
                Log.i("Service: Binding", "successful");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, Context.BIND_AUTO_CREATE);
    }

}
