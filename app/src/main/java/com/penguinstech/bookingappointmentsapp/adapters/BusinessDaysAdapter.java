package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.BusinessDay;
import com.penguinstech.bookingappointmentsapp.model.BusinessHours;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class    BusinessDaysAdapter extends RecyclerView.Adapter<BusinessDaysAdapter.ViewHolder> {

    private final List<BusinessDay> businessDayList;
    private final Context context;
//    public static final long HOUR = 3600*1000;
    public static final DateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
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



        holder.dayCheckbox.setChecked(businessDay.isChecked());
        enableViews(holder.hoursRV,businessDay.isChecked());//disable views initially

        holder.dayCheckbox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            businessDay.setChecked(isChecked);
            enableViews(holder.hoursRV, isChecked);//disable or enable views on checkbox change

            //remove hours if checkbox is unchecked
            if (!isChecked) {

                ArrayList<BusinessHours> list = new ArrayList<>();
                list.add(businessDay.getListOfBusinessHours().get(0));
                businessDay.setListOfBusinessHours(list);
                updateUi(holder, businessDay.getListOfBusinessHours());
            }

        });

        updateUi(holder,businessDay.getListOfBusinessHours());
    }

    private void enableViews(View v, boolean enabled) {
        //disable all children views recursively
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0;i<vg.getChildCount();i++) {
                enableViews(vg.getChildAt(i), enabled);
            }
        }
        v.setEnabled(enabled);
        v.setClickable(enabled);
    }

    private void updateUi(ViewHolder holder, ArrayList<BusinessHours> list) {

        HoursAdapter adapter = new HoursAdapter(context, list);
        holder.hoursRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.hoursRV.setAdapter(adapter);
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
