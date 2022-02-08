package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.penguinstech.bookingappointmentsapp.adapters.BusinessDaysAdapter;
import com.penguinstech.bookingappointmentsapp.model.Appointment;
import com.penguinstech.bookingappointmentsapp.model.BusinessDay;
import com.penguinstech.bookingappointmentsapp.model.BusinessHours;
import com.penguinstech.bookingappointmentsapp.model.Client;
import com.penguinstech.bookingappointmentsapp.model.Company;
import com.penguinstech.bookingappointmentsapp.model.Service;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class AppointmentDatesActivity extends AppCompatActivity {

    static List<Service> selectedServices;
    FirebaseFirestore db;//firestore instance
    String companyId;
    Company company;
    Appointment appointment = new Appointment();
    LinearLayout timeSlotsLL;
    static final String topic = "notifications";
    final static SimpleDateFormat FORMAT = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_dates);

        init();
    }

    private void init() {

        timeSlotsLL = findViewById(R.id.timeSlotsLL);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        companyId = getIntent().getStringExtra("companyId");
        selectedServices = (ArrayList<Service>) getIntent().getSerializableExtra("selectedServicesList");
        List<String> serviceIds = new ArrayList<>();
        float totalPrice = 0;
        for (Service service:selectedServices) {
            serviceIds.add(service.getServiceName());
            totalPrice += Float.parseFloat(service.getPrice());
        }
        //add services and price to appointment
        appointment.setServiceIds(TextUtils.join(",", serviceIds));
        appointment.setTotalPrice(String.valueOf(totalPrice));
        appointment.setDuration(getDuration(selectedServices));

        findViewById(R.id.loader_1).setVisibility(View.VISIBLE);
        db.collection("companies").whereEqualTo("firebaseId", companyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    findViewById(R.id.loader_1).setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {


                        company = queryDocumentSnapshots.toObjects(Company.class).get(0);

                        final Calendar selectedCalendar = Calendar.getInstance(TimeZone.getDefault());
                        //get current time in company timezone id
                        //convert to user timezone
                        final Calendar minDate = Calendar.getInstance(TimeZone.getTimeZone(company.getTimeZoneId()));
                        FORMAT.setTimeZone(TimeZone.getDefault());
                        String date = FORMAT.format(minDate.getTime());
                        try {
                            minDate.setTime(FORMAT.parse(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
                            selectedCalendar.set(Calendar.YEAR, year);
                            selectedCalendar.set(Calendar.MONTH,month);
                            selectedCalendar.set(Calendar.DAY_OF_MONTH,day);
                            updateLabel(selectedCalendar);
                            try {
                                getAvailableTimeSlots(selectedCalendar);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        };

                        findViewById(R.id.selectDateBtn).setOnClickListener(V->{
                            DatePickerDialog datePickerDialog = new DatePickerDialog(
                                    AppointmentDatesActivity.this,
                                    dateSetListener,
                                    selectedCalendar.get(Calendar.YEAR),
                                    selectedCalendar.get(Calendar.MONTH),
                                    selectedCalendar.get(Calendar.DAY_OF_MONTH));
                            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
                            datePickerDialog.show();
                        });
                    }
                }).addOnFailureListener(e-> {
            Toast.makeText(this, "Sorry could not load company info", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.bookAppointment1).setOnClickListener(v->{
            if (appointment.getStartTime() == null) {
                Toast.makeText(AppointmentDatesActivity.this, "Invalid start time", Toast.LENGTH_SHORT).show();
            }else if (appointment.getStartTime().equals("")) {
                Toast.makeText(AppointmentDatesActivity.this, "Invalid start time", Toast.LENGTH_SHORT).show();
            }else {

                /**
                 * if your app has login feature, remove this code
                 * uncomment the code below
                 * remove ClientInformationForm (popup) class
                 */
                new ClientInformationForm(AppointmentDatesActivity.this, appointment, company)
                    .show(getSupportFragmentManager(), "AddClientInfoPopUp");

//                appointment.setClient(
//                        new Client("client full name",
//                                "phone number",
//                                "email"
//                        )
//                );
//
//                //set appointment to server
//                DocumentReference ref = db.collection("company_appointments")
//                        .document(company.getFirebaseId())
//                        .collection("appointments")
//                        .document();
//                appointment.setFirebaseId(ref.getId());
//                appointment.setCompanyId(company.getFirebaseId());
//                ref.set(appointment)
//                        .addOnSuccessListener(documentReference -> {
//                            Toast.makeText(AppointmentDatesActivity.this, "Successfully added appointment", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(AppointmentDatesActivity.this, BookingConfirmationActivity.class);
////                                intent.putExtra("selectedServicesList", (Serializable) selectedServices);
//                            intent.putExtra("appointment", appointment);
//                            intent.putExtra("companyId", company.getFirebaseId());
//                            startActivity(intent);
//
//                        }).addOnFailureListener(e-> {
//                    Toast.makeText(AppointmentDatesActivity.this, "Could not add appointment", Toast.LENGTH_SHORT).show();
//                });
            }
        });

    }

    private void getAvailableTimeSlots(Calendar selectedCalendar) throws ParseException {

        // get the  start hour (00:00 hrs)of the  selected day and last hour (11:59 PM) of the day
        Calendar startHouOfSelectedCalendar = (Calendar) selectedCalendar.clone();
        startHouOfSelectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startHouOfSelectedCalendar.set(Calendar.MINUTE, 0);
        startHouOfSelectedCalendar.set(Calendar.SECOND, 0);
        Calendar endHourOfSelectedCalendar = (Calendar) selectedCalendar.clone();
        endHourOfSelectedCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endHourOfSelectedCalendar.set(Calendar.MINUTE, 59);
        endHourOfSelectedCalendar.set(Calendar.SECOND, 59);

//        convert both resulting dates to providers timezone
        SimpleDateFormat format1 = new SimpleDateFormat("dd-M-yyyy HH:mm:ss", Locale.ENGLISH);
        format1.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
        String date = format1.format(startHouOfSelectedCalendar.getTime());
        String date2 = format1.format(endHourOfSelectedCalendar.getTime());
        startHouOfSelectedCalendar.setTime(format1.parse(date));
        endHourOfSelectedCalendar.setTime(format1.parse(date2));

//        retrieve the day name from the resulting converted start time and lastHour
        format1.applyPattern("EEEE");
        String startHourDayName = format1.format(startHouOfSelectedCalendar.getTime()).toLowerCase();
        String endHourDayName = format1.format(endHourOfSelectedCalendar.getTime()).toLowerCase();


        ArrayList<String> listOfAvailableSlots = new ArrayList<>();
        if(startHourDayName.equals(endHourDayName)){

            //check if day has scheduled slots
            for (int i = 0; i < company.getBusinessDayList().size();i++) {
                BusinessDay businessDay  = company.getBusinessDayList().get(i);
                if (businessDay.getDay().toLowerCase().equals(startHourDayName)) {
                    if (businessDay.isChecked()) {
                        //get the business hours
                        for (BusinessHours hour : businessDay.getListOfBusinessHours()) {
                            //first add the start time
                            //then add one hour to slot as long as its not above end time
                            Calendar startTime = createTime(hour.getStartTime(), startHouOfSelectedCalendar);
                            listOfAvailableSlots.add(FORMAT.format(startTime.getTime()));
                            Calendar endTime = createTime(hour.getEndTime(), startHouOfSelectedCalendar);
                            Calendar nextSlot = createTime(hour.getStartTime(), startHouOfSelectedCalendar);
                            nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                            //add one hour until we exceed the end time

                            while (nextSlot.compareTo(endTime) < 0) {
                                nextSlot.setTimeZone(TimeZone.getTimeZone(startHouOfSelectedCalendar.getTimeZone().getID()));
                                listOfAvailableSlots.add(FORMAT.format(nextSlot.getTime()));
                                nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                            }

                        }



                    }else {
                        Toast.makeText(AppointmentDatesActivity.this, "Sorry there are no available time slots for this day", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }else {

            // retrieve  the start time and end time of selected date
            String[] days = new String[]{startHourDayName, endHourDayName};
            for (int j = 0;  j < days.length;j++) {

                String dayName = days[j];

                for (int i = 0; i < company.getBusinessDayList().size();i++) {
                    BusinessDay businessDay  = company.getBusinessDayList().get(i);
                    if (businessDay.getDay().toLowerCase().equals(dayName)) {
                        if (businessDay.isChecked()) {
                            //get the business hours
                            for (BusinessHours hour : businessDay.getListOfBusinessHours()) {
                                //first add the start time if it is greater then the selected start hour date
                                //then add one hour to slot as long as its not above end time

//                                for the start time day name (eg monday), retrieve all hours that are
//                                greater than our convert start time but less than that days last hour

                                if(j == 0) {

                                    Calendar startOfNextDayHour  = (Calendar) endHourOfSelectedCalendar.clone();
                                    startOfNextDayHour.set(Calendar.HOUR_OF_DAY, 0);
                                    startOfNextDayHour.set(Calendar.MINUTE, 0);
                                    startOfNextDayHour.set(Calendar.SECOND, 0);
                                    Calendar startTime = createTime(hour.getStartTime(), startHouOfSelectedCalendar);
                                    Log.i("startOfNextDayHour", FORMAT.format(startOfNextDayHour.getTime()));
                                    Log.i("startTime", FORMAT.format(startTime.getTime()));
                                    Log.i("startHour", FORMAT.format(startHouOfSelectedCalendar.getTime()));
                                    if (startTime.compareTo(startHouOfSelectedCalendar) > 0 && startTime.compareTo(startOfNextDayHour) < 0) {

                                        Calendar sTime = createTime(hour.getStartTime(), startHouOfSelectedCalendar);
                                        listOfAvailableSlots.add(FORMAT.format(sTime.getTime()));
                                        Calendar endTime = createTime(hour.getEndTime(), startHouOfSelectedCalendar);
                                        Calendar nextSlot = createTime(hour.getStartTime(), startHouOfSelectedCalendar);
                                        nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                                        //add one hour until we exceed the end time

                                        while (nextSlot.compareTo(endTime) < 0) {
                                            listOfAvailableSlots.add(FORMAT.format(nextSlot.getTime()));
                                            nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                                        }

                                    }
                                }else {

//                                    for the last hour day name (eg tuesday), retrieve all end time hours that are
//                                    less than our converted last hour(eg. 02:59) but greater than the days first hour

                                    Calendar startTime = createTime(hour.getStartTime(), endHourOfSelectedCalendar);
                                    Calendar startOfDayHour  = (Calendar) endHourOfSelectedCalendar.clone();
                                    startOfDayHour.set(Calendar.HOUR_OF_DAY, 0);
                                    startOfDayHour.set(Calendar.MINUTE, 0);
                                    startOfDayHour.set(Calendar.SECOND, 0);
                                    if (startTime.compareTo(endHourOfSelectedCalendar) < 0 && startTime.compareTo(startOfDayHour) > 0) {

                                        Calendar sTime = createTime(hour.getStartTime(), startHouOfSelectedCalendar);
                                        listOfAvailableSlots.add(FORMAT.format(sTime.getTime()));
                                        Calendar nextSlot = createTime(hour.getStartTime(), endHourOfSelectedCalendar);
                                        nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                                        Log.i("nextSlot", FORMAT.format(nextSlot.getTime()));
                                        Calendar eTime = createTime(hour.getEndTime(), endHourOfSelectedCalendar);

                                        while (nextSlot.compareTo(eTime) < 0) {
                                            nextSlot.setTimeZone(TimeZone.getTimeZone(endHourOfSelectedCalendar.getTimeZone().getID()));
                                            listOfAvailableSlots.add(FORMAT.format(nextSlot.getTime()));
                                            nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                                        }

                                    }
                                }


                            }



                        }else {
//                            Toast.makeText(AppointmentDatesActivity.this, "Sorry there are no available time slots for this day", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

        }

        getUnbookedAppointments(listOfAvailableSlots, startHouOfSelectedCalendar, endHourOfSelectedCalendar);

    }

    private void  getUnbookedAppointments(List<String> listOfAvailableSlots, Calendar startHourDayDate, Calendar endHourOfSelectedCalendar) {
//get appointments that are already scheduled for this day
        FORMAT.setTimeZone(TimeZone.getTimeZone(startHourDayDate.getTimeZone().getID()));
        db.collection("company_appointments")
                .document(company.getFirebaseId())
                .collection("appointments")
                .whereGreaterThanOrEqualTo("date", FORMAT.format(startHourDayDate.getTime()))
                .whereLessThanOrEqualTo("date", FORMAT.format(endHourOfSelectedCalendar.getTime()))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<Appointment> appointmentArrayList = queryDocumentSnapshots.toObjects(Appointment.class);
                    //check if the timeslot is booked
                    List<String> newAvailableList = new ArrayList<>(listOfAvailableSlots);

                    for (Appointment appointment: appointmentArrayList) {


                        //change availability of the time slots
                        Calendar endTimeOfAppointment = Calendar.getInstance(TimeZone.getTimeZone(company.getTimeZoneId()));
                        try {
                            FORMAT.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
                            endTimeOfAppointment.setTime(FORMAT.parse(appointment.getDate()));
                            //add duration to time
                            String[] duration = appointment.getDuration().split(":");
                            int durationHours = (duration.length > 0)?Integer.parseInt(duration[0]):0;
                            int durationMins = (duration.length > 1)?Integer.parseInt(duration[1]):0;
                            Log.i("endTimeOfAppointment", FORMAT.format(endTimeOfAppointment.getTime()));
                            endTimeOfAppointment.add(Calendar.HOUR_OF_DAY, durationHours);
                            endTimeOfAppointment.add(Calendar.MINUTE, durationMins);
                            endTimeOfAppointment.set(Calendar.SECOND, 0);
                            Log.i("endTimeOfAppointment", FORMAT.format(endTimeOfAppointment.getTime()));

                            for (int j = 0; j < listOfAvailableSlots.size();j++) {

                                String date_time = listOfAvailableSlots.get(j);
                                Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone(company.getTimeZoneId()));
                                endTime.setTime(FORMAT.parse(date_time));

                                if(endTimeOfAppointment.compareTo(endTime) >= 0) {
                                    if(newAvailableList.contains(date_time)) {
                                        //remove data
                                        newAvailableList.remove(newAvailableList.indexOf(date_time));
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    displayAvailableHourSlots(newAvailableList);


                })
                .addOnFailureListener(e-> {
                    Log.i("firebase:error", e.getMessage());
                });
    }

//    private void getAvailableTimeSlots(Calendar selectedCalendar) {
//
//
//        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(selectedCalendar.getTime());
//        //check if day has scheduled slots
//        for (int i = 0; i < company.getBusinessDayList().size();i++) {
//            BusinessDay businessDay  = company.getBusinessDayList().get(i);
//            if (businessDay.getDay().toLowerCase().equals(dayOfWeek.toLowerCase())) {
//                if (businessDay.isChecked()) {
//                    //get the business hours
//                    ArrayList<String> listOfAvailableSlots = new ArrayList<>();
//                    for (BusinessHours hour : businessDay.getListOfBusinessHours()) {
//                        //first add the start time
//                        //then add one hour to slot as long as its not above end time
//                        listOfAvailableSlots.add(hour.getStartTime());
//                        Calendar endTime = createTime(hour.getEndTime());
//                        Calendar nextSlot = createTime(hour.getStartTime());
//                        nextSlot.add(Calendar.HOUR_OF_DAY, 1);
//                        //add one hour until we exceed the end time
//
//                        while (nextSlot.compareTo(endTime) < 0) {
//                            listOfAvailableSlots.add(BusinessDaysAdapter.sdf.format(nextSlot.getTime()));
//                            nextSlot.add(Calendar.HOUR_OF_DAY, 1);
//                        }
//
//                    }
//
//                    getUnbookedAppointments(listOfAvailableSlots);
//
//
//                }else {
//                    Toast.makeText(AppointmentDatesActivity.this, "Sorry there are no available time slots for this day", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//
//
//
//
//
//    }
//
//    private void getUnbookedAppointments(List<String> listOfAvailableSlots) {
//
//        //get appointments that are already scheduled for this day
//        db.collection("company_appointments")
//                .document(company.getFirebaseId())
//                .collection("appointments").whereEqualTo("date", appointment.getDate())
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//
//                    List<Appointment> appointmentArrayList = queryDocumentSnapshots.toObjects(Appointment.class);
//                    //check if the timeslot is booked
//                    List<String> newAvailableList = listOfAvailableSlots;
//
//                    for (Appointment appointment: appointmentArrayList) {
//
//
//                        //change availability of the time slots
//                        Calendar endTimeOfAppointment = createTime(appointment.getStartTime());
//                        //add duration to time
//                        String[] duration = appointment.getDuration().split(":");
//                        int durationHours = (duration.length > 0)?Integer.parseInt(duration[0]):0;
//                        int durationMins = (duration.length > 1)?Integer.parseInt(duration[1]):0;
//                        endTimeOfAppointment.add(Calendar.HOUR_OF_DAY, durationHours);
//                        endTimeOfAppointment.add(Calendar.MINUTE, durationMins);
//
//                        for (int j = 0; j < listOfAvailableSlots.size();j++) {
//
//                            String hour = listOfAvailableSlots.get(j);
//                            Calendar endTime = createTime(hour);
//
//                            if(endTimeOfAppointment.compareTo(endTime) >= 0) {
//                                if(newAvailableList.contains(hour)) newAvailableList.remove(newAvailableList.indexOf(hour));
//                            }
//                        }
//
//                    }
//
//                    displayAvailableHourSlots(newAvailableList);
//
//
//                });
//    }


    private void displayAvailableHourSlots(List<String> listOfAvailableSlots) {

        timeSlotsLL.removeAllViews();
        //display dates
        if (listOfAvailableSlots.size() > 0) {

            for (String time: listOfAvailableSlots) {

                //convert dates back to user default timezone
                Calendar new_time = Calendar.getInstance(TimeZone.getTimeZone(company.getTimeZoneId()));
                try {
                    FORMAT.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
                    new_time.setTime(FORMAT.parse(time));
                    BusinessDaysAdapter.sdf.setTimeZone(TimeZone.getDefault());
                    timeSlotsLL.addView(createButton(BusinessDaysAdapter.sdf.format(new_time.getTime())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }else {
            Toast.makeText(AppointmentDatesActivity.this, "Sorry All slots have been booked", Toast.LENGTH_SHORT).show();

        }
    }


    private void updateLabel(Calendar myCalendar){
//        String myFormat="MM/dd/yy";
//        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
//        dateFormat.applyPattern("EEE, d MMM yyyy");

//        FORMAT.setTimeZone(TimeZone.getDefault());
//        String date = FORMAT.format()
        FORMAT.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
        appointment.setDate(FORMAT.format(myCalendar.getTime()));
        Log.i("DATE", FORMAT.format(myCalendar.getTime()));
        TextView tv = findViewById(R.id.selectedDateTv);
        tv.setText(new StringBuilder().append("Selected Date is: ").append(FORMAT.format(myCalendar.getTime())));
    }

    private Button createButton(String txt) {
        Button button = new Button(AppointmentDatesActivity.this);
        button.setText(txt);
        button.setOnClickListener(v->{
            appointment.setStartTime(txt);
            // update date

            Calendar date = Calendar.getInstance(TimeZone.getDefault());
            try {
                //get appointment date
                FORMAT.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
                date.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
                date.setTime(FORMAT.parse(appointment.getDate()));
                FORMAT.setTimeZone(TimeZone.getDefault());
                //set time
                Calendar dateTime = createTime(txt, Calendar.getInstance(TimeZone.getDefault()));
                //convert to company timezone and save
                BusinessDaysAdapter.sdf.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
                String timeStr = BusinessDaysAdapter.sdf.format(dateTime.getTime());
                Calendar _dateTime = createTime(timeStr, date);
                FORMAT.setTimeZone(TimeZone.getTimeZone(company.getTimeZoneId()));
                appointment.setDate(FORMAT.format(_dateTime.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //change active background color
            //set all buttons to grey
            //add active color to clicked button
            for (int i = 0; i < timeSlotsLL.getChildCount(); i++) {
                View btnView = timeSlotsLL.getChildAt(i);
                if (btnView instanceof Button){
                    Button btn = (Button) btnView;
                    btn.setBackgroundResource(android.R.drawable.btn_default);;
                }
            }

            v.setBackgroundResource(R.color.purple_200);
        });
        return button;
    }

    private static Calendar createTime(String time, Calendar calendar) {

        Calendar mCalendar = (Calendar) calendar.clone();
        mCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
        mCalendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1].split(" ")[0]));
        mCalendar.set(Calendar.SECOND, 0);
        Log.i("mCalendar", FORMAT.format(mCalendar.getTime()));
        return (Calendar) mCalendar.clone(); //return a clone so as it can be created as a separate variable
    }

    private String getDuration(List<Service> selectedServices) {
        int hours = 0;
        int minutes = 0;
        for (Service service: selectedServices) {
            if (Integer.parseInt(service.getMins()) > 0) {
                minutes += Integer.parseInt(service.getMins());
                if (minutes >= 60) {
                    hours++;
                    minutes %= 60;
                }
            }

            if (Integer.parseInt(service.getHours()) > 0) {
                hours += Integer.parseInt(service.getHours());
            }
        }

        return hours + ":" + minutes;
    }

    public  static class ClientInformationForm extends BottomSheetDialogFragment {

        private final Context context;
        private final Appointment appointment;
        private final FirebaseFirestore db;//firestore instance
        private final Company company;
        private final ClientInformationForm clientInformationForm = this;

        public ClientInformationForm(Context context, Appointment appointment, Company company) {
            this.appointment = appointment;
            this.context = context;
            this.company = company;
            db = FirebaseFirestore.getInstance();
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.client_contact_layout_form, container, false);
            EditText fullNameEt = view.findViewById(R.id.fullNameEt);
            EditText emailEt = view.findViewById(R.id.emailEt);
            EditText phoneNoEt = view.findViewById(R.id.phoneNoEt);
            view.findViewById(R.id.bookAppointmentBtn).setOnClickListener(v->{

                if(!fullNameEt.getText().toString().trim().equals("") && !emailEt.getText().toString().trim().equals("")
                    && !phoneNoEt.getText().toString().trim().equals("")
                ) {

                    appointment.setClient(
                            new Client(fullNameEt.getText().toString(),
                                    phoneNoEt.getText().toString(),
                                    emailEt.getText().toString()
                            )
                    );

                    //set appointment to server
                    DocumentReference ref = db.collection("company_appointments")
                            .document(company.getFirebaseId())
                            .collection("appointments")
                            .document();
                    appointment.setFirebaseId(ref.getId());
                    appointment.setCompanyId(company.getFirebaseId());
                    ref.set(appointment)
                            .addOnSuccessListener(documentReference -> {
                                clientInformationForm.dismiss();
                                Intent intent = new Intent(context, BookingConfirmationActivity.class);
//                                intent.putExtra("selectedServicesList", (Serializable) selectedServices);
                                intent.putExtra("appointment", appointment);
                                intent.putExtra("companyId", company.getFirebaseId());
                                startActivity(intent);

                            }).addOnFailureListener(e-> {
                        Toast.makeText(context, "Could not add appointment", Toast.LENGTH_SHORT).show();
                    });


                }else {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
    }
}