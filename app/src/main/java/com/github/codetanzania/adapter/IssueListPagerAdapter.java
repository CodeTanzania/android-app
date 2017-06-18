package com.github.codetanzania.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.codetanzania.CivilianFeedback;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.fragment.EmptyIssuesFragment;
import com.github.codetanzania.ui.fragment.ServiceRequestsFragment;
import com.github.codetanzania.util.ServiceRequestsUtil;

import java.util.ArrayList;

import tz.co.codetanzania.R;

/**
 * This manages issue lists, creating a tab for all issues, open issues and closed issues.
 * If it is provided an empty list, it will display only an empty fragment.
 */

public class IssueListPagerAdapter extends FragmentPagerAdapter{
    private static int NUM_ITEMS = 3;

    private ArrayList<ServiceRequest> all;
    private ArrayList<ServiceRequest> open;
    private ArrayList<ServiceRequest> closed;

    private boolean isEmpty;

    public IssueListPagerAdapter(FragmentManager fm, ArrayList<ServiceRequest> requests) {
        super(fm);
        if (requests.isEmpty()) {
            isEmpty = true;
            return;
        }

        all = new ArrayList<>(requests);
        open = new ArrayList<>();
        closed = new ArrayList<>();

        ServiceRequestsUtil.sort(requests);
        for (ServiceRequest request : requests) {
            if (request.resolvedAt == null) {
                open.add(request);
            } else {
                closed.add(request);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (isEmpty) {
            return EmptyIssuesFragment.getNewInstance(null);
        }

        switch (position) {
            case 0: return ServiceRequestsFragment.getNewInstance(all);
            case 1: return ServiceRequestsFragment.getNewInstance(open);
            case 2: return ServiceRequestsFragment.getNewInstance(closed);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (isEmpty) {
            return null;
        }
        Context context = CivilianFeedback.getContext();
        switch (position) {
            case 0: return context.getString(R.string.tab_all_issues);
            case 1: return context.getString(R.string.tab_open_issues);
            case 2: return context.getString(R.string.tab_closed_issues);
        }
        return null;
    }

    @Override
    public int getCount() {
        return isEmpty ? 1 : NUM_ITEMS;
    }
}
