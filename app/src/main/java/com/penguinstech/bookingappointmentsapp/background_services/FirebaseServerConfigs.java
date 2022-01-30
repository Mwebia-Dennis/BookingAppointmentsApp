package com.penguinstech.bookingappointmentsapp.background_services;

public interface FirebaseServerConfigs {
    //get project id from your project firebase console -> settings -> project settings - > general
    //base url: https://fcm.googleapis.com/v1/projects/myproject-id/
    String BASE_SERVER_URL = "https://fcm.googleapis.com/";
    //get server key from your project firebase console -> settings -> project settings - > cloud messaging
    String SERVER_KEY = "AAAAyCAeMOw:APA91bGbL7fkL9REq_KpJKrKFClqJhp8v3Cnvs2Tm1APQ_K3eozUBRYsWDott-Z5wWi6J-vEwiW7ymvpa5Vkv1rQ6kW94uxz3THJAJ73HDF8dhDH238BqbSUpOHHGz4OsPD4KdQbW-e3";
    String CONTENT_TYPE = "application/json";
}
