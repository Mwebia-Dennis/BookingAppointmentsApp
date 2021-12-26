package com.penguinstech.bookingappointmentsapp;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

public class HoursAdapter  extends RecyclerView.Adapter<HoursAdapter.ViewHolder> {

    private final List<BusinessHours> businessHourList;
    private final Context context;
    private final HoursAdapter classContext = this;

    public HoursAdapter (Context context, List<BusinessHours> businessHourList) {
        this.businessHourList = businessHourList;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.hours_layout, parent, false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BusinessHours businessHours = businessHourList.get(position);
        holder.startTimeTv.setText(businessHours.getStartTime());
        holder.endTimeTv.setText(businessHours.getEndTime());
//        Log.i("position", String.valueOf(position));
//        if(position == 0) {
//            holder.removeHourBtn.setVisibility(View.GONE);
//        }else {
//
//            holder.addNewHourBtn.setVisibility(View.GONE);
//        }


        holder.startTimeTv.setOnClickListener(v->{
            changeStartTime(position, v);
        });

        holder.endTimeTv.setOnClickListener(v->{
            changeEndTime(position, v);
        });

        holder.addNewHourBtn.setOnClickListener(v->{
            String endTime = businessHourList.get(businessHourList.size()-1).getEndTime();
            int hr = Integer.parseInt(endTime.split(":")[0]);
            if(hr< 12 && endTime.split(":")[1].split(" ")[1].toLowerCase().equals("pm")) {
                hr += 12;
            }
            if (hr == 12 && endTime.split(":")[1].split(" ")[1].toLowerCase().equals("am")) {

                Snackbar.make(v,"Hey, 11.59 PM is the last hour of the day", Snackbar.LENGTH_LONG).show();
            }else {

                int mins = Integer.parseInt(endTime.split(":")[1].split(" ")[0]);
                Calendar nweStartTime = Calendar.getInstance();
                nweStartTime.set(Calendar.HOUR_OF_DAY, hr);
                nweStartTime.set(Calendar.MINUTE, mins);
                nweStartTime.set(Calendar.SECOND, 0);
                nweStartTime.set(Calendar.MILLISECOND, 0);

                Calendar newEndTime = (Calendar) nweStartTime.clone();
                newEndTime.set(Calendar.HOUR_OF_DAY, hr + 1);

                Log.i("new day", BusinessDaysAdapter.sdf.format(nweStartTime.getTime()));
                businessHourList.add(new BusinessHours(BusinessDaysAdapter.sdf.format(nweStartTime.getTime()),
                        BusinessDaysAdapter.sdf.format(newEndTime.getTime())));
                this.notifyDataSetChanged();
            }
        });
        holder.removeHourBtn.setOnClickListener(v->{
            businessHourList.remove(position);
            this.notifyDataSetChanged();
            Toast.makeText(context, "Hour removed", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public int getItemCount() {
        return businessHourList.size();
    }

    private void changeStartTime( int position, View v) {
        String currentTime = businessHourList.get(position).getStartTime();
        Calendar mcurrentTime = Calendar.getInstance();
        mcurrentTime.set(Calendar.HOUR, Integer.parseInt(currentTime.split(":")[0]));
        mcurrentTime.set(Calendar.MINUTE, Integer.parseInt(currentTime.split(":")[1].split(" ")[0]));
        mcurrentTime.set(Calendar.AM_PM,
                currentTime.split(":")[1].split(" ")[1].toLowerCase().equals("am")?Calendar.AM:Calendar.PM
        );
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (timePicker, selectedHour, selectedMinute) -> {

            Log.i("selected hour", String.valueOf(selectedHour));


            //create a date from the selected date
            Calendar newStartTime = Calendar.getInstance();
            newStartTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            newStartTime.set(Calendar.MINUTE, selectedMinute);
            newStartTime.set(Calendar.SECOND, 0);
            newStartTime.set(Calendar.MILLISECOND, 0);

            Calendar previousTime = null;
            Calendar nextStartTime = null;
            if(position > 0 ) {

                //get the start time of the previously scheduled hour before the current one
                String endTime = businessHourList.get(position-1).getEndTime();
                previousTime = Calendar.getInstance();
                previousTime.set(Calendar.HOUR, Integer.parseInt(endTime.split(":")[0]));
                previousTime.set(Calendar.MINUTE, Integer.parseInt(endTime.split(":")[1].split(" ")[0]));
                previousTime.set(Calendar.SECOND, 0);
                previousTime.set(Calendar.MILLISECOND, 0);
                previousTime.set(Calendar.AM_PM,
                        endTime.split(":")[1].split(" ")[1].toLowerCase().equals("am")?Calendar.AM:Calendar.PM
                        );

            }
            if (position < businessHourList.size()-1 ) {

                //get the start time of the next scheduled hour after the current one
                String startTime = businessHourList.get(position+1).getStartTime();
                nextStartTime = Calendar.getInstance();
                nextStartTime.set(Calendar.HOUR, Integer.parseInt(startTime.split(":")[0]));
                nextStartTime.set(Calendar.MINUTE, Integer.parseInt(startTime.split(":")[1].split(" ")[0]));
                nextStartTime.set(Calendar.SECOND, 0);
                nextStartTime.set(Calendar.MILLISECOND, 0);
                nextStartTime.set(Calendar.AM_PM,
                        startTime.split(":")[1].split(" ")[1].toLowerCase().equals("am")?Calendar.AM:Calendar.PM
                );

            }

            if(previousTime != null && newStartTime.compareTo(previousTime) < 0) {
                //if selected time is below the previous meeting start time, warn user
                //newStartTime is less than previousTime
                Snackbar.make(v, "You already have a meeting scheduled for this hour "+BusinessDaysAdapter.sdf.format(newStartTime.getTime()),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            if(nextStartTime != null && newStartTime.compareTo(nextStartTime) > 0) {
                //if selected time is below the previous meeting start time, warn user
                //newStartTime is less than previousTime
                Snackbar.make(v, "You already have a meeting scheduled for this hour "+BusinessDaysAdapter.sdf.format(newStartTime.getTime()),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            //set the time on textView
            Calendar endTime = null;
            if(nextStartTime !=null){
                endTime = (Calendar) nextStartTime.clone();
            }else {
                endTime = (Calendar) newStartTime.clone();
                endTime.set(Calendar.HOUR_OF_DAY, selectedHour+1);
            }
            businessHourList.set(position, new BusinessHours(BusinessDaysAdapter.sdf.format(newStartTime.getTime()),
                    BusinessDaysAdapter.sdf.format(endTime.getTime())));
            this.notifyDataSetChanged();

        }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE), false);
        timePickerDialog.setTitle("Select Start Time");
        timePickerDialog.show();
    }
    private void changeEndTime( int position, View v) {
        String currentTime = businessHourList.get(position).getEndTime();
        Calendar mcurrentTime = Calendar.getInstance();
        mcurrentTime.set(Calendar.HOUR, Integer.parseInt(currentTime.split(":")[0]));
        mcurrentTime.set(Calendar.MINUTE, Integer.parseInt(currentTime.split(":")[1].split(" ")[0]));
        mcurrentTime.set(Calendar.AM_PM,
                currentTime.split(":")[1].split(" ")[1].toLowerCase().equals("am")?Calendar.AM:Calendar.PM
        );
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (timePicker, selectedHour, selectedMinute) -> {

            Log.i("selected hour", String.valueOf(selectedHour));


            //create a date from the selected date
            Calendar newEndTime = Calendar.getInstance();
            newEndTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            newEndTime.set(Calendar.MINUTE, selectedMinute);
            newEndTime.set(Calendar.SECOND, 0);
            newEndTime.set(Calendar.MILLISECOND, 0);

            //get the start time of current holder
            String currentStartTime = businessHourList.get(position-1).getStartTime();
            Calendar previousTime = Calendar.getInstance();
            previousTime.set(Calendar.HOUR, Integer.parseInt(currentStartTime.split(":")[0]));
            previousTime.set(Calendar.MINUTE, Integer.parseInt(currentStartTime.split(":")[1].split(" ")[0]));
            previousTime.set(Calendar.AM_PM,
                    currentStartTime.split(":")[1].split(" ")[1].toLowerCase().equals("am")?Calendar.AM:Calendar.PM
            );


            Calendar nextStartTime = null;
            if (position < businessHourList.size()-1 ) {

                //get the start time of the next scheduled hour after the current one
                String startTime = businessHourList.get(position+1).getStartTime();
                nextStartTime = Calendar.getInstance();
                nextStartTime.set(Calendar.HOUR, Integer.parseInt(startTime.split(":")[0]));
                nextStartTime.set(Calendar.MINUTE, Integer.parseInt(startTime.split(":")[1].split(" ")[0]));
                nextStartTime.set(Calendar.SECOND, 0);
                nextStartTime.set(Calendar.MILLISECOND, 0);
                nextStartTime.set(Calendar.AM_PM,
                        startTime.split(":")[1].split(" ")[1].toLowerCase().equals("am")?Calendar.AM:Calendar.PM
                );

            }

            if(newEndTime.compareTo(previousTime) < 0) {
                //if selected time is below the previous meeting start time, warn user
                //newStartTime is less than previousTime
                Snackbar.make(v, "You have already scheduled for this hour "+BusinessDaysAdapter.sdf.format(newEndTime.getTime()),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            if(nextStartTime != null && newEndTime.compareTo(nextStartTime) > 0) {
                //if selected time is below the previous meeting start time, warn user
                //newStartTime is less than previousTime
                Snackbar.make(v, "You have already scheduled for this hour "+BusinessDaysAdapter.sdf.format(newEndTime.getTime()),
                        Snackbar.LENGTH_LONG).show();
                return;
            }
            //set the time on textView
            BusinessHours oldHour = businessHourList.get(position);
            businessHourList.set(position, new BusinessHours(oldHour.getStartTime(),
                    BusinessDaysAdapter.sdf.format(newEndTime.getTime())));
            this.notifyDataSetChanged();

        }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE), false);
        timePickerDialog.setTitle("Select Start Time");
        timePickerDialog.show();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView startTimeTv, endTimeTv;
        ImageButton removeHourBtn, addNewHourBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            startTimeTv = itemView.findViewById(R.id.startTimeTv);
            endTimeTv = itemView.findViewById(R.id.endTimeTv);
            addNewHourBtn = itemView.findViewById(R.id.addNewHourBtn);
            removeHourBtn = itemView.findViewById(R.id.removeHourBtn);

        }
    }
}
