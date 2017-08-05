package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tz.co.codetanzania.R;

/**
 * This creates a location fragment using Mapbox.
 */

public class LocationFragment extends MapboxBaseFragment {
    private Button mSubmitLocation;
    private Location mLatestLocation;
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
                    mListener.selectLocation(mLatestLocation.getLatitude(), mLatestLocation.getLongitude());
                }
            });
        }
        return rootView;
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
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        mLatestLocation = location;
    }

    @Override
    protected int getFragLayoutId() {
        return R.layout.frag_location;
    }


    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }
}
