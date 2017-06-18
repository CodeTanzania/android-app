package com.github.codetanzania.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.codetanzania.adapter.IssueListPagerAdapter;
import com.github.codetanzania.model.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

import tz.co.codetanzania.R;

import static android.content.ContentValues.TAG;
import static com.github.codetanzania.ui.fragment.ServiceRequestsFragment.SERVICE_REQUESTS;

/**
 * This fragment contains a view pager which can be used to switch between "all",
 * "open" and "closed" issues.
 */

public class ServiceRequestsTabFragment extends Fragment {
    private ViewPager mViewPager;
    private ArrayList<ServiceRequest> mServiceRequests;

    /* singleton method */
    public static ServiceRequestsTabFragment getNewInstance(ArrayList<ServiceRequest> requests) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(
                SERVICE_REQUESTS, requests);
        ServiceRequestsTabFragment instance = new ServiceRequestsTabFragment();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_issues_tabs, parent, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.vp_TicketsActivity);
        mServiceRequests = getArguments()
                .getParcelableArrayList(SERVICE_REQUESTS);

        Log.i(TAG, "ServiceRequestFra onViewCreated");
        mViewPager.setAdapter(new IssueListPagerAdapter(
                //getFragmentManager(),
                getChildFragmentManager(),
                mServiceRequests));
    }
}