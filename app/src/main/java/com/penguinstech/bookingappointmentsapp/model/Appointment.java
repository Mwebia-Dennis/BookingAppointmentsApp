package com.penguinstech.bookingappointmentsapp.model;

public class Appointment {

    Client client;
    String firebaseId, date, duration, startTime, totalPrice;
    String appointmentStatus = AppointmentStatus.PENDING;
    String notificationStatus = NotificationStatus.UNREAD;
    String serviceIds;//a list of service id that are joined to form a string separated by a comma

    public Appointment() {}

    public Appointment(Client client, String date, String duration, String startTime, String serviceIds, String totalPrice) {
        this.client = client;
        this.date = date;
        this.duration = duration;
        this.startTime = startTime;
        this.serviceIds = serviceIds;
        this.totalPrice = totalPrice;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(String serviceIds) {
        this.serviceIds = serviceIds;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
}
