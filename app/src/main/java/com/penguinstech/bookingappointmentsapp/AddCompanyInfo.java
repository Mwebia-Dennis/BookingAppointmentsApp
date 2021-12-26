package com.penguinstech.bookingappointmentsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class AddCompanyInfo extends AppCompatActivity {

    BusinessDaysAdapter businessDaysAdapter;
    ArrayList<BusinessDay> listOfBusinessDays;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company_info);

        this.overridePendingTransition(R.anim.company_info_enter_anim,R.anim.company_info_leave_anim);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        init();
    }

    private void init() {
        listOfBusinessDays = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.business_days);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddCompanyInfo.this, LinearLayoutManager.VERTICAL, false));
        businessDaysAdapter = new BusinessDaysAdapter(this, listOfBusinessDays);
        recyclerView.setAdapter(businessDaysAdapter);
        loadBusinessDays();
    }

    private void loadBusinessDays() {
        final String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : daysOfWeek) {

            listOfBusinessDays.add(new BusinessDay(day, new ArrayList<>()));
        }
        businessDaysAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_company_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.save_menu_btn) {
            Toast.makeText(AddCompanyInfo.this, "Save btn clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.company_info_enter_anim,R.anim.company_info_leave_anim);
    }
}