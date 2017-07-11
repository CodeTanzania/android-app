package com.github.codetanzania.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.ui.fragment.SliderItemFragment;

import java.util.List;

public class SliderItemViewPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Open311Service> mItems;

    public SliderItemViewPagerAdapter(
            FragmentManager fm, @NonNull List<Open311Service> items) {
        super(fm);
        this.mItems = items;
    }

    @Override
    public Fragment getItem(int position) {
        Open311Service item = mItems.get(position);
        return SliderItemFragment.getNewInstance(item);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }
}
