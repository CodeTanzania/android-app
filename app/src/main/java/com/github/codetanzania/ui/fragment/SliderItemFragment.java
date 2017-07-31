package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.codetanzania.api.model.Open311Service;

import tz.co.codetanzania.R;

public class SliderItemFragment extends Fragment {

    private static final String TAG_SERVICE_REQUEST = "service_request";

    /* bridges communication between the activity and the fragment */
    private OnStartReportIssueActivity onStartReportIssueActivity;

    public static SliderItemFragment getNewInstance(@NonNull Open311Service open311Service) {
        Bundle args = new Bundle();
        args.putParcelable(TAG_SERVICE_REQUEST, open311Service);
        SliderItemFragment frag = new SliderItemFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            onStartReportIssueActivity = (OnStartReportIssueActivity) ctx;
        } catch (ClassCastException cce) {
            throw new ClassCastException(String.format("%s must implement %s",
                    ctx.getClass().getName(), OnStartReportIssueActivity.class.getName()));
        }
    }

    @Override public View onCreateView(
        LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_view_home_menu_item_content, group, false);
    }

    @Override public void onViewCreated(View fragView, Bundle savedInstanceState) {
        bindDataToViews(fragView);
    }

    private void bindDataToViews(View fragView) {
        Bundle args = getArguments();
        Open311Service open311Service = args.getParcelable(TAG_SERVICE_REQUEST);
        TextView categoryTextView = (TextView) fragView.findViewById(R.id.tv_ItemCategory);
        TextView titleTextView    = (TextView) fragView.findViewById(R.id.tv_ItemTitle);
        TextView descriptionTextView = (TextView) fragView.findViewById(R.id.tv_ItemDescription);
        Button btnReportIssue = (Button) fragView.findViewById(R.id.btn_ReportIssue);

        categoryTextView.setText(open311Service.name);
        titleTextView.setText(open311Service.name);
        descriptionTextView.setText(open311Service.description);

        /* attach event to the class */
        bindEvent(btnReportIssue, open311Service);
    }

    private void bindEvent(Button btn, final Open311Service service) {
           btn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    onStartReportIssueActivity.startReportIssueActivity(service);
               }
           });
    }

    public interface OnStartReportIssueActivity {
        void startReportIssueActivity(Open311Service service);
    }
}
