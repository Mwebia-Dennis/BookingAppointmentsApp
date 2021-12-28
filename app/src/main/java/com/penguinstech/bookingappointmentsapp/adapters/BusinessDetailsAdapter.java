package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.penguinstech.bookingappointmentsapp.CompanyDetails;
import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.Company;

import java.util.List;

public class BusinessDetailsAdapter  extends RecyclerView.Adapter<BusinessDetailsAdapter.ViewHolder> {

    private final List<String> detailList;
    private final Context context;

    public BusinessDetailsAdapter (Context context, List<String> detailList) {
        this.detailList = detailList;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.single_detail_layout, parent, false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.detailTv.setText(detailList.get(position));
    }


    @Override
    public int getItemCount() {
        return detailList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView detailTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            detailTv = itemView.findViewById(R.id.detailTv);

        }
    }
}
