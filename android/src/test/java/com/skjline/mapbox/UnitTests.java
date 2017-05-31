package com.skjline.mapbox;

import android.location.Address;
import android.location.Location;

import com.mapbox.services.android.geocoder.AndroidGeocoder;
import com.skjline.mapbox.service.MapBoxService;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;

import static org.mockito.Mockito.when;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTests {
    @Mock
    AndroidGeocoder mockedAndroidGeoCoder;
    @Mock
    Location dummyLocation;
    @Mock
    Address dummyAddress;

    @Rule
    public MockitoRule rules = MockitoJUnit.rule();

    @Test
    public void geoCoderObservable() throws Exception {
        dummyLocation = Mockito.mock(Location.class);
        dummyAddress = Mockito.mock(Address.class);
        mockedAndroidGeoCoder = Mockito.mock(AndroidGeocoder.class);

        when(dummyLocation.getLatitude()).thenReturn(0d);
        when(dummyLocation.getLongitude()).thenReturn(0d);

        when(dummyAddress.getFeatureName()).thenReturn("MyRoad");

        when(mockedAndroidGeoCoder.getFromLocation(dummyLocation.getLatitude(), dummyLocation.getLongitude(), 1))
                .thenReturn(Collections.singletonList(dummyAddress));

        MapBoxService coder = new MapBoxService(mockedAndroidGeoCoder, "test token");
        coder.getGeocode(dummyLocation).subscribe(
                address -> {
                    Assert.assertEquals(address, "MyRoad");
                }
        );
    }
}
