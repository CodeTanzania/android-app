package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.codetanzania.api.location.LocationTracker;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import tz.co.codetanzania.R;

/**
 * This creates a location fragment using Mapbox.
 */

public class LocationFragment extends MapboxBaseFragment implements
        LocationTracker.LocationListener {
    private Button mSubmitLocation;
    private LocationTracker mLocationTracker;
    private OnSelectLocation mListener;

    /**
     * The interface bridges communication between #LocationFragment,
     * and the context where it is attached. The context must implement this
     * interface in order to receive location coordinates whenever necessary
     */
    public interface OnSelectLocation {
        /**
         * Interface's only method. The callback is invoked when the current device's
         * location is approximated.
         */
        void selectLocation(double lats, double longs);
    }

    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            mListener = (OnSelectLocation) ctx;
        } catch (ClassCastException cce) {
            throw new ClassCastException(ctx.toString() + " must implement LocationFragment#OnSelectLocation" +
                    " interface");
        }
    }

    @Override
    protected int getFragLayoutId() {
        return R.layout.frag_location;
    }

    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView != null) {
            // TODO next button should only be enabled after location is received
            mSubmitLocation = (Button) rootView.findViewById(R.id.btn_Next);
            mSubmitLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLatLng != null) {
                        mListener.selectLocation(mLatLng.getLatitude(), mLatLng.getLongitude());
                    }
                }
            });
        }
        return rootView;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        super.onMapReady(mapboxMap);

        mLocationTracker = new LocationTracker(getActivity());
        mLocationTracker.start(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationTracker != null) {
            mLocationTracker.onPause();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mLocationTracker != null) {
            mLocationTracker.respondToActivityResult(requestCode, resultCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (mLocationTracker != null) {
            mLocationTracker.respondToPermissions(requestCode, grantResults);
        }
    }

    @Override
    public String getPermissionAlertTitle() {
        return getString(R.string.location_permission_dialog_title);
    }

    @Override
    public String getPermissionAlertDescription() {
        return getString(R.string.location_permission_dialog_description);
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(getActivity(), R.string.location_permission_denied, Toast.LENGTH_LONG).show();
        getActivity().finish();
    }


    @Override
    public void onLocationChanged(Location location) {
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateCamera();
        if (mMarker != null) {
            updateMarker(mLatLng, R.string.location_marker_title, R.string.location_marker_description);
        }else{
            addMarker(mLatLng, R.string.location_marker_title, R.string.location_marker_description);
        }
    }
}
