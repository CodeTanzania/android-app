package com.github.codetanzania.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.codetanzania.Constants;
import com.github.codetanzania.adapter.IssueMultimediaAdapter;
import com.github.codetanzania.adapter.IssueProgressTimelineAdapter;
import com.github.codetanzania.model.Comment;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.model.Status;
import com.github.codetanzania.util.Util;

import java.util.ArrayList;
import java.util.List;

import tz.co.codetanzania.R;

public class IssueDetailsFragment extends Fragment {

    // reference to the open311Service request
    private ServiceRequest mServiceRequest;

    // reference to the recycler view. used to show map and image captured when
    // user submitted an issue
    private RecyclerView mAttachmentsRecyclerView;

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

        // update ticket status
        TextView tvTicketStatus = (TextView) fragView.findViewById(R.id.tv_TicketStatus);
        // setup status accordingly
        if (serviceRequest.status.type == Status.OPEN) {
            tvTicketStatus.setText(R.string.text_status_pending);
        } else {
            tvTicketStatus.setText(R.string.text_status_closed);
        }

        // bind data
        //TextView tvTicketId = (TextView) fragView.findViewById(R.id.tv_TicketID);
        // tvTicketId.setText(serviceRequest.address);
        // TextView tvReporter = (TextView) fragView.findViewById(R.id.tv_Reporter);
        // tvReporter.setText(serviceRequest.reporter.name);
        // TextView tvReportTimestamp = (TextView) fragView.findViewById(R.id.tv_ReportTimestamp);
        String timestamp = "Unknown time";

        if (serviceRequest.createdAt != null) {
            timestamp = "  " + Util.formatDate(serviceRequest.updatedAt, "dd MMM HH:mm");
        }

        // tvReportTimestamp.setText(timestamp);
        TextView tvTicketTitle = (TextView) fragView.findViewById(R.id.tv_TicketTitle);
        tvTicketTitle.setText(serviceRequest.service.name);
        TextView tvLocation = (TextView) fragView.findViewById(R.id.tv_Location);
        tvLocation.setText(serviceRequest.jurisdiction);
        TextView tvDescription = (TextView) fragView.findViewById(R.id.tv_Description);
        tvDescription.setText(serviceRequest.description);

        mAttachmentsRecyclerView = (RecyclerView)
                fragView.findViewById(R.id.rv_Attachments);

        List<String> attachments = serviceRequest.attachments == null ?
                new ArrayList<String>() : serviceRequest.attachments;
        TextView tvAttachments = (TextView) fragView.findViewById(R.id.tv_Attachments);
        tvAttachments.setText(R.string.text_issue_attachment);

        // convert longitude and latitude string to doubles
        double longitude = serviceRequest.longitude;
        double latitude  = serviceRequest.latitude;

        // create adapter
        IssueMultimediaAdapter issueMultimediaAdapter =
            new IssueMultimediaAdapter(getActivity(), attachments, new double[]{latitude, longitude});

        // setup recycler view
        mAttachmentsRecyclerView.setAdapter(
                issueMultimediaAdapter);

        // linear layout manager
        LinearLayoutManager layoutManager =
            new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        // setup recycler view
        mAttachmentsRecyclerView.setLayoutManager(layoutManager);

        mAttachmentsRecyclerView.setVisibility(attachments.isEmpty() ? View.GONE : View.VISIBLE);

        // Comments/statuses
        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment();
        comment.commentor = serviceRequest.reporter.name;
        comment.content = getString(R.string.text_received_status);
        comment.timestamp = serviceRequest.updatedAt;
        comments.add(comment);

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
