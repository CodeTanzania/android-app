package com.github.codetanzania.adapter;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import tz.co.codetanzania.R;

public class RecentItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<RecentItem>  mRecentItems;
    private final Context mContext;
    private final int nItemsCount;
    private final OnRecentItemClick mRecentItemClickListener;
    private final OnMoreItemsClick mClickListener;

    private static final int DEFAULT_MAXIMUM_ITEMS_COUNT = 6;

    private static final int TYPE_RECENT_ITEM = 0;
    private static final int TYPE_LINK_ITEM   = 1;

    public RecentItemsAdapter(
            Context ctx, List<RecentItem> recentItems, OnRecentItemClick recentClick, OnMoreItemsClick clickListener) {
        this(ctx, recentItems, recentClick, clickListener, DEFAULT_MAXIMUM_ITEMS_COUNT);
    }

    public RecentItemsAdapter(
            Context ctx, List<RecentItem> recentItems, OnRecentItemClick recentItemClick , OnMoreItemsClick clickListener, int itemsCount) {
        // sort most recent items in the list
        this.mContext = ctx;
        this.mRecentItemClickListener = recentItemClick;
        this.mClickListener = clickListener;
        if (recentItems.size() < itemsCount) {
            nItemsCount = recentItems.size();
            this.mRecentItems = recentItems;
        } else {
            nItemsCount = itemsCount;
            this.mRecentItems = recentItems.subList(0, itemsCount - 1);
            // last item will be loaded as a link to load more items
            this.mRecentItems.add(null);
        }

        // sort the most recent items
        // Collections.sort(mRecentItems);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView;
        switch (viewType) {
            case TYPE_LINK_ITEM:
                itemView = inflater.inflate(R.layout.card_view_more_recent_items, parent, false);
                return new MoreItemsComponentViewHolder(itemView, mClickListener);
            case TYPE_RECENT_ITEM:
                itemView = inflater.inflate(R.layout.card_view_recent_item, parent, false);
                return new ItemViewHolder(itemView, mRecentItemClickListener);
            default:
                throw new UnsupportedOperationException("No view type defined. viewType=" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(
            RecyclerView.ViewHolder holder, int position) {
        RecentItem recentItem = this.mRecentItems.get(position);
        boolean mayBindLinkData = recentItem == null;
        if (mayBindLinkData) {
            ((MoreItemsComponentViewHolder)holder).bindEvents();
        } else {
            ((ItemViewHolder)holder).bind(recentItem);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mRecentItems.get(position) == null ?
                TYPE_LINK_ITEM : TYPE_RECENT_ITEM;
    }

    @Override
    public int getItemCount() {
        return nItemsCount;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        /* hold reference to the entire item for when the client clicks the item */
        private final View cardViewRecentItem;

        // relative time text view
        private final TextView tvRelativeTime;

        /* click listener */
        private final OnRecentItemClick onRecentItemClick;


        public ItemViewHolder(View itemView, OnRecentItemClick onRecentItemClick) {
            super(itemView);
            cardViewRecentItem = itemView.findViewById(R.id.cardView_RecentItem);
            tvRelativeTime = (TextView) itemView.findViewById(R.id.tv_RelativeTime);
            this.onRecentItemClick = onRecentItemClick;
        }

        void bind(RecentItem recentItem) {

            long now = System.currentTimeMillis();

            CharSequence relativeTimeSpanString = DateUtils.getRelativeTimeSpanString(
                  recentItem.dateCreated.getTime(), now, DateUtils.DAY_IN_MILLIS);

            tvRelativeTime.setText(relativeTimeSpanString);
            bindEvents(recentItem);
        }

        private void bindEvents(final RecentItem recentItem) {
            cardViewRecentItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecentItemClick.onRecentClicked(recentItem.itemId);
                }
            });
        }
    }

    static class MoreItemsComponentViewHolder extends RecyclerView.ViewHolder {

        private final View mMoreItems;
        private final OnMoreItemsClick mClickListener;

        public MoreItemsComponentViewHolder(View itemView, OnMoreItemsClick clickListener) {
            super(itemView);
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

    public static class RecentItem implements Parcelable, Comparable<RecentItem> {

        private String itemId;
        private String base64EncodedImage;
        private Date dateCreated;
        private String category;

        public RecentItem(String itemId, String base64EncodedImage, Date dateCreated, String category) {
            this.itemId = itemId;
            this.base64EncodedImage = base64EncodedImage;
            this.dateCreated = dateCreated;
            this.category    = category;
        }


        protected RecentItem(Parcel in) {
            itemId = in.readString();
            base64EncodedImage = in.readString();
            dateCreated = new Date(in.readLong());
            category    = in.readString();
        }

        public static final Creator<RecentItem> CREATOR = new Creator<RecentItem>() {
            @Override
            public RecentItem createFromParcel(Parcel in) {
                return new RecentItem(in);
            }

            @Override
            public RecentItem[] newArray(int size) {
                return new RecentItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(itemId);
            dest.writeString(base64EncodedImage);
            dest.writeLong(dateCreated.getTime());
            dest.writeString(category);
        }

        @Override
        public int compareTo(RecentItem o) {
            if (o == null || o.dateCreated == null) return -1;
            return o.dateCreated.compareTo(this.dateCreated);
        }
    }

    public interface OnRecentItemClick {
        void onRecentClicked(String itemId);
    }

    public interface OnMoreItemsClick {
        void onMoreItemsClicked(View view);
    }
}
