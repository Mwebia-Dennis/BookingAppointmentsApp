package com.penguinstech.bookingappointmentsapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.penguinstech.bookingappointmentsapp.adapters.BusinessDaysAdapter;
import com.penguinstech.bookingappointmentsapp.model.BusinessDay;
import com.penguinstech.bookingappointmentsapp.model.BusinessHours;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

public class AddCompanyInfo extends AppCompatActivity {

    BusinessDaysAdapter businessDaysAdapter;
    ArrayList<BusinessDay> listOfBusinessDays;
    ActivityResultLauncher<Intent> activityResultLauncher;
    FirebaseFirestore db;//firestore instance
    StorageReference storageRef;
    Boolean isLogoImage = true;
    Boolean isEditing = false;
    Uri logoUri, businessPhotoUri = null;
    Company company = new Company();
    EditText name;
    EditText des;
    EditText address;
    EditText phone;
    EditText email;
    EditText website;
    EditText instagram;
    EditText facebook;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company_info);

        this.overridePendingTransition(R.anim.company_info_enter_anim,R.anim.company_info_leave_anim);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        init();
    }

    private void init() {

        name = findViewById(R.id.business_name);
        des = findViewById(R.id.business_des);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);
        website = findViewById(R.id.website);
        instagram = findViewById(R.id.instagram);
        facebook = findViewById(R.id.facebook);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("files/"+ Util.owner);
        //check if there is company data in the intent
        //if there is data then we are editing
        if(getIntent().getSerializableExtra("companyDetails") != null) {
            isEditing = true;
            company = (Company) getIntent().getSerializableExtra("companyDetails");
            updateUi();
        }
        listOfBusinessDays = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.business_days);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddCompanyInfo.this, LinearLayoutManager.VERTICAL, false));
        businessDaysAdapter = new BusinessDaysAdapter(this, listOfBusinessDays);
        recyclerView.setAdapter(businessDaysAdapter);
        loadBusinessDays();



        findViewById(R.id.add_logo_img).setOnClickListener(v-> {
            isLogoImage = true;
            requestReadStoragePermission();
        });

        findViewById(R.id.add_business_photo).setOnClickListener(v-> {
            isLogoImage = false;
            requestReadStoragePermission();
        });


        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            final Uri imageUri = result.getData().getData();
                            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_SHORT).show();


                            if(isLogoImage) {
                                //save logo image
                                logoUri = imageUri;
                                ImageView logoImageView = findViewById(R.id.logo_iv);
                                Glide.with(this).load(imageUri).into(logoImageView);
//                                Picasso.get()
//                                        .load(imageUri)
//                                        .into(logoImageView);
                            }else {
                                businessPhotoUri = imageUri;
                                ImageView photoImageView = findViewById(R.id.photo_iv);
                                Glide.with(this)
                                        .load(imageUri)
                                        .into(photoImageView);
                            }
                        }
                    }
                });
    }

    private void updateUi() {
        name.setText(company.getCompanyName());

        if(company.getDescription() != null) {
            des.setText(company.getDescription());
        }
        if(company.getAddress() != null) {
            address.setText(company.getAddress());
        }
        if(company.getPhone() != null) {
            phone.setText(company.getPhone());
        }
        if(company.getEmail() != null) {
            email.setText(company.getEmail());
        }

        if(company.getInstagram() != null) {
            instagram.setText(company.getInstagram());
        }
        if(company.getFacebook() != null) {
            facebook.setText(company.getFacebook());
        }

    }

    private void loadBusinessDays() {
        //if we are editing get details from company object
        if (isEditing){
            listOfBusinessDays.addAll(company.getBusinessDayList());
        }else {
            //else add dummy samples on UI
            final String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            for (String day : daysOfWeek) {

                listOfBusinessDays.add(new BusinessDay(day, getHours(), false));
            }
        }
        businessDaysAdapter.notifyDataSetChanged();
    }


    private ArrayList<BusinessHours> getHours() {

        ArrayList<BusinessHours> list = new ArrayList<>();
        list.clear();

        //business hours between 08 am and 09 am
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);


        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 9);
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.SECOND, 0);
        endTime.set(Calendar.MILLISECOND, 0);
        list.add(new BusinessHours(BusinessDaysAdapter.sdf.format(startTime.getTime()),
                BusinessDaysAdapter.sdf.format(endTime.getTime())));
        return list;
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
            //save details

            saveCompanyDetails();


