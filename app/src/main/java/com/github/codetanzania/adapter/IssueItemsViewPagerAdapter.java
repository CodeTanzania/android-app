package com.github.codetanzania.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.fragment.MapItemFragment;
import com.github.codetanzania.ui.fragment.PhotoItemFragment;

/*
 * IssueItemsViewPagerAdapter is a ViewPagerAdapter that does the
 * transitioning of the IssueItems (Map and Picture) which were
 * submitted when user was reporting an issue.
 */
public class IssueItemsViewPagerAdapter extends FragmentStatePagerAdapter {

    private final ServiceRequest mServiceRequest;
    private final int nPages;

    public IssueItemsViewPagerAdapter(FragmentManager fm, ServiceRequest serviceRequest, int numPages) {
        super(fm);
        mServiceRequest = serviceRequest;
        this.nPages = numPages;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();

        switch (position) {
            case 0:
                args.putString(PhotoItemFragment.KEY_PHOTO_DATA, mServiceRequest.attachments.get(0));
                return PhotoItemFragment.getNewInstance(args);
            case 1:
                args.putFloat(MapItemFragment.KEY_LONGITUDE, mServiceRequest.longitude);
                args.putFloat(MapItemFragment.KEY_LATITUDE,  mServiceRequest.latitude);
                return MapItemFragment.getNewInstance(args);
            default:
                throw new UnsupportedOperationException("No fragment at position: " + position);
        }
    }

    @Override
    public int getCount() {
        return nPages;
    }
}
