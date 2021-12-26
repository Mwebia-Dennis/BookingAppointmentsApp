package com.penguinstech.bookingappointmentsapp;

import java.util.ArrayList;

public class BusinessDay {

    private final String day;
    private final ArrayList<BusinessHours> listOfBusinessHours;

    public BusinessDay(String day, ArrayList<BusinessHours> listOfBusinessHours) {
        this.day = day;
        this.listOfBusinessHours = listOfBusinessHours;
    }

    public String getDay() {
        return day;
    }

    public ArrayList<BusinessHours> getListOfBusinessHours() {
        return listOfBusinessHours;
    }
}
