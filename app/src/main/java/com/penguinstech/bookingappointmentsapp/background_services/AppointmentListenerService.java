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

    @Override
    public void onCreate() {
        super.onCreate();
//        android.os.Debug.waitForDebugger();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

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

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                //new data has been added
                                //show notification
                                Appointment appointment = dc.getDocument().toObject(Appointment.class);

                                StringBuilder message = new StringBuilder().append(appointment.getClient().getFullName())
                                        .append(" requested an appointment with your company");
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                                    startMyOwnForeground(String.valueOf(message));
                                else
                                    showNotification(String.valueOf(message));
                            }
                        }


                    });
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground(String message)
    {
        String channelName = "Appointment Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        showNotification(message);


    }

    private void showNotification(String message) {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder
                .setContentTitle("New Appointment Request")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_MIN);
        }
        Notification notification = notificationBuilder.build();
        startForeground(2, notification);
    }

}
