package com.penguinstech.bookingappointmentsapp;

import java.util.ArrayList;

public class BusinessDay {

    private final String day;
    private final ArrayList<BusinessHours> listOfBusinessHours;
    private boolean isChecked;

    public BusinessDay(String day, ArrayList<BusinessHours> listOfBusinessHours, boolean isChecked) {
        this.day = day;
        this.listOfBusinessHours = listOfBusinessHours;
        this.isChecked = isChecked;
    }

    public String getDay() {
        return day;
    }

    public ArrayList<BusinessHours> getListOfBusinessHours() {
        return listOfBusinessHours;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
