package com.github.codetanzania.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.fragment.StaticMapFragment;
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
                if (nPages == 2) {
                    return PhotoItemFragment.getNewInstance(mServiceRequest.getImageUri());
                }
                // if no picture, show map.
            case 1:
                return StaticMapFragment.getNewInstance(
                        mServiceRequest.latitude, mServiceRequest.longitude);
            default:
                throw new UnsupportedOperationException("No fragment at position: " + position);
        }
    }

    @Override
    public int getCount() {
        return nPages;
    }
}
