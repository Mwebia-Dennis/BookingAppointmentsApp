package com.penguinstech.bookingappointmentsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.adapters.CompanyAdapter;
import com.penguinstech.bookingappointmentsapp.adapters.NotificationAdapter;
import com.penguinstech.bookingappointmentsapp.model.Appointment;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.NotificationStatus;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    List<Appointment> appointmentList;
    FirebaseFirestore db;//firestore instance

    String companyId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        init();
    }

    private void init() {

        companyId = getIntent().getStringExtra("companyId");
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        appointmentList = new ArrayList<>();
        updateUi();
        //update notifications from unread to read
        new Thread(this::updateNotificationStatuses).start();


    }

    private void updateNotificationStatuses() {
        db.collection("company_appointments")
                .document(companyId)
                .collection("appointments")
                .whereEqualTo("notificationStatus", NotificationStatus.UNREAD)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        //update notifications to read
                        List<Appointment> list = queryDocumentSnapshots.toObjects(Appointment.class);
                        for(Appointment appointment: list) {

                            db.collection("company_appointments")
                                    .document(companyId)
                                    .collection("appointments")
                                    .document(appointment.getFirebaseId()).update("notificationStatus", NotificationStatus.READ);

                        }
                    }


                }).addOnFailureListener(e->{
            Log.i("error", e.getMessage());
        });
    }


    private void updateUi() {
        db.collection("company_appointments")
                .document(companyId)
                .collection("appointments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("firestore onSuccess", "LIST EMPTY");
                    } else {
                        // get all data and display

                        //convert whole queryDocumentSnapshots to list
                        appointmentList = queryDocumentSnapshots.toObjects(Appointment.class);

                        RecyclerView recyclerView = findViewById(R.id.notificationsRv);
                        recyclerView.setLayoutManager(new LinearLayoutManager(NotificationsActivity.this, LinearLayoutManager.VERTICAL, false));
                        NotificationAdapter adapter = new NotificationAdapter(NotificationsActivity.this, appointmentList);
                        recyclerView.setAdapter(adapter);
                    }


                }).addOnFailureListener(e->{
            Log.i("error", e.getMessage());
        });
    }
}