package com.github.codetanzania.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.github.codetanzania.Constants;
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
    private final Context mContext;

    public IssueItemsViewPagerAdapter(Context ctx, FragmentManager fm, ServiceRequest serviceRequest, int numPages) {
        super(fm);
        mServiceRequest = serviceRequest;
        this.nPages = numPages;
        this.mContext = ctx;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();

        switch (position) {
            case 0:
                if (nPages == 2) {
                    // first page should be picture.
                    // TODO: we retrieve data from the cache because android has a limitation which
                    // prevents developers from passing data that exceeds 1MB using intents.
                    SharedPreferences prefs = mContext.getSharedPreferences(Constants.Const.KEY_SHARED_PREFS,
                            Context.MODE_PRIVATE);
                    String imgData = prefs.getString(Constants.BASE_64_ENCODED_IMG_DATA, null);
                    args.putString(PhotoItemFragment.KEY_PHOTO_DATA, imgData);
                    return PhotoItemFragment.getNewInstance(args);
                }
                // if no picture, show map.
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
