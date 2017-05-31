package com.skjline.mapbox.service;

import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mapbox.services.android.geocoder.AndroidGeocoder;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v4.DirectionsCriteria;
import com.mapbox.services.mapmatching.v4.MapboxMapMatching;
import com.mapbox.services.mapmatching.v4.models.MapMatchingResponse;
import com.skjline.mapbox.Properties;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapBoxService {
    private static final String tag = MapBoxService.class.getSimpleName();

    private String token;
    private AndroidGeocoder geoCoder;

    public MapBoxService(AndroidGeocoder geoCoder, String token) {
        this.geoCoder = geoCoder;

        this.token = token;
        geoCoder.setAccessToken(token);
    }

    /**
     * Gets nearby street using Mapbox geocode api of the given location coordinates
     *
     * @param location geo position
     * @return street address, unknown position if error occurs
     */
    public Observable<String> getGeocode(final Location location) {
        return Observable
                .fromCallable(() -> getLocation(location))
                .onErrorReturn(value -> {
                    Log.e(tag, "error: " + value.getMessage());
                    return "unknown position";
                });
    }

    /**
     * Invokes MapBox geocode API service
     *
     * @param location geo position
     * @return feature name at the position
     * @throws Exception MapBox Service Exception or IO Exception
     */
    private String getLocation(Location location) throws Exception {
        return geoCoder
                .getFromLocation(location.getLatitude(), location.getLongitude(), 1)
                .get(0).getFeatureName();
    }

    /**
     * Invokes MapBox geocode API services to auto complete point of interest
     *
     * @param part A Partial string to be completed
     * @return A List of related address
     */
    public Single<List<Address>> getAutocomplete(String part) {
        return Single
                .fromCallable(() -> geoCoder.getFromLocationName(part, 10))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Invokes map matching service
     * todo: update to return results of ... type
     *
     * @param raw list of points
     * @throws ServicesException
     */
    public void getMatchedCoordinates(List<Position> raw) throws ServicesException {
        if (raw == null || raw.size() < 1) {
            return;
        }

        MapboxMapMatching matcher = new MapboxMapMatching.Builder()
                .setAccessToken(token)
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .setGpsPrecison(8)
                .setTrace(LineString.fromCoordinates(raw))
                .build();

        matcher.enqueueCall(new Callback<MapMatchingResponse>() {
            @Override
            public void onResponse(Call<MapMatchingResponse> call, Response<MapMatchingResponse> response) {

                if (response.code() != 200 || response.body() == null) {
                    try {
                        Log.e(tag, "unable to complete map-matching" + new String(response.errorBody().bytes()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                JsonObject object = response.body().getFeatures().get(0).getProperties();
                Properties property = new Gson().fromJson(object, Properties.class);

                // todo: capturing parameters
                Log.d(tag, "Parsed Properties " +
                        String.format(Locale.getDefault(), "property: %s %s %s",
                                property.getConfidence(), property.getDistance(), property.getDuration()));
            }

            @Override
            public void onFailure(Call<MapMatchingResponse> call, Throwable throwable) {
                Log.e(tag, "unable to match map coordinates: " + throwable.getMessage());
            }
        });
    }
}

