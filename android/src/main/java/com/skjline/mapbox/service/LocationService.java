package com.skjline.mapbox.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import io.reactivex.Observable;

@SuppressWarnings("MissingPermission")
public class LocationService implements LocationUpdater {
    private LocationManager locationManager;

    private Observable<Location> observable;
    private PositionUpdateListener listener;

    public LocationService() {
    }

    @Override
    public Observable<Location> enableService(Context context) {
        if (observable == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            initializeLocationService();

            observable = Observable.create(emitter -> listener = emitter::onNext);
        }

        return observable;
    }

    @Override
    public void disableService() {
        locationManager.removeUpdates(locationListener);
        locationListener = null;
    }

    private void initializeLocationService() {
        String locationProvider = locationManager.getBestProvider(createFineCriteria(), true);

        if (!TextUtils.isEmpty(locationProvider) && !locationProvider.equals("passive")) {
            locationManager.requestLocationUpdates(locationProvider, 1000, 0f, locationListener);
        }
    }

    private LocationListener locationListener = new LocationListener() {
        private Location last;

        @Override
        public void onLocationChanged(Location location) {
            if (last == null) {
                last = location;
            }

            if (listener == null) {
                return;
            }

            Log.d(getClass().getSimpleName(), "Updating Location: " + location.toString());
            listener.onPositionChanged(location);
            last = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            reset();
            String locationProvider = locationManager.getBestProvider(createFineCriteria(), true);
            locationManager.requestLocationUpdates(locationProvider, 0, 0f, locationListener);
        }

        @Override
        public void onProviderDisabled(String provider) {
            reset();
            locationManager.removeUpdates(locationListener);
        }

        private void reset() {
            last = null;
        }
    };

    private interface PositionUpdateListener {
        void onPositionChanged(Location location);
    }

    private static Criteria createFineCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        return criteria;
    }
}
