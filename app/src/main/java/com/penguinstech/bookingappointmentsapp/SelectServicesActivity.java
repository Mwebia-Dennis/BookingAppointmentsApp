package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.adapters.SelectedServiceAdapter;
import com.penguinstech.bookingappointmentsapp.adapters.ServicesAdapter;
import com.penguinstech.bookingappointmentsapp.model.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectServicesActivity extends AppCompatActivity {

//    List<Service> serviceList = new ArrayList<>();
    List<Service> selectedServices = new ArrayList<>();
    String companyId;
    FirebaseFirestore db;//firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_services);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//place the back button
        companyId = getIntent().getStringExtra("companyId");
        FirebaseApp.initializeApp(SelectServicesActivity.this);
        db = FirebaseFirestore.getInstance();

        SelectedServiceAdapter selectedServiceAdapter = new SelectedServiceAdapter(this, selectedServices);
        RecyclerView selectedServicesRv = findViewById(R.id.selected_services_rv);
        selectedServicesRv.setLayoutManager(new LinearLayoutManager(SelectServicesActivity.this, LinearLayoutManager.VERTICAL, false));
        selectedServicesRv.setAdapter(selectedServiceAdapter);

        db.collection("company_services")
                .document(companyId)
                .collection("services").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        // get all data and display
                        //convert whole queryDocumentSnapshots to list
                        List<Service> serviceList = queryDocumentSnapshots.toObjects(Service.class);
                        RecyclerView allServicesRv = findViewById(R.id.all_services_rv);
                        allServicesRv.setLayoutManager(new LinearLayoutManager(SelectServicesActivity.this, LinearLayoutManager.VERTICAL, false));
                        ServicesAdapter adapter = new ServicesAdapter(SelectServicesActivity.this, serviceList, true, selectedServiceAdapter);
                        allServicesRv.setAdapter(adapter);
                    }else {
                        Toast.makeText(this, "This company does not offer any service, sorry for the inconvenience", Toast.LENGTH_LONG)
                                .show();
                        finish();
                    }


                }).addOnFailureListener(e->{
            Log.i("error", e.getMessage());
        });


        findViewById(R.id.continueBtn).setOnClickListener(v-> {
            if(selectedServices.size() > 0) {

                Intent intent = new Intent(SelectServicesActivity.this, AppointmentDatesActivity.class);
                intent.putExtra("selectedServicesList", (Serializable) selectedServices);
                intent.putExtra("companyId", companyId);
                startActivity(intent);
            }else {
                Toast.makeText(this, "Hey, you need to select at least one service to continue", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}