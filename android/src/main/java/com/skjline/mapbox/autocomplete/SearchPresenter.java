package com.skjline.mapbox.autocomplete;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.skjline.mapbox.service.MapBoxService;

public interface SearchPresenter {
    interface Presenter {
        void setupSearch(MapBoxService service, SearchPresenter.View view);
    }

    interface View {
        void updatePosition(boolean hold, LatLng latLng);
    }
}
