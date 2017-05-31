package com.skjline.mapbox.util;

import android.location.Location;
import android.support.annotation.IntDef;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LocationUtils {
    private static double calculateDistance(Location from, Location to) {
        return (from != null) ? to.distanceTo(from) : 0;
    }

    public static LatLng fromLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }


    @IntDef({LocationType.GOOGLE_API, LocationType.ANDROID_SERVICE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LocationType {
        int GOOGLE_API = 1;
        int ANDROID_SERVICE = 2;
    }
}
