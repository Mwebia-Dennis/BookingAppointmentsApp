package com.penguinstech.bookingappointmentsapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.AppointmentDatesActivity;
import com.penguinstech.bookingappointmentsapp.BookingConfirmationActivity;
import com.penguinstech.bookingappointmentsapp.R;
import com.penguinstech.bookingappointmentsapp.model.Appointment;
import com.penguinstech.bookingappointmentsapp.model.AppointmentStatus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Appointment> appointmentList;
    private final Context context;
    private final FirebaseFirestore db;//firestore instance

    public NotificationAdapter (Context context, List<Appointment> appointmentList) {
        this.appointmentList = appointmentList;
        this.context = context;
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.notification_layout, parent, false);
        return new ViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Appointment appointment = appointmentList.get(position);
        holder.msgTv.setText(new StringBuilder().append(appointment.getClient().getFullName())
                .append(" requested an appointment with your company"));

        SimpleDateFormat dateFormat=new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(
                appointment.getCompanyTimeZone() != null
                        ? appointment.getCompanyTimeZone()
                        : TimeZone.getDefault().getID()
        ));
        try {
            calendar.setTime(dateFormat.parse(appointment.getDate()));
            Calendar time = AppointmentDatesActivity.createTime(appointment.getStartTime());
            calendar.set(Calendar.HOUR, time.get(Calendar.HOUR));
            calendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            calendar.set(Calendar.AM_PM, time.get(Calendar.AM_PM));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateFormat.applyPattern("EEE, d MMM yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        holder.appointmentDetailsTv.setText(new StringBuilder().append(dateFormat.format(calendar.getTime()))
                .append(". Services: ")
                .append(appointment.getServiceIds())
        );

        //setting status label
        holder.statusTv.setText(appointment.getAppointmentStatus());
        if (appointment.getAppointmentStatus().equals(AppointmentStatus.ACCEPTED)) {
            holder.statusTv.setTextColor(Color.GREEN);
        }else if (appointment.getAppointmentStatus().equals(AppointmentStatus.CANCELLED)) {
            holder.statusTv.setTextColor(Color.RED);
        }

        //setting up buttons
        if (!appointment.getAppointmentStatus().equals(AppointmentStatus.PENDING)) {

            holder.buttonContainerLL.setVisibility(View.GONE);
        }else {

            holder.acceptBtn.setOnClickListener(v-> {

                db.collection("company_appointments")
                        .document(appointment.getCompanyId())
                        .collection("appointments")
                        .document(appointment.getFirebaseId()).update("appointmentStatus", AppointmentStatus.ACCEPTED)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Appointment Accepted Successfully", Toast.LENGTH_SHORT).show();
                            holder.buttonContainerLL.setVisibility(View.GONE);
                            holder.statusTv.setText(AppointmentStatus.ACCEPTED);
                            holder.statusTv.setTextColor(Color.GREEN);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Sorry could not accept appointment, try again later", Toast.LENGTH_SHORT).show();
                        });
            });

            holder.declineBtn.setOnClickListener(v-> {

                db.collection("company_appointments")
                        .document(appointment.getCompanyId())
                        .collection("appointments")
                        .document(appointment.getFirebaseId()).update("appointmentStatus", AppointmentStatus.CANCELLED)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Appointment declined Successfully", Toast.LENGTH_SHORT).show();
                            holder.buttonContainerLL.setVisibility(View.GONE);
                            holder.statusTv.setText(AppointmentStatus.CANCELLED);
                            holder.statusTv.setTextColor(Color.RED);
                        }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Sorry could not decline appointment, try again later", Toast.LENGTH_SHORT).show();
                });
            });
        }

    }



    @Override
    public int getItemCount() {
        return appointmentList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView msgTv, appointmentDetailsTv,statusTv;
        Button acceptBtn;
        AppCompatButton declineBtn;
        LinearLayout buttonContainerLL;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            msgTv = itemView.findViewById(R.id.messageTv);
            appointmentDetailsTv = itemView.findViewById(R.id.appointmentDetailsTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            declineBtn = itemView.findViewById(R.id.declineBtn);
            buttonContainerLL = itemView.findViewById(R.id.buttonContainerLL);

        }
    }
}

