package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
    public ServicesAdapter (Context context, List<Service> serviceList) {
        this.serviceList = serviceList;
        this.context = context;
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
    }





    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, priceTv, durationTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.serviceName);
            priceTv = itemView.findViewById(R.id.priceTextView);
            durationTv = itemView.findViewById(R.id.durationTv);

        }
    }
}

