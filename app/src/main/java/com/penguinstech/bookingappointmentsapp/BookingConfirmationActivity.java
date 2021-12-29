package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.model.Appointment;
import com.penguinstech.bookingappointmentsapp.model.Service;

import java.util.ArrayList;

public class BookingConfirmationActivity extends AppCompatActivity {

    private FirebaseFirestore db;//firestore instance
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        init();
    }

    public void init() {

        FirebaseApp.initializeApp(BookingConfirmationActivity.this);
        db = FirebaseFirestore.getInstance();
        String companyId = getIntent().getStringExtra("companyId");
        Appointment appointment = (Appointment) getIntent().getSerializableExtra("appointment");
        TextView selectedServicesTv = findViewById(R.id.selectedServices);
        TextView totalPriceTv = findViewById(R.id.totalPrice);
        TextView totalDurationTv = findViewById(R.id.totalDuration);

        selectedServicesTv.setText(appointment.getFirebaseId());
        totalPriceTv.setText(appointment.getTotalPrice());
        totalDurationTv.setText(appointment.getDuration());

        findViewById(R.id.returnToHomeBtn).setOnClickListener(v->{
            returnHome();
        });
        findViewById(R.id.cancelAppointment).setOnClickListener(v->{
            //delete appointment
            db.collection("company_appointments")
                    .document(companyId)
                    .collection("appointments")
                    .document(appointment.getFirebaseId()).delete().addOnSuccessListener(aVoid -> {
                        Toast.makeText(BookingConfirmationActivity.this, "Appointment Cancelled Successfully", Toast.LENGTH_SHORT).show();
                        returnHome();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(BookingConfirmationActivity.this, "Sorry could not cancel appointment, try again later", Toast.LENGTH_SHORT).show();
                    });
        });

    }

    private void returnHome() {
        Intent intent = new Intent(BookingConfirmationActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}