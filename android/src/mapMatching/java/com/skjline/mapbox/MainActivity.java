package com.skjline.mapbox;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.android.geocoder.AndroidGeocoder;
import com.mapbox.services.commons.models.Position;
import com.skjline.mapbox.map.MapFragment;
import com.skjline.mapbox.service.LocationAPI;
import com.skjline.mapbox.service.LocationService;
import com.skjline.mapbox.service.LocationUpdater;
import com.skjline.mapbox.service.MapBoxService;
import com.skjline.mapbox.util.LocationUtils.LocationType;
import com.skjline.mapbox.util.Permission;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    @LocationType
    private int type = LocationType.ANDROID_SERVICE;

    // Location service with Google Api Service
    private LocationUpdater api;

    private MapboxMap map;
    private MapBoxService mapboxService;

    private Location previousLocation;

    private Disposable disposable;
    private boolean hasPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getString(R.string.access_token);
        MapboxAccountManager.start(getApplicationContext(), token);

        setContentView(R.layout.activity_mapmatching_main);

        // gets a map view encapsulated in a map fragment
        MapFragment.getMapView(this, R.id.mapview_fragment)
                .getMapAsync(mapBoxMap -> {
                    map = mapBoxMap;
                    enableLocationService(type);
                });

        mapboxService = new MapBoxService(new AndroidGeocoder(this, Locale.getDefault()), token);

        Button match = (Button) findViewById(R.id.btn_map_match);
        match.setOnClickListener(view -> matchMapCoordinate());

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

    private void matchMapCoordinate() {
        readJsonFile()
                .onErrorReturnItem("error")
                .map(content -> {
                    // fixme: Probably it is redundant to keep reading a file. However, a use-cases will be
                    // to work with different list of coordinates each time, so leave as is for now.
                    List<Position> raw = new ArrayList<>();

                    if (content.equals("error")) {
                        Log.e("MainActivity", "Error parsing json string file");
                        return raw;
                    }

                    JsonElement element = new Gson().fromJson(content, JsonElement.class);
                    JsonObject object = element.getAsJsonObject();

                    JsonArray features = object.getAsJsonArray("features");
                    JsonObject feature = features.get(0).getAsJsonObject();

                    JsonObject geometry = feature.getAsJsonObject("geometry");
                    JsonArray coords = geometry.getAsJsonArray("coordinates");

                    for (int i = 0; i < coords.size(); i++) {
                        double lat = coords.get(i).getAsJsonArray().get(0).getAsDouble();
                        double lng = coords.get(i).getAsJsonArray().get(1).getAsDouble();

                        raw.add(Position.fromCoordinates(lat, lng));
                    }

                    return raw;
                })
                .filter(points -> points.size() > 0)
                .map(points -> {
                    // todo: draw map-matched routes
                    mapboxService.getMatchedCoordinates(points);
                    return points;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::drawBeforeMapMatching);
    }


    private void drawBeforeMapMatching(List<Position> points) {
        LatLng[] pointsArray = new LatLng[points.size()];

        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = new LatLng(points.get(i).getLatitude(), points.get(i).getLongitude());
        }

        map.addPolyline(new PolylineOptions()
                .add(pointsArray)
                .color(Color.parseColor("#8a8acb"))
                .alpha(0.65f)
                .width(4));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .includes(Arrays.asList(pointsArray))
                .build();

        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50), 2000);
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
                .filter(location -> previousLocation == null || previousLocation != location)
                .subscribe(location -> previousLocation = location);
    }

    private Single<String> readJsonFile() {
        return Single.fromCallable(() -> {
            StringBuilder builder = new StringBuilder("");

            InputStream istream = getAssets().open("trace.geojson");
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream, "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();

            return builder.toString();
        }).subscribeOn(Schedulers.io());
    }
}
