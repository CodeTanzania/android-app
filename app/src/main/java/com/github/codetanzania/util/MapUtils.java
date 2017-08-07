package com.github.codetanzania.util;

import android.content.res.Resources;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.text.DecimalFormat;

import tz.co.codetanzania.R;

/**
 * This is used for Map functions that can be used throughout the app.
 */

public class MapUtils {
    public static final LatLngBounds DAR_BOUNDS = new LatLngBounds.Builder()
            .include(new LatLng( -7.2, 38.9813)) // Northeast
            .include(new LatLng( -6.45, 39.65))  // Southwest
            .build();

    private static DecimalFormat coordinateFormat = new DecimalFormat("#.0000");

    public static String formatCoordinateString(Resources resources, LatLng location) {
        if (location == null) {
            return resources.getString(R.string.default_address_display);
        }
        return String.format(resources.getString(R.string.coordinate_display),
                    coordinateFormat.format(location.getLatitude()),
                    coordinateFormat.format(location.getLongitude()));
    }
}
