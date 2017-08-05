package com.github.codetanzania.ui.fragment;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.codetanzania.api.location.LocationTracker;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.Locale;

/**
 * This should be used as a base fragment for fragments that wish to use
 * a mapbox view.
 */

public abstract class MapboxBaseFragment extends Fragment implements
        OnMapReadyCallback,
        LocationTracker.LocationListener {

    private final String accessToken = "pk.eyJ1Ijoia3J0b25nYSIsImEiOiJjajV2ZzAzcDMwMXhlMnFwNGNvZXBucDFsIn0.BxafRKx6aBYMFC-R8x_xkw";

    protected MapView mMapView;
    protected MapboxMap mMapboxMap;
    protected MarkerOptions mMarker;
    protected LatLng mLatLng;
    private LocationTracker mLocationTracker;

    private boolean mLocationFoundPreviously;

    protected abstract int getFragLayoutId();
    protected abstract int getMapViewId();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), accessToken);
        View rootView = inflater.inflate(getFragLayoutId(), container, false);

        mMapView = (MapView) rootView.findViewById(getMapViewId());
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocationTracker.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLocationTracker.respondToActivityResult(requestCode, resultCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationTracker.respondToPermissions(requestCode, grantResults);
    }

    private void updateCamera() {
        mMapView.setCameraDistance(10);
        CameraPosition position;
        if (mLocationFoundPreviously) {
            position = new CameraPosition.Builder()
                    .target(mLatLng) // Sets the new camera position
                    .build(); // Creates a CameraPosition from the builder
        }
        else {
            mLocationFoundPreviously = true;
            position = new CameraPosition.Builder()
                    .target(mLatLng) // Sets the new camera position
                    .zoom(15) // Sets the zoom
                    //.bearing(180) // Rotate the camera
                    //.tilt(30) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder
        }

        mMapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 7000);
    }

    private void addMarker() {
        mMarker = new MarkerOptions()
                .position(mLatLng)
                .title("Location")
                .snippet("Welcome to you");
        mMapboxMap.addMarker(mMarker);
    }

    private void updateMarker() {
        mMapboxMap.clear();
        mMarker = new MarkerOptions()
                .position(mLatLng)
                .title("Location")
                .snippet("Welcome to you");
        mMapboxMap.addMarker(mMarker);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;

        mLocationTracker = new LocationTracker(getActivity());
        mLocationTracker.start(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateCamera();
        if (mMarker != null) {
            updateMarker();
        }else{
            addMarker();
        }
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(getActivity(), "DAWASCO needs a location to find the problem. GPS is required. Enable location and try again.", Toast.LENGTH_LONG).show();
        getActivity().finish();
    }
}
