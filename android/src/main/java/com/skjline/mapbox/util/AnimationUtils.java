package com.skjline.mapbox.util;

import android.animation.TypeEvaluator;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class AnimationUtils {
    /**
     * Marker positional animation evaluator
     * @see <a href="https://www.mapbox.com/android-sdk/examples/animated-marker/">MapBox Example</a>
     * @see <a href="https://developer.android.com/guide/topics/graphics/prop-animation.html#type-evaluator">Android TypeEvaluator</a>
     */
    public static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }
}

