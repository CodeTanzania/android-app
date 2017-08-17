package com.github.codetanzania.adapter;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.codetanzania.model.Comment;
import com.github.codetanzania.util.LocalTimeUtils;
import com.github.vipulasri.timelineview.TimelineView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tz.co.codetanzania.R;

/**
 * This is used tho show progress of issues, based on comments attached by server.
 */
public class IssueProgressTimelineAdapter extends RecyclerView.Adapter<IssueProgressTimelineAdapter.TimelineViewHolder> {

    private List<Comment> mComments;

    public IssueProgressTimelineAdapter(List<Comment> comments) {
        mComments = comments;
        // sort comments by timestamp
        Collections.sort(mComments, new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
                return o2.timestamp.compareTo(o1.timestamp);
            }
        });
    }

    @Override
    public TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.issue_timeline_view, parent, false);
        return new TimelineViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(TimelineViewHolder holder, int position) {
        holder.bind(mComments.get(position));
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, mComments.size());
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {

        TimelineView mTimelineView;
        AppCompatTextView mStatusTimestamp;
        AppCompatTextView mStatusContent;

        TimelineViewHolder(View itemView, int viewType) {
            super(itemView);
            mStatusTimestamp = (AppCompatTextView) itemView.findViewById(R.id.tv_StatusTimestamp);
            mStatusContent = (AppCompatTextView) itemView.findViewById(R.id.tv_StatusContent);
            mTimelineView = (TimelineView) itemView.findViewById(R.id.timeline_Marker);
            mTimelineView.initLine(viewType);
        }

        void bind(Comment comment) {
            mStatusContent.setText(comment.content);
            mStatusTimestamp.setText(LocalTimeUtils.formatShortDate(comment.timestamp));
        }
    }
}
