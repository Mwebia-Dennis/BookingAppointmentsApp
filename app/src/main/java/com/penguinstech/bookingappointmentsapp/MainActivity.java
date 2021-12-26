package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    CompanyAdapter adapter;
    List<Company> companyList;
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

        companyList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.mainRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        adapter = new CompanyAdapter(MainActivity.this, companyList);
        recyclerView.setAdapter(adapter);
        updateUi();
    }

    private void updateUi() {

        for (int i = 0;i<10;i++) {

            companyList.add(new Company("xyz" + String.valueOf(i), "dennis"));
        }
        adapter.notifyDataSetChanged();

    }
}