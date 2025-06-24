package com.penguinstech.bookingappointmentsapp.model;

import java.io.Serializable;
import java.util.ArrayList;

//NOTE: implementing serializable class will enable sharing of this object as a bundle between 2 activities
public class BusinessDay implements Serializable {

    private String day;
    private ArrayList<BusinessHours> listOfBusinessHours;
    private boolean isChecked;

    public BusinessDay(String day, ArrayList<BusinessHours> listOfBusinessHours, boolean isChecked) {
        this.day = day;
        this.listOfBusinessHours = listOfBusinessHours;
        this.isChecked = isChecked;
    }

    public BusinessDay(){}//required because of firebase

    public void setDay(String day) {
        this.day = day;
    }

    public void setListOfBusinessHours(ArrayList<BusinessHours> listOfBusinessHours) {
        this.listOfBusinessHours = listOfBusinessHours;
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
