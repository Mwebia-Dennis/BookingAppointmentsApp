package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.Service;

import java.util.List;

public class SelectedServiceAdapter extends RecyclerView.Adapter<SelectedServiceAdapter.ViewHolder> {

    public final List<Service> serviceList;
    private final Context context;
    public SelectedServiceAdapter (Context context, List<Service> serviceList) {
        this.serviceList = serviceList;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.selected_service_layout, parent, false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Service service = serviceList.get(position);
        StringBuilder serviceStr = new StringBuilder();
        serviceStr
                .append(service.getServiceName())
                .append("; ")
                .append(service.getHours())
                .append(" hr(s)")
                .append(" ")
                .append(service.getMins())
                .append(" mins")
                .append("-")
                .append("$ ")
                .append(service.getPrice());
        holder.selectedServiceTv.setText(serviceStr);

        holder.removeSelectedServiceBtn.setOnClickListener(v->{

            serviceList.remove(position);
            this.notifyDataSetChanged();
        });
    }





    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView selectedServiceTv;
        ImageButton removeSelectedServiceBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            selectedServiceTv = itemView.findViewById(R.id.selected_service_tv);
            removeSelectedServiceBtn = itemView.findViewById(R.id.removeSelectedServiceBtn);

        }
    }
}


