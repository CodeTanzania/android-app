package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.codetanzania.Constants;
import com.github.codetanzania.adapter.IssueItemsViewPagerAdapter;
import com.github.codetanzania.adapter.IssueProgressTimelineAdapter;
import com.github.codetanzania.model.Comment;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator;
import tz.co.codetanzania.R;

public class IssueDetailsFragment extends Fragment {

    // reference to the recycler view. used to show map and image captured when
    // user submitted an issue
    // private RecyclerView mAttachmentsRecyclerView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_issue_details, group, false);
    }

    public static IssueDetailsFragment getInstance(Bundle args) {
        IssueDetailsFragment inst = new IssueDetailsFragment();
        inst.setArguments(args);
        return inst;
    }

    @Override
    public void onViewCreated(View fragView, Bundle savedInstanceState) {

        Bundle args = getArguments();
        ServiceRequest serviceRequest = args.getParcelable(Constants.Const.TICKET);

        // TODO: This check is only necessary so as to get rid of the android limitation which
        // limits the size of data to be bundled in the intent to 1MB. We use the shared preference
        // to cache data which we then retrieve later when the activity is created.
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.Const.KEY_SHARED_PREFS,
                Context.MODE_PRIVATE);

        int numFrags = prefs.getString(Constants.BASE_64_ENCODED_IMG_DATA, null) == null ? 1 : 2;

        // view pager
        ViewPager viewPager = (ViewPager) fragView.findViewById(R.id.viewPager);
        IssueItemsViewPagerAdapter viewPagerAdapter = new IssueItemsViewPagerAdapter(
                getActivity(), getChildFragmentManager(), serviceRequest, numFrags);
        viewPager.setAdapter(viewPagerAdapter);
        CircleIndicator indicator = (CircleIndicator) fragView.findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);

        // bind description data
        TextView tvIssueDate = (TextView) fragView.findViewById(R.id.tv_IssueDate);
        tvIssueDate.setText(Util.formatDate(serviceRequest.createdAt, "yyyy-MM-dd HH:mm:ss"));

        // tvReportTimestamp.setText(timestamp);
        TextView tvIssueCategoryContent = (TextView) fragView.findViewById(R.id.tv_IssueCategoryContent);
        tvIssueCategoryContent.setText(serviceRequest.service.name);
        TextView tvIssueContent = (TextView) fragView.findViewById(R.id.tv_IssueContent);
        tvIssueContent.setText(serviceRequest.description);

        List<Comment> comments = new ArrayList<>();
        Comment comment;

        TextView tvIssueStatus = (TextView) fragView.findViewById(R.id.tv_IssueStatus);

        // special case: when issue is resolved
        if (serviceRequest.resolvedAt != null) {
            comment = new Comment();
            comment.commentor = serviceRequest.reporter.name;
            comment.content = getString(R.string.text_close_status);
            comment.timestamp = serviceRequest.resolvedAt;
            comments.add(comment);

            tvIssueStatus.setText(
               String.format(Locale.getDefault(), "Closed after %s",
                   Util.timeElapse(serviceRequest.createdAt, serviceRequest.resolvedAt, getActivity())));
        } else {
            tvIssueStatus.setText(R.string.text_pending_description);
        }

        comment = new Comment();
        comment.commentor = serviceRequest.reporter.name;
        comment.content = getString(R.string.text_received_status);
        comment.timestamp = serviceRequest.createdAt;
        comments.add(comment);
        // comments.add(comment)

        if (serviceRequest.comments != null) {
            comments.addAll(serviceRequest.comments);
        }

        IssueProgressTimelineAdapter issueProgressTimelineAdapter = new IssueProgressTimelineAdapter(comments);

        RecyclerView statusesRecyclerView = (RecyclerView) fragView.findViewById(R.id.rv_IssueProgress);
        statusesRecyclerView.setAdapter(issueProgressTimelineAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        statusesRecyclerView.setLayoutManager(manager);
    }
}
