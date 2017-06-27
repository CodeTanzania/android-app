package com.github.codetanzania.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import tz.co.codetanzania.R;

public class MapItemFragment extends Fragment {

    public static final String KEY_LONGITUDE = MapItemFragment.class.getSimpleName() + "/longitude";
    public static final String KEY_LATITUDE  = MapItemFragment.class.getSimpleName() + "/latitude";

    private float longitude;
    private float latitude;

    public static MapItemFragment getNewInstance(Bundle args) {
        MapItemFragment frag = new MapItemFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // read coordinates from the restored state or from the arguments
        Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
        longitude = bundle.getFloat(KEY_LONGITUDE);
        latitude  = bundle.getFloat(KEY_LATITUDE);
    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        View fragView = inflater.inflate(R.layout.card_view_static_osm, viewGroup, false);
        prepareOSM((MapView) fragView.findViewById(R.id.map));
        return fragView;
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        outState.putFloat(KEY_LATITUDE, latitude);
        outState.putFloat(KEY_LONGITUDE, longitude);
        super.onSaveInstanceState(outState);
    }

    private void prepareOSM(MapView mapView) {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        // mMapView.setBuiltInZoomControls(true);
        // mMapView.setMultiTouchControls(true);

        // set zoom level and location
        IMapController mapController = mapView.getController();
        mapController.setZoom(21);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mapController.setCenter(startPoint);

        // add marker
        Marker mMarker = new Marker(mapView);
        mMarker.setPosition(startPoint);
        mMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(mMarker);
        mMarker.setIcon(ContextCompat.getDrawable(
                mapView.getContext(), R.drawable.ic_location_on_white_24dp));
        mapView.getOverlays().add(mMarker);
    }
}
