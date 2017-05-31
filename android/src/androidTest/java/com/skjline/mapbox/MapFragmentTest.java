package com.skjline.mapbox;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mapbox.mapboxsdk.maps.MapView;
import com.skjline.mapbox.map.MapFragment;
import com.skjline.mapbox.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MapFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testFragmentContainingMapView() {
        MainActivity activity = activityTestRule.getActivity();
        MapView mapView = MapFragment.getMapView(activity, R.id.mapview_fragment);

        onView(withId(R.id.mapview_fragment)).check(matches(withChild(withId(mapView.getId()))));
    }
}
