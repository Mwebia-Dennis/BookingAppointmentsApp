package com.penguinstech.bookingappointmentsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AppointmentDatesActivity extends AppCompatActivity {

    static List<Service> selectedServices;
    FirebaseFirestore db;//firestore instance
    String companyId;
    Company company;
    Appointment appointment = new Appointment();
    LinearLayout timeSlotsLL;
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

                        final Calendar selectedCalendar = Calendar.getInstance();

                        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
                            selectedCalendar.set(Calendar.YEAR, year);
                            selectedCalendar.set(Calendar.MONTH,month);
                            selectedCalendar.set(Calendar.DAY_OF_MONTH,day);
                            updateLabel(selectedCalendar);
                            getAvailableTimeSlots(selectedCalendar);

                        };

                        findViewById(R.id.selectDateBtn).setOnClickListener(V->{
                            DatePickerDialog datePickerDialog = new DatePickerDialog(
                                    AppointmentDatesActivity.this,
                                    dateSetListener,
                                    selectedCalendar.get(Calendar.YEAR),
                                    selectedCalendar.get(Calendar.MONTH),
                                    selectedCalendar.get(Calendar.DAY_OF_MONTH));
                            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
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

    private void getAvailableTimeSlots(Calendar selectedCalendar) {


        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(selectedCalendar.getTime());
        //check if day has scheduled slots
        for (int i = 0; i < company.getBusinessDayList().size();i++) {
            BusinessDay businessDay  = company.getBusinessDayList().get(i);
            if (businessDay.getDay().toLowerCase().equals(dayOfWeek.toLowerCase())) {
                if (businessDay.isChecked()) {
                    //get the business hours
                    ArrayList<String> listOfAvailableSlots = new ArrayList<>();
                    for (BusinessHours hour : businessDay.getListOfBusinessHours()) {
                        //first add the start time
                        //then add one hour to slot as long as its not above end time
                        listOfAvailableSlots.add(hour.getStartTime());
                        Calendar endTime = createTime(hour.getEndTime());
                        Calendar nextSlot = createTime(hour.getStartTime());
                        nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                        //add one hour until we exceed the end time

                        while (nextSlot.compareTo(endTime) < 0) {
                            listOfAvailableSlots.add(BusinessDaysAdapter.sdf.format(nextSlot.getTime()));
                            nextSlot.add(Calendar.HOUR_OF_DAY, 1);
                        }

                    }

                    getUnbookedAppointments(listOfAvailableSlots);


                }else {
                    Toast.makeText(AppointmentDatesActivity.this, "Sorry there are no available time slots for this day", Toast.LENGTH_SHORT).show();
                }
            }
        }





    }

    private void getUnbookedAppointments(List<String> listOfAvailableSlots) {

        //get appointments that are already scheduled for this day
        db.collection("company_appointments")
                .document(company.getFirebaseId())
                .collection("appointments").whereEqualTo("date", appointment.getDate())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<Appointment> appointmentArrayList = queryDocumentSnapshots.toObjects(Appointment.class);
                    //check if the timeslot is booked
//                    List<String> availableSlots = listOfAvailableSlots;
                    for (Appointment appointment: appointmentArrayList) {


                        //change availability of the time slots
                        Calendar endTimeOfAppointment = createTime(appointment.getStartTime());
                        //add duration to time
                        int durationHours = Integer.parseInt(appointment.getDuration().split(":")[0]);
                        int durationMins = Integer.parseInt(appointment.getDuration().split(":")[1]);
                        endTimeOfAppointment.add(Calendar.HOUR_OF_DAY, durationHours);
                        endTimeOfAppointment.add(Calendar.MINUTE, durationMins);

                        for (int j = 0; j < listOfAvailableSlots.size();j++) {

                            String hour = listOfAvailableSlots.get(j);
                            Calendar endTime = createTime(hour);

                            if(endTimeOfAppointment.compareTo(endTime) >= 0) {
                                listOfAvailableSlots.remove(j);
                            }
                        }

                    }

                    displayAvailableHourSlots(listOfAvailableSlots);


                });
    }


    private void displayAvailableHourSlots(List<String> listOfAvailableSlots) {

        timeSlotsLL.removeAllViews();
        //display dates
        if (listOfAvailableSlots.size() > 0) {

            for (String hour: listOfAvailableSlots) {
                timeSlotsLL.addView(createButton(hour));
            }

        }else {
            Toast.makeText(AppointmentDatesActivity.this, "Sorry All slots have been booked", Toast.LENGTH_SHORT).show();

        }
    }


    private void updateLabel(Calendar myCalendar){
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        dateFormat.applyPattern("EEE, d MMM yyyy");
        appointment.setDate(dateFormat.format(myCalendar.getTime()));
        TextView tv = findViewById(R.id.selectedDateTv);
        tv.setText(new StringBuilder().append("Selected Date is: ").append(dateFormat.format(myCalendar.getTime())));
    }

    private Button createButton(String txt) {
        Button button = new Button(AppointmentDatesActivity.this);
        button.setText(txt);
        button.setOnClickListener(v->{
            appointment.setStartTime(txt);
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

    private static Calendar createTime(String time) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1].split(" ")[0]));
        calendar.set(Calendar.AM_PM,
                time.split(":")[1].split(" ")[1].toLowerCase().equals("am")?Calendar.AM:Calendar.PM
        );
        return calendar;
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
            FirebaseApp.initializeApp(context);
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
                                Toast.makeText(context, "Successfully added appointment", Toast.LENGTH_SHORT).show();
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