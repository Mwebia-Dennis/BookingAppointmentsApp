package com.penguinstech.bookingappointmentsapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BusinessDaysAdapter extends RecyclerView.Adapter<BusinessDaysAdapter.ViewHolder> {

    private final List<BusinessDay> businessDayList;
    private final Context context;
    public static final long HOUR = 3600*1000;
    public static final DateFormat sdf = new SimpleDateFormat("hh:mm a");
    public BusinessDaysAdapter (Context context, List<BusinessDay> businessDayList) {
        this.businessDayList = businessDayList;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.business_day_layout, parent, false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BusinessDay businessDay = businessDayList.get(position);
        holder.dayCheckbox.setText(businessDay.getDay());


        ArrayList<BusinessHours> businessHourList = getHours();
        updateUi(holder,businessHourList);
    }

    private void updateUi(ViewHolder holder, ArrayList<BusinessHours> list) {

        HoursAdapter adapter = new HoursAdapter(context, list);
        holder.hoursRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.hoursRV.setAdapter(adapter);
    }

    private ArrayList<BusinessHours> getHours() {
        ArrayList<BusinessHours> list = new ArrayList<>();
        list.clear();

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
        list.add(new BusinessHours(sdf.format(startTime.getTime()),
                sdf.format(endTime.getTime())));
        return list;
    }

    public void addBusinessHours() {

        //get last hour end time
        //add an hour and use it as start time to next hour
        //add 2 hours to use the it as end time

    }


    @Override
    public int getItemCount() {
        return businessDayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox dayCheckbox;
        RecyclerView hoursRV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayCheckbox = itemView.findViewById(R.id.dayCheckbox);
            hoursRV = itemView.findViewById(R.id.hoursRV);

        }
    }
}
