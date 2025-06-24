package com.penguinstech.bookingappointmentsapp.background_services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.penguinstech.bookingappointmentsapp.MainActivity;
import com.penguinstech.bookingappointmentsapp.NotificationsActivity;
import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.Appointment;
import com.penguinstech.bookingappointmentsapp.model.NotificationStatus;

import java.util.ArrayList;
import java.util.List;

public class AppointmentListenerService extends Service {

    FirebaseFirestore db;//firestore instance
    ArrayList<String> companyIds = new ArrayList<>();
    final String NOTIFICATION_CHANNEL_ID = "com.penguinstech.bookingappointmentsapp.background_services";

    public AppointmentListenerService() {
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    String channelName = "Appointment Background Service";
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel chan;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }


        startForeground(1, getNotification("Starting service"));

        Log.i("service:create", "true");
//        android.os.Debug.waitForDebugger();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        stopForeground(true);//remove the notification
        this.companyIds = intent.getStringArrayListExtra("companyIds");


        listenToAppointmentChanges();
        return START_STICKY;//restart even when app is killed
    }

    private void listenToAppointmentChanges() {

        //add listener for all user companies
        Log.i("id", this.companyIds.get(0));
        for (String companyId: companyIds) {

            db.collection("company_appointments")
                    .document(companyId)
                    .collection("appointments").addSnapshotListener((snapshots, error) -> {

                        if (error != null) {
                            Log.i("service:error", error.getMessage());
                            return;
                        }

                        int notifId = 1;
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                //new data has been added
                                //show notification
                                Appointment appointment = dc.getDocument().toObject(Appointment.class);
                                if (appointment.getNotificationStatus().equals(NotificationStatus.UNREAD)) {

                                    //UPDATE STATUS TO NOTIFICATION RECEIVED
                                    db.collection("company_appointments")
                                            .document(companyId)
                                            .collection("appointments")
                                            .document(appointment.getFirebaseId()).update("notificationStatus", NotificationStatus.RECEIVED);
                                    //SHOW NOTIFICATION
                                    StringBuilder message = new StringBuilder().append(appointment.getClient().getFullName())
                                            .append(" requested an appointment with your company");
                                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                                        startMyOwnForeground(String.valueOf(message), notifId);
                                    else
                                        showNotification(String.valueOf(message), notifId);
                                }

                            }
                            notifId++;
                        }


                    });
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground(String message, int notifId)
    {
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        showNotification(message, notifId);


    }

    private void showNotification(String message, int notifId) {

//        startForeground(notifId, notification);
        ((NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE)).notify(notifId, getNotification(message));
    }

    private Notification getNotification(String message){
        Intent intent = new Intent(this, NotificationsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder
                .setContentTitle("New Appointment Request")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .setCategory(Notification.CATEGORY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_MIN);
        }
        return notificationBuilder.build();
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("service:destroy", "true");
        Intent broadcastIntent = new Intent();
        broadcastIntent.putStringArrayListExtra("companyIds", companyIds);
        broadcastIntent.setAction("restart_service");
        broadcastIntent.setClass(this, RestartServiceReceiver.class);
        this.sendBroadcast(broadcastIntent);
        super.onTaskRemoved(rootIntent);
    }
}
