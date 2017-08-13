package com.github.codetanzania.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.fragment.PhotoItemFragment;
import com.github.codetanzania.ui.fragment.StaticMapFragment;

import java.util.ArrayList;
import java.util.List;

import tz.co.codetanzania.R;

public class RecentItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ServiceRequest>  mRecentItems;
    private final Context mContext;
    private final int mIssuesCount;
    private final int mMoreCount;
    private final OnRecentIssueClick mIssueClickListener;
    private final OnMoreItemsClick mMoreClickListener;

    private static final int DEFAULT_MAXIMUM_ITEMS_COUNT = 6;

    private static final int TYPE_ISSUE = 0;
    private static final int TYPE_MORE = 1;

    public RecentItemsAdapter(
            Context ctx, List<ServiceRequest> recentIssues, OnRecentIssueClick recentClick, OnMoreItemsClick clickListener) {
        this(ctx, recentIssues, recentClick, clickListener, DEFAULT_MAXIMUM_ITEMS_COUNT);
    }

    public RecentItemsAdapter(
            Context ctx, List<ServiceRequest> recentItems, OnRecentIssueClick recentIssueClick , OnMoreItemsClick moreClickListener, int numToShow) {
        // sort most recent items in the list
        this.mContext = ctx;
        this.mIssueClickListener = recentIssueClick;
        this.mMoreClickListener = moreClickListener;

        // TODO: Add sorting here??
        if (recentItems.size() < numToShow) {
            mMoreCount = 0;
            mIssuesCount = recentItems.size();
            mRecentItems = new ArrayList<>();
            for (ServiceRequest request : recentItems) {
                mRecentItems.add(request);
            }
        } else {
            mMoreCount = recentItems.size()-numToShow;
            mIssuesCount = numToShow;
            mRecentItems = recentItems.subList(0, numToShow - 1);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView;
        switch (viewType) {
            case TYPE_ISSUE:
                itemView = inflater.inflate(R.layout.cardview_issue_grid_item, parent, false);
                return new IssueThumbnailViewHolder(itemView, mIssueClickListener);
            case TYPE_MORE:
                itemView = inflater.inflate(R.layout.card_view_more_recent_items, parent, false);
                return new MoreLinkViewHolder(itemView, mMoreCount, mMoreClickListener);
            default:
                throw new UnsupportedOperationException("No view type defined. viewType=" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ISSUE) {
            ServiceRequest recentItem = this.mRecentItems.get(position);
            ((IssueThumbnailViewHolder)holder).bind(recentItem);
        } else {
            ((MoreLinkViewHolder)holder).bindEvents();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == DEFAULT_MAXIMUM_ITEMS_COUNT-1 ?
                TYPE_MORE : TYPE_ISSUE;
    }

    @Override
    public int getItemCount() {
        return mIssuesCount;
    }

    static class IssueThumbnailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /* hold reference to the entire item for when the client clicks the item */
        private final CardView cardViewRecentItem;
        private ServiceRequest serviceRequest;

        // relative time text view
        private final TextView tvRelativeTime;
        private final FrameLayout flThumbnailContainer;

        /* click listener */
        private final OnRecentIssueClick onRecentIssueClick;

        /* used to show photo or map fragment in cardview */
        private final FragmentManager mFragmentManager;


        public IssueThumbnailViewHolder(View itemView, OnRecentIssueClick onRecentIssueClick) {
            super(itemView);
            cardViewRecentItem = (CardView) itemView.findViewById(R.id.cardView_RecentItem);
            tvRelativeTime = (TextView) itemView.findViewById(R.id.tv_RelativeTime);
            flThumbnailContainer = (FrameLayout) itemView.findViewById(R.id.fl_ThumbnailContainer);
            this.onRecentIssueClick = onRecentIssueClick;

            mFragmentManager = ((FragmentActivity)flThumbnailContainer.getContext()).getSupportFragmentManager();

        }

        void bind(ServiceRequest request) {
            serviceRequest = request;
            long now = System.currentTimeMillis();

            CharSequence relativeTimeSpanString = DateUtils.getRelativeTimeSpanString(
                  request.createdAt.getTime(), now, DateUtils.DAY_IN_MILLIS);

            Fragment fragment;
            int uniqueId = View.generateViewId();
            flThumbnailContainer.setId(uniqueId);
            if (request.hasPhotoAttachment()) {
                fragment = PhotoItemFragment.getNewInstance(request.getImageUri());
            } else {
                fragment = StaticMapFragment.getNewInstance(request.latitude, request.longitude);
                ((StaticMapFragment) fragment).setClickListener(this);
            }
            mFragmentManager.beginTransaction()
                    .replace(flThumbnailContainer.getId(), fragment)
                    .commit();

            tvRelativeTime.setText(relativeTimeSpanString);
            bindEvents(request);
        }

        private void bindEvents(final ServiceRequest recentItem) {
            cardViewRecentItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecentIssueClick.onRecentClicked(recentItem);
                }
            });
            flThumbnailContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecentIssueClick.onRecentClicked(recentItem);
                }
            });
        }

        @Override
        public void onClick(View v) {
            onRecentIssueClick.onRecentClicked(serviceRequest);
        }
    }

    static class MoreLinkViewHolder extends RecyclerView.ViewHolder {

        private final View mMoreItems;
        private final OnMoreItemsClick mClickListener;

        public MoreLinkViewHolder(View itemView, int num, OnMoreItemsClick clickListener) {
            super(itemView);
            TextView tv = (TextView) itemView.findViewById(R.id.tv_moreMediaItems);
            tv.setText("+"+num);

            mMoreItems = itemView.findViewById(R.id.cardView_MoreMediaItems);
            mClickListener = clickListener;
        }

        public void bindEvents() {
            mMoreItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        mClickListener.onMoreItemsClicked(v);
                    }
                }
            });
        }
    }

    public interface OnRecentIssueClick {
        void onRecentClicked(ServiceRequest item);
    }

    public interface OnMoreItemsClick {
        void onMoreItemsClicked(View view);
    }
}
