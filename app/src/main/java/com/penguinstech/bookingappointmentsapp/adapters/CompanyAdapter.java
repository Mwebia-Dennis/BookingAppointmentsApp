package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.AddCompanyInfo;
import com.penguinstech.bookingappointmentsapp.CompanyDetails;
import com.penguinstech.bookingappointmentsapp.NotificationsActivity;
import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.Appointment;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.NotificationStatus;

import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {

    private final List<Company> companyList;
    private final Context context;

    public CompanyAdapter (Context context, List<Company> companyList) {
        this.companyList = companyList;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.company_layout, parent, false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Company company = companyList.get(position);
        holder.companyNameTv.setText(company.getCompanyName());
        holder.ownerNameTv.setText(company.getOwnerName());
        if (company.getLogo() != null && !company.getLogo().equals("")) {

            Glide.with(context).load(company.getLogo()).into(holder.businessLogo);

        }
        holder.notificationBadge.setOnClickListener(v->{
            Intent intent = new Intent(context, NotificationsActivity.class);
            intent.putExtra("companyId", company.getFirebaseId());
            context.startActivity(intent);
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), CompanyDetails.class);
            intent.putExtra("companyDetails", company);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.editCompanyBtn.setOnClickListener(_v->{

            Intent intent = new Intent(holder.itemView.getContext(), AddCompanyInfo.class);
            intent.putExtra("companyDetails", company);
            holder.itemView.getContext().startActivity(intent);
        });

        loadNewNotifications(company.getFirebaseId(),holder.notificationBadgeTv);
    }

    private void loadNewNotifications(String companyId, TextView notificationBadgeTv) {
        FirebaseFirestore db;//firestore instance
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
        db.collection("company_appointments")
                .document(companyId)
                .collection("appointments")
                .whereEqualTo("notificationStatus", NotificationStatus.UNREAD)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        //set the notification count
                        List<Appointment> list = queryDocumentSnapshots.toObjects(Appointment.class);
                        notificationBadgeTv.setText(String.valueOf(list.size()));
                    }


                }).addOnFailureListener(e->{
            Log.i("error", e.getMessage());
        });
    }


    @Override
    public int getItemCount() {
        return companyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyNameTv, ownerNameTv, notificationBadgeTv;
        ImageView businessLogo, notificationBadge;
        AppCompatButton editCompanyBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            companyNameTv = itemView.findViewById(R.id.company_name_tv);
            ownerNameTv = itemView.findViewById(R.id.owner_tv);
            businessLogo = itemView.findViewById(R.id.business_logo);
            notificationBadge = itemView.findViewById(R.id.notificationBadge);
            notificationBadgeTv = itemView.findViewById(R.id.notificationBadgeTv);
            editCompanyBtn = itemView.findViewById(R.id.editCompanyBtn);

        }
    }
}
