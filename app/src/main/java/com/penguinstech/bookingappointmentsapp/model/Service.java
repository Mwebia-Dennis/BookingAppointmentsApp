package com.penguinstech.bookingappointmentsapp.model;

import java.io.Serializable;

public class Service implements Serializable {

    private String firebaseId, serviceName, price, hours, mins;

    public Service(){}//required for serialization in firebase
    public Service(String firebaseId, String serviceName, String price, String hours, String mins) {
        this.firebaseId = firebaseId;
        this.serviceName = serviceName;
        this.price = price;
        this.hours = hours;
        this.mins = mins;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPrice() {
        return price;
    }

    public String getHours() {
        return hours;
    }

    public String getMins() {
        return mins;
    }

    public String getFirebaseId() {
        return firebaseId;
    }
}
