package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.adapters.CompanyAdapter;
import com.penguinstech.bookingappointmentsapp.background_services.AppointmentListenerService;
import com.penguinstech.bookingappointmentsapp.background_services.RestartServiceReceiver;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {



    List<Company> companyList;
    FirebaseFirestore db;//firestore instance
    Intent serviceIntent = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_new_company) {
            startActivity(new Intent(MainActivity.this, AddCompanyInfo.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        companyList = new ArrayList<>();
        updateUi();


    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    private void startBackgroundService() {
        ArrayList<String> companyIds = new ArrayList<>();
        for (int i = 0; i < companyList.size(); i++){

            Company company = companyList.get(i);
            if (company.getOwnerName().equals(Util.owner))
                companyIds.add(company.getFirebaseId());
        }
        AppointmentListenerService service = new AppointmentListenerService();
        if (!isMyServiceRunning(service.getClass())) {
            serviceIntent = new Intent(this, service.getClass());
            serviceIntent.putStringArrayListExtra("companyIds", companyIds);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    private void updateUi() {

        db.collection("companies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("firestore onSuccess", "LIST EMPTY");
                    } else {
                        // get all data and display

                        //convert whole queryDocumentSnapshots to list
                        companyList = queryDocumentSnapshots.toObjects(Company.class);
                        startBackgroundService();
                        RecyclerView recyclerView = findViewById(R.id.mainRv);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                        CompanyAdapter adapter = new CompanyAdapter(MainActivity.this, companyList);
                        recyclerView.setAdapter(adapter);
                    }


                }).addOnFailureListener(e->{
            Log.i("error", e.getMessage());
        });
    }

    @Override
    protected void onDestroy() {

        if(serviceIntent != null) {

            Log.i("closed", "intent");
            stopService(serviceIntent);
            Intent broadcastIntent = new Intent();
            broadcastIntent.putStringArrayListExtra("companyIds", serviceIntent.getStringArrayListExtra("companyIds"));
            broadcastIntent.setAction("restart_service");
            broadcastIntent.setClass(this, RestartServiceReceiver.class);
            this.sendBroadcast(broadcastIntent);
        }
        super.onDestroy();
    }
}