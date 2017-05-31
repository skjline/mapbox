package com.skjline.mapbox;

import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.android.geocoder.AndroidGeocoder;
import com.skjline.mapbox.autocomplete.SearchPresenter.Presenter;
import com.skjline.mapbox.map.MapFragment;
import com.skjline.mapbox.service.LocationAPI;
import com.skjline.mapbox.service.LocationService;
import com.skjline.mapbox.service.LocationUpdater;
import com.skjline.mapbox.service.MapBoxService;
import com.skjline.mapbox.util.AnimationUtils;
import com.skjline.mapbox.util.LocationUtils;
import com.skjline.mapbox.util.LocationUtils.LocationType;
import com.skjline.mapbox.util.Permission;
import com.skjline.mapbox.R;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    @LocationType
    private int type = LocationType.GOOGLE_API;

    // Location service with Google Api Service
    private LocationUpdater api;

    private MapboxMap map;
    private MapBoxService mapboxService;

    private Marker marker;

    private String previousPlace;
    private Location previousLocation;

    /**
     * A flag to temporarily bypass my location update when a poi is selected
     * Needs to be reworked.
     */
    private boolean hold;

    private Disposable disposable;
    private boolean hasPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getString(R.string.access_token);
        MapboxAccountManager.start(getApplicationContext(), token);

        setContentView(R.layout.activity_geocoder_main);

        // gets a map view encapsulated in a map fragment
        MapFragment.getMapView(this, R.id.mapview_fragment)
                .getMapAsync(mapBoxMap -> {
                    map = mapBoxMap;
                    enableLocationService(type);
                });

        mapboxService = new MapBoxService(new AndroidGeocoder(this, Locale.getDefault()), token);

        Presenter search = (Presenter) findViewById(R.id.et_location_entry);
        search.setupSearch(mapboxService, this::zoomToTarget);

        hasPermission = Permission.requestLocationPermission(this);
    }

    @Override
    public void onRequestPermissionsResult(int request, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(request, permissions, results);

        switch (request) {
            case Permission.LOCATION_PERMISSION_REQUEST:
                // do nothing for now
                if (results[0] == PackageManager.PERMISSION_GRANTED) {
                    hasPermission = true;
                    enableLocationService(type);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (api == null) {
            return;
        }

        api.disableService();
        api = null;

        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    private void enableLocationService(@LocationType int type) {
        if (map == null || !hasPermission) {
            // either map isn't ready yet or doesn't have gps permissions yet
            return;
        }

        switch (type) {
            case LocationType.GOOGLE_API:
                api = new LocationAPI();
                break;
            case LocationType.ANDROID_SERVICE:
                api = new LocationService();
                break;
        }

        disposable = api.enableService(getApplicationContext())
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::updateUserLocation)
                .observeOn(Schedulers.io())
                .filter(location -> {
                    if (!location.equals(previousLocation)) {
                        previousLocation = location;
                        return true;
                    }
                    return false;
                })
                .flatMap(location -> mapboxService.getGeocode(location))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(address -> {
                    if (TextUtils.isEmpty(address)) {
                        address = "unknown location";
                    }

                    if (!address.equals(previousPlace)) {
                        previousPlace = address;
                        Toast.makeText(MainActivity.this, address, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public Location updateUserLocation(Location location) {
        if (hold) {
            return location;
        }

        zoomToTarget(false, LocationUtils.fromLocation(location));
        return location;
    }

    private void zoomToTarget(boolean hold, LatLng target) {
        this.hold = hold;

        CameraPosition position = new CameraPosition.Builder()
                .target(target)
                .zoom(16)
                .build();

        // todo: consider a more finite solution than this arbitrarily timed solution
        new Handler().postDelayed(() -> dropMarkerFromTopScreen(target), 3500);

        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 5000);
        if (marker != null) {
            map.removeMarker(marker);
            marker = null;
        }
    }

    private void dropMarkerFromTopScreen(LatLng target) {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

        // initialize marker on the top of the screen
        LatLng init = new LatLng(bounds.getLatNorth(), target.getLongitude());
        marker = map.addMarker(new MarkerOptions().position(init));

        // move marker into exact position
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, "position",
                new AnimationUtils.LatLngEvaluator(),
                marker.getPosition(), target);
        animator.setDuration(500);
        animator.start();
    }
}
