package com.skjline.mapbox.service;

import android.content.Context;
import android.location.Location;

import io.reactivex.Observable;

public interface LocationUpdater {
    Observable<Location> enableService(Context context);
    void disableService();
}
