package com.github.codetanzania.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.codetanzania.adapter.RecentItemsAdapter;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.activity.IssueListActivity;
import com.github.codetanzania.ui.activity.ReportIssueActivity;
import com.github.codetanzania.util.ServiceRequestsUtil;
import com.github.codetanzania.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tz.co.codetanzania.R;

public class RecentMediaItemsFragment extends Fragment implements
        Callback<ResponseBody>,
        RecentItemsAdapter.OnMoreItemsClick,
        RecentItemsAdapter.OnRecentIssueClick {

    private static final String RECENT_ITEMS = "recent_items";
    private static final String SERVICE_REQUESTS = "service_requests";

    // a list of service requests
    private List<ServiceRequest> mServiceRequests;

    // error state
    private static final int ERROR_STATE = 0;
    private static final int IDLE_STATE  = 1;
    private static final int LOADING_ITEMS_STATE = 2;
    private static final int EMPTY_ITEMS_STATE   = 3;

    // keep reference to the views
    private ProgressBar mProgressBar;
    private View        mErrorView;
    private View        mEmptyMediaItemsView;
    private View        mMediaItemsView;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup viewGroup,
            Bundle savedInstanceState ) {

        return inflater.inflate(R.layout.frag_recent_media_items, viewGroup, false);
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean isFromBackStack = savedInstanceState != null;

        if (!isFromBackStack) {
            updateUI(LOADING_ITEMS_STATE);
            fetchItems();
        } else {
            mServiceRequests =
                    savedInstanceState.getParcelableArrayList(SERVICE_REQUESTS);
            if (mServiceRequests != null) {
                updateUI(IDLE_STATE);
                prepareRecentItems2();
            }
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        if (mServiceRequests != null) {
            outState.putParcelableArrayList(SERVICE_REQUESTS,
                    (ArrayList<? extends Parcelable>) mServiceRequests);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // initialize views
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar_MediaItemsLoader);
        mErrorView   = view.findViewById(R.id.layout_NetworkError);
        mEmptyMediaItemsView = view.findViewById(R.id.layout_EmptyMediaAction);
        mMediaItemsView = view.findViewById(R.id.recycler_view_RecentMediaItems);

        final TextView gridTitleView = (TextView) view.findViewById(R.id.tv_MediaTitle);
        gridTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAllIssues();
            }
        });
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.isSuccessful()) {
            // convert response to issues and show 6 of them
            List<ServiceRequest> requests = ServiceRequestsUtil.fromResponseBody(response);
            // sort the items
            if (requests == null || requests.isEmpty()) {
                // display empty message
                updateUI(EMPTY_ITEMS_STATE);
                handleEmptyRecentItemsEvent();
            } else {
                // cache attachment to improve memory consumption
                // TODO put in servicerequestsutil creation of objects
                boolean cacheSuccessful = ServiceRequestsUtil.cacheAttachments(getActivity(), requests);
                if (!cacheSuccessful) {
                    Toast.makeText(
                            getActivity(), R.string.warning_io_failure, Toast.LENGTH_SHORT).show();
                }
                // put the most recent items up in the list
                Collections.sort(requests, ServiceRequestsUtil.NewestFirstComparator);
                prepareRecentItems(requests);
                updateUI(IDLE_STATE);
            }
        } else {
            Toast.makeText(getActivity(), "An error has occurred " +
                    response.code(), Toast.LENGTH_SHORT).show();
            updateUI(ERROR_STATE);
            handleNetworkErrorEvent();
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        updateUI(ERROR_STATE);
        handleNetworkErrorEvent();
    }

    public void fetchItems() {
        String token = Util.getAuthToken(getActivity());
        Reporter reporter = Util.getCurrentReporter(getActivity());
        assert reporter != null;

        // load data from the server
        Open311Api.ServiceBuilder api = new Open311Api.ServiceBuilder(getActivity());
        api.getIssuesByUser(token, reporter.phone, this).enqueue(this);
    }

    private void handleNetworkErrorEvent() {
        mErrorView.findViewById(R.id.btn_ReloadMediaItems)
             .setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     fetchItems();
                     updateUI(LOADING_ITEMS_STATE);
                 }
             });
    }

    private void handleEmptyRecentItemsEvent() {
        mEmptyMediaItemsView.findViewById(R.id.btn_PostFirstItem)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startAddFirstItemActivity();
                    }
                });
    }

    private void startAddFirstItemActivity() {
        startActivity(new Intent(getActivity(), ReportIssueActivity.class));
    }

    private void updateUI(int currentState) {
        // FIXME: efficient implementation should consider something versatile as branching statements or control statements (if...else)
        mProgressBar.setVisibility(currentState == LOADING_ITEMS_STATE ? View.VISIBLE : View.GONE);
        mMediaItemsView.setVisibility(currentState == IDLE_STATE ? View.VISIBLE : View.GONE);
        mEmptyMediaItemsView.setVisibility(currentState == EMPTY_ITEMS_STATE ? View.VISIBLE : View.GONE);
        mErrorView.setVisibility(currentState == ERROR_STATE ? View.VISIBLE : View.GONE);
    }

    private void prepareRecentItems(List<ServiceRequest> requests) {
        mServiceRequests = requests;
        prepareRecentItems2();
    }

    private void prepareRecentItems2() {
        RecentItemsAdapter recentItemsAdapter =
                new RecentItemsAdapter(getActivity(), mServiceRequests, this, this);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);

        ((RecyclerView) mMediaItemsView).setAdapter(recentItemsAdapter);
        ((RecyclerView) mMediaItemsView).setLayoutManager(staggeredGridLayoutManager);
    }

    private void viewAllIssues() {
        Bundle extras = new Bundle();
        extras.putParcelableArrayList(IssueListActivity.KEY_ITEMS_QUALIFIER,
                (ArrayList<? extends Parcelable>) mServiceRequests);
        Intent activityIntent = new Intent(getActivity(), IssueListActivity.class);
        activityIntent.putExtras(extras);
        startActivity(activityIntent);
    }

    @Override
    public void onMoreItemsClicked(View view) {
        viewAllIssues();
    }

    @Override
    public void onRecentClicked(ServiceRequest item) {
        System.out.println("Item clicked");
        Util.startPreviewIssueActivity(getActivity(), item);
    }
}
