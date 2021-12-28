package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.fonts.Font;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.adapters.BottomPopUp;
import com.penguinstech.bookingappointmentsapp.adapters.BusinessDetailsAdapter;
import com.penguinstech.bookingappointmentsapp.adapters.CompanyAdapter;
import com.penguinstech.bookingappointmentsapp.model.BusinessDay;
import com.penguinstech.bookingappointmentsapp.model.BusinessHours;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.Service;
import com.penguinstech.bookingappointmentsapp.model.Util;

import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompanyDetails extends AppCompatActivity {


    ArrayList<String> list = new ArrayList<>();
    Company company;
    BusinessDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_details);

        init();
    }

    public void init() {

        company = (Company) getIntent().getSerializableExtra("companyDetails");

        adapter = new BusinessDetailsAdapter(this, list);
        RecyclerView recyclerView = findViewById(R.id.detailsRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(CompanyDetails.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.bookAppointment).setOnClickListener(v-> {
            Intent i = new Intent(CompanyDetails.this, SelectServicesActivity.class);
            i.putExtra("companyId", company.getFirebaseId());
            startActivity(i);
        });

        updateUi();

    }

    private  void updateUi() {


        CircleImageView imageView = findViewById(R.id.profile_image);
        TextView nameTv = findViewById(R.id.name);
        TextView hoursTv = findViewById(R.id.business_hours_tv);

        //set up data on the UI (am using html to display data)
        nameTv.setText("Name: "+company.getCompanyName());

        StringBuilder businessHours = new StringBuilder();
        for (BusinessDay businessDay : company.getBusinessDayList()) {
            if (businessDay.isChecked()) {

                businessHours.append("<p>").append(Util.bold(businessDay.getDay())).append("</p>");
                for (BusinessHours businessHours1: businessDay.getListOfBusinessHours()) {
                    businessHours.append("<p style='text-align: right !important;'>")
                            .append("&nbsp &nbsp &nbsp &nbsp &nbsp ")
                            .append(businessHours1.getStartTime())
                            .append(" - ")
                            .append(businessHours1.getEndTime())
                            .append("</p>");
                }

            }else {

                businessHours
                        .append("<p>")
                        .append(Util.bold(businessDay.getDay()))
                        .append("<p style='color: red'>")
                        .append("&nbsp &nbsp &nbsp &nbsp &nbsp ")
                        .append("closed").append("</p></p>");
            }
        }

        hoursTv.setText(Html.fromHtml(String.valueOf(businessHours)));


        if (company.getLogo() != null && !company.getLogo().equals("")) {
            Glide.with(CompanyDetails.this).load(company.getLogo()).into(imageView);
        }
        if(company.getAddress() != null && !company.getAddress().equals("")){
            list.add("Address: "+company.getAddress());
        }
        if(company.getDescription() != null && !company.getDescription().equals("")){
            list.add("Description: "+company.getDescription());
        }
        if(company.getPhone() != null && !company.getPhone().equals("")){
            list.add("Phone: "+company.getPhone());
        }
        if(company.getEmail() != null && !company.getEmail().equals("")){
            list.add("Email: "+company.getEmail());
        }
        if(company.getWebsite() != null && !company.getWebsite().equals("")){
            list.add("Website: "+company.getWebsite());
        }
        if(company.getInstagram() != null && !company.getInstagram().equals("")){
            list.add("Instagram: "+company.getInstagram());
        }
        if(company.getFacebook() != null && !company.getFacebook().equals("")){
            list.add("Facebook: "+company.getFacebook());
        }
        if(company.getOwnerName() != null && !company.getOwnerName().equals("")){
            list.add("Staff: "+company.getOwnerName());
        }

        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.company_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_service) {
            new BottomPopUp(CompanyDetails.this, company.getFirebaseId(),true)
                    .show(getSupportFragmentManager(), "NewServicePopUp");
        }
        if (item.getItemId() == R.id.all_services) {

            //load services first
            new BottomPopUp(CompanyDetails.this, company.getFirebaseId(),false)
                    .show(getSupportFragmentManager(), "AllServicePopUp");


        }
        return super.onOptionsItemSelected(item);
    }
}