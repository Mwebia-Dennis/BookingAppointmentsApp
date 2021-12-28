package com.penguinstech.bookingappointmentsapp.model;

import java.io.Serializable;

public class BusinessHours implements Serializable {

    private String startTime;
    private String endTime;

    public BusinessHours() {}//needed for firebase

    public BusinessHours(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartTime() {
        return startTime;
    }

}
