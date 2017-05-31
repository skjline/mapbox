package com.skjline.mapbox.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class Permission {
    private static final String tag = Permission.class.getSimpleName();

    public static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String LOCATION_PERMISSIONS = Manifest.permission_group.LOCATION;

    public static boolean requestLocationPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSIONS)
                == PackageManager.PERMISSION_GRANTED) {
            // Location permission is already granted
            Log.wtf(tag, "Permission is granted");
            return true;
        }

        Log.wtf(tag, "Requesting a permission");
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        return false;
    }
}
