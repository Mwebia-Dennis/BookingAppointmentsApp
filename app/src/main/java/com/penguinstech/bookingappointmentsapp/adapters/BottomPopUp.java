package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.MainActivity;
import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.Service;

import java.util.List;

public class BottomPopUp extends BottomSheetDialogFragment {


    FirebaseFirestore db;//firestore instance
    Context context;
    BottomPopUp bottomPopUp = this;
    boolean isForm;
    String companyId;
    public BottomPopUp(Context context, String companyId,  boolean isForm) {

        this.context = context;
        this.companyId = companyId;
        this.isForm = isForm;
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (isForm) {
            return inflateAddServiceForm(inflater, container);
        }else {
            return inflateShowServicesLayout(inflater, container);
        }
    }

    public View inflateAddServiceForm(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        View v = inflater.inflate(R.layout.new_service_layout,
                container, false);

        v.findViewById(R.id.add_service_btn).setOnClickListener(view->{

            TextView priceTv, hoursTV, minsTv, nameTv;
            priceTv = v.findViewById(R.id.priceTv);
            hoursTV = v.findViewById(R.id.hourTv);
            minsTv = v.findViewById(R.id.minsTv);
            nameTv = v.findViewById(R.id.serviceNameTv);

            if(!priceTv.getText().toString().trim().equals("")
                    && !nameTv.getText().toString().trim().equals("")
                    && !hoursTV.getText().toString().trim().equals("")
                    && !minsTv.getText().toString().trim().equals("")){

                //add service to firebase
                Toast.makeText(context, "Adding Service, please wait...", Toast.LENGTH_SHORT).show();
                DocumentReference ref = db.collection("company_services")
                        .document(companyId)
                        .collection("services")
                        .document();
                ref.set(new Service(ref.getId(), nameTv.getText().toString() ,priceTv.getText().toString(),hoursTV.getText().toString(),minsTv.getText().toString()))
                        .addOnSuccessListener(documentReference -> {

                            bottomPopUp.dismiss();
                            Toast.makeText(context, "Successfully added service", Toast.LENGTH_SHORT).show();

                        }).addOnFailureListener(e-> {
                    Toast.makeText(context, "Could not add service", Toast.LENGTH_SHORT).show();
                });

            }else {
                Snackbar.make(view, "All fields are required", Snackbar.LENGTH_LONG ).show();
            }

        });



        return v;
    }


    public View inflateShowServicesLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {

        View v = inflater.inflate(R.layout.show_service_layout,
                container, false);

        db.collection("company_services")
                .document(companyId)
                .collection("services").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        // get all data and display

                        //convert whole queryDocumentSnapshots to list
                        v.findViewById(R.id.loader).setVisibility(View.GONE);
                        List<Service> serviceList = queryDocumentSnapshots.toObjects(Service.class);
                        RecyclerView containerRv = v.findViewById(R.id.containerRv);
                        containerRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                        ServicesAdapter adapter = new ServicesAdapter(context, serviceList);
                        containerRv.setAdapter(adapter);
                    }


                }).addOnFailureListener(e->{
            Log.i("error", e.getMessage());
        });




        return v;
    }

}
