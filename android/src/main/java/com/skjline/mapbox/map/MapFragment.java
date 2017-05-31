package com.skjline.mapbox.map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.maps.MapView;

/**
 * A simple {@link Fragment} subclass.<P>
 * Contains a single map view child, A caller may refer map view instance with {@link MapFragment#mapViewId}
 */
public class MapFragment extends Fragment {
    private final int mapViewId;

    private MapView mapView;

    public int getMapViewId() {
        return mapViewId;
    }

    public MapFragment() {
        // generate view id requires api > 17
        mapViewId = View.generateViewId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = inflater.getContext();

        FrameLayout layout = new FrameLayout(context);
        layout.addView(mapView = new MapView(context));
        mapView.setId(mapViewId);
        mapView.onCreate(savedInstanceState);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public static MapView getMapView(AppCompatActivity activity, int fragmentId) {
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(fragmentId);
        if (fragment instanceof MapFragment) {
            return (MapView) activity.findViewById(((MapFragment) fragment).getMapViewId());
        }

        throw new UnsupportedOperationException("Activity does not contain a MapFragment");
    }
}