//            Toast.makeText(AddCompanyInfo.this, "Save btn clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.company_info_enter_anim,R.anim.company_info_leave_anim);
    }

    private void saveCompanyDetails() {
        if(name.getText().toString().trim().equals("")) {
            //validate form, business name
            name.setError("this field is required");
            name.requestFocus();
        }else {

            company.setCompanyName(name.getText().toString().trim());

            if(!des.getText().toString().trim().equals("")) {
                company.setDescription(des.getText().toString().trim());
            }
            if(!address.getText().toString().trim().equals("")) {
                company.setAddress(address.getText().toString().trim());
            }
            if(!phone.getText().toString().trim().equals("")) {
                company.setPhone(phone.getText().toString().trim());
            }
            if(!email.getText().toString().trim().equals("")) {
                company.setEmail(email.getText().toString().trim());
            }
            if(!website.getText().toString().trim().equals("")) {
                company.setWebsite(website.getText().toString().trim());
            }
            if(!instagram.getText().toString().trim().equals("")) {
                company.setInstagram(instagram.getText().toString().trim());
            }
            if(!facebook.getText().toString().trim().equals("")) {
                company.setFacebook(facebook.getText().toString().trim());
            }
            company.setOwnerName(Util.owner);
            company.setBusinessDayList(listOfBusinessDays);

            Toast.makeText(this, "loading please wait...", Toast.LENGTH_SHORT).show();
            DocumentReference ref;
            if(!isEditing){

                ref = db.collection("companies").document();
                company.setFirebaseId(ref.getId());
            }else {
                ref = db.collection("companies").document(company.getFirebaseId());
            }

            ref.set(company)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Adding Data: ", "Successful");
                        //add images and update table

                        //add logo image
                        if(logoUri != null) {
                            Toast.makeText(AddCompanyInfo.this, "Logo image saving in background", Toast.LENGTH_SHORT).show();
                            storageRef.child(UUID.randomUUID().toString()).putFile(logoUri)
                                    .addOnCompleteListener(task -> {

                                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                            db.collection("companies")
                                                    .document(company.getFirebaseId()).update("logo", uri.toString()).addOnSuccessListener(aVoid->{

                                                        Log.i("logo upload: ", "Successful");
                                            }).addOnFailureListener(e-> {

                                                Log.i("logo upload: ", "failed");
                                            });


                                        });

                                    })
                                    .addOnFailureListener(e -> {

                                        Log.i("logo upload: ", "failed");
                                    });
                        }

                        //add business photo image


                        if(businessPhotoUri != null) {
                            Toast.makeText(AddCompanyInfo.this, "Business Photo saving in background", Toast.LENGTH_SHORT).show();
                            storageRef.child(UUID.randomUUID().toString()).putFile(businessPhotoUri)
                                    .addOnCompleteListener(task -> {

                                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                            db.collection("companies")
                                                    .document(company.getFirebaseId()).update("photo", uri.toString()).addOnSuccessListener(aVoid->{

                                                Log.i("photo upload: ", "Successful");
                                            }).addOnFailureListener(e-> {

                                                Log.i("photo upload: ", "failed");
                                            });


                                        });

                                    })
                                    .addOnFailureListener(e -> {

                                        Log.i("photo upload: ", "failed");
                                    });
                        }

                        onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Adding Data", "Failed");
                    });
//            Log.i("name", company.getCompanyName());


        }
    }

    private void getImage() {

        //check more info (java and kotlin code) from this resource https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activityResultLauncher.launch(photoPickerIntent);
    }

    public final int READ_STORAGE_REQUEST_CODE = 2549;//this is a random number
    private void requestReadStoragePermission(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_STORAGE_REQUEST_CODE);
        }
        getImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == READ_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //GET IMAGE FROM GALLERY
            getImage();

        }else {
            Toast.makeText(AddCompanyInfo.this, "Sorry could not access images, permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}