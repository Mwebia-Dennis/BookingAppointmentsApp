package com.penguinstech.bookingappointmentsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AppointmentDatesActivity extends AppCompatActivity {

    List<Service> selectedServices;
    FirebaseFirestore db;//firestore instance
    String companyId;
    Company company;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_dates);

        init();
    }

    private void init() {

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        companyId = getIntent().getStringExtra("companyId");
        selectedServices = (ArrayList<Service>) getIntent().getSerializableExtra("selectedServicesList");

        findViewById(R.id.loader_1).setVisibility(View.VISIBLE);
        db.collection("companies").whereEqualTo("firebaseId", companyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    findViewById(R.id.loader_1).setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {


                        company = queryDocumentSnapshots.toObjects(Company.class).get(0);

                        final Calendar selectedCalendar = Calendar.getInstance();

                        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
                            selectedCalendar.set(Calendar.YEAR, year);
                            selectedCalendar.set(Calendar.MONTH,month);
                            selectedCalendar.set(Calendar.DAY_OF_MONTH,day);
                            updateLabel(selectedCalendar);
                        };

                        findViewById(R.id.selectDateBtn).setOnClickListener(V->{
                            DatePickerDialog datePickerDialog = new DatePickerDialog(
                                    AppointmentDatesActivity.this,
                                    dateSetListener,
                                    selectedCalendar.get(Calendar.YEAR),
                                    selectedCalendar.get(Calendar.MONTH),
                                    selectedCalendar.get(Calendar.DAY_OF_MONTH));
                            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                            datePickerDialog.show();
                        });
                    }
                }).addOnFailureListener(e-> {
            Toast.makeText(this, "Sorry could not load company info", Toast.LENGTH_SHORT).show();
            finish();
        });
    }


    private void updateLabel(Calendar myCalendar){
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        dateFormat.applyPattern("EEE, d MMM yyyy");
        TextView tv = findViewById(R.id.selectedDateTv);
        tv.setText(new StringBuilder().append("Selected Date is: ").append(dateFormat.format(myCalendar.getTime())));
    }
}