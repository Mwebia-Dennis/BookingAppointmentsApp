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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.penguinstech.bookingappointmentsapp.NotificationsActivity;
import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.Util;

import java.util.List;
import java.util.Random;

public class NotificationService extends FirebaseMessagingService {

    final String NOTIFICATION_CHANNEL_ID = "com.penguinstech.bookingappointmentsapp.background_services";
    String channelName = "Appointment Background Service";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //update token in firebase
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("companies")
                .whereEqualTo("ownerName", Util.owner).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Company> companyList1 = queryDocumentSnapshots.toObjects(Company.class);
                    for (Company company: companyList1) {
                        db.collection("companies")
                                .document(company.getFirebaseId())
                                .update("adminMsgToken", token);
                    }
                }).addOnFailureListener(e->{
           Log.i("updating token", "failed, error = "+e.getMessage());
        });

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        int notifId = new Random().nextInt(11) + 1;
        RemoteMessage.Notification notif = remoteMessage.getNotification();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground(notif.getTitle(), notif.getBody(), notifId);
        else
            showNotification(notif.getTitle(), notif.getBody(), notifId);
//            showNotification(String.valueOf(remoteMessage.getData().get("message")), notifId);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground(String title, String message, int notifId)
    {
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        showNotification(title , message, notifId);


    }


    private void showNotification(String title, String message, int notifId){
        Intent intent = new Intent(this, NotificationsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .setCategory(Notification.CATEGORY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        ((NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE)).notify(notifId, notificationBuilder.build());
//        startForeground(notifId, notificationBuilder.build());
    }
}
