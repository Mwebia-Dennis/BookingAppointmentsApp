package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.BusinessDay;
import com.penguinstech.bookingappointmentsapp.model.BusinessHours;
import com.penguinstech.bookingappointmentsapp.model.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ViewHolder> {

    private final List<Service> serviceList;
    private final Context context;
    private final boolean isSelectingService;
    private final SelectedServiceAdapter selectedServiceAdapter;
    public ServicesAdapter (Context context, List<Service> serviceList, boolean isSelectingService, SelectedServiceAdapter selectedServiceAdapter) {
        this.serviceList = serviceList;
        this.context = context;
        this.isSelectingService = isSelectingService;
        this.selectedServiceAdapter = selectedServiceAdapter;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.service_layout, parent, false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Service service = serviceList.get(position);
        holder.nameTv.setText(service.getServiceName());
        holder.priceTv.setText(service.getPrice());
        StringBuilder duration = new StringBuilder();
        duration.append(service.getHours())
                .append(" hr(s)")
                .append(" ").append(service.getMins()).append(" mins");
        holder.durationTv.setText(duration);

        holder.selectServiceBtn.setOnClickListener(v->{
            //add selected service to list and update UI
            selectedServiceAdapter.serviceList.add(service);
            selectedServiceAdapter.notifyDataSetChanged();
        });

        if(!isSelectingService) {
            holder.selectServiceBtn.setVisibility(View.GONE);
        }
    }





    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, priceTv, durationTv;
        ImageButton selectServiceBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.serviceName);
            priceTv = itemView.findViewById(R.id.priceTextView);
            durationTv = itemView.findViewById(R.id.durationTv);
            selectServiceBtn = itemView.findViewById(R.id.selectServiceBtn);

        }
    }
}

