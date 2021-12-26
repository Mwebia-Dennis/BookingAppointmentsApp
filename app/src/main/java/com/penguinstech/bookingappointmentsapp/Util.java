package com.penguinstech.bookingappointmentsapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

public class Util {

    public static String owner = "user1";
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

}
