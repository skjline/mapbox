package com.skjline.mapbox.service;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Google Play Api based location service using {@link com.google.android.gms.location.FusedLocationProviderApi}
 * A better power managed and dynamically optimizing location provider.
 * Requires a device with Google Play Service installed and
 * the application depends on play-services-location
 *
 * <p>
 * dependencies {<br>
 * <tb>compile 'com.google.android.gms:play-services-location:<version>'<br>
 * }<br>
 */
@SuppressWarnings("MissingPermission")
public class LocationAPI implements LocationUpdater {
    private static final String tag = LocationAPI.class.getSimpleName();

    private GoogleApiClient client;
    private LocationRequest request;
    private LocationListener listener;

    private Observable<Location> observable;

    public LocationAPI() {
    }

    @Override
    public void disableService() {
        if (listener == null) {
            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(client, listener);

        client.disconnect();

        client = null;
        request = null;
        listener = null;
    }

    @Override
    public Observable<Location> enableService(Context context) {
        if (observable != null) {
            return observable;
        }

        client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(tag, "Google Api Service connected");

                        // permission suppressed for it's managed on application level
                        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, listener);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(tag, "Google Api Service suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(tag, "Google Api Service failed connection");
                    }
                })
                .build();

        request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        observable = Observable.create(new ObservableOnSubscribe<Location>() {
            @Override
            public void subscribe(ObservableEmitter<Location> emitter) throws Exception {
                listener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.wtf(tag,
                                String.format(Locale.getDefault(),
                                        "Location: %f, %f",
                                        location.getLatitude(), location.getLongitude()));
                        emitter.onNext(location);
                    }
                };
            }
        });

        client.connect();

        return observable;
    }
}
