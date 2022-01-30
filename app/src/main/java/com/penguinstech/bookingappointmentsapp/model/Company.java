package com.penguinstech.bookingappointmentsapp.model;

import java.io.Serializable;
import java.util.ArrayList;

//NOTE: implementing serializable class will enable sharing of this object as a bundle between 2 activities
public class Company implements Serializable {

    private String firebaseId, companyName, ownerName,
            description, logo, photo, address, phone, email,
            website, instagram, facebook, adminMsgToken, timeZoneId;
    ArrayList<BusinessDay> businessDayList;

    public Company() {}

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }


    public ArrayList<BusinessDay> getBusinessDayList() {
        return businessDayList;
    }

    public void setBusinessDayList(ArrayList<BusinessDay> businessDayList) {
        this.businessDayList = businessDayList;
    }

    public String getAdminMsgToken() {
        return adminMsgToken;
    }

    public void setAdminMsgToken(String adminMsgToken) {
        this.adminMsgToken = adminMsgToken;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }
}
