package com.penguinstech.bookingappointmentsapp;

public class BusinessHours {

    private final String startTime;
    private final String endTime;

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
