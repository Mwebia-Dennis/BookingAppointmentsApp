package com.penguinstech.bookingappointmentsapp.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class Util {

    public static String owner = "user1";
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
    public static String bold(String text) {
        return String.format("<b>%s</b>", text);
    }

}
