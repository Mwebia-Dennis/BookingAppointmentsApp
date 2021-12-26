package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {



    List<Company> companyList;
    FirebaseFirestore db;//firestore instance
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

        db.collection("companies")
                .document(Util.owner).collection("company").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("firestore onSuccess", "LIST EMPTY");
                    } else {
                        // get all data and display

                        //convert whole queryDocumentSnapshots to list
                        companyList = queryDocumentSnapshots.toObjects(Company.class);
                        updateUi();
                    }


                }).addOnFailureListener(e->{
            Log.i("error", e.getMessage());
        });
    }

    private void updateUi() {

        RecyclerView recyclerView = findViewById(R.id.mainRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        CompanyAdapter adapter = new CompanyAdapter(MainActivity.this, companyList);
        recyclerView.setAdapter(adapter);

    }
}