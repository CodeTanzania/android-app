package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.andraskindler.parallaxviewpager.ParallaxViewPager;
import com.github.codetanzania.adapter.SliderItemViewPagerAdapter;
import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.model.Service;
import com.github.codetanzania.ui.IssueCategoryPickerDialog;
import com.github.codetanzania.util.Open311ServicesUtil;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import tz.co.codetanzania.R;

/**
 * This shows a fragment with a report leakage, report lack of water, and get all category buttons.
 */

public class NewIssueButtonsFragment extends Fragment {
    private LinearLayout mReportNoWaterButton;
    private LinearLayout mReportLeakageButton;

    private Open311Service mNoWaterService = null;
    private Open311Service mLeakageService = null;

    /* the spinner dialog to let users select issue category */
    private IssueCategoryPickerDialog mIssueCategoryPickerDialog;

    /* callback to select issue category from the spinner dialog */
    private IssueCategoryPickerDialog.OnSelectIssueCategory mOnSelectIssueCategory;

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            mOnSelectIssueCategory = (IssueCategoryPickerDialog.OnSelectIssueCategory) ctx;
        } catch (ClassCastException cce) {
            throw new ClassCastException(String.format(
                    "%s must implement %s",
                    IssueCategoryPickerDialog.OnSelectIssueCategory.class.getName()
            ));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO This is a hack that will only work if service names stay the same
        final List<Open311Service> services = Open311ServicesUtil.cached(getContext());
        for (Open311Service service : services) {
            if ("Lack of Water".equals(service.name)) {
                mNoWaterService = service;
            }
            else if ("Leakage".equals(service.name)) {
                mLeakageService = service;
            }
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup viewGroup,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_issue_buttons, viewGroup, false);
    }

    @Override
    public void onViewCreated(View fragView, Bundle savedInstanceState) {
        // link to viewing all the items
        Button btnViewAllCategories = (Button) fragView.findViewById(R.id.btn_OpenOpen311ServiceList);
        btnViewAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIssueCategoryPickerDialog == null) {
                    initializeIssueCategoryPickerDialog();
                }
                mIssueCategoryPickerDialog.show();
            }
        });
        mReportNoWaterButton = (LinearLayout) fragView.findViewById(R.id.report_lack_of_water);
        mReportNoWaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSelectIssueCategory.onIssueCategorySelected(mNoWaterService);
            }
        });
        mReportLeakageButton = (LinearLayout) fragView.findViewById(R.id.report_leakage);
        mReportLeakageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSelectIssueCategory.onIssueCategorySelected(mLeakageService);
            }
        });

        /* setup events */
        bindEvents(btnViewAllCategories);
    }

    private void bindEvents(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIssueCategoryPickerDialog == null) {
                    initializeIssueCategoryPickerDialog();
                }
                mIssueCategoryPickerDialog.show();
            }
        });
    }

    private void initializeIssueCategoryPickerDialog() {
        mIssueCategoryPickerDialog = new IssueCategoryPickerDialog(
                (ArrayList<Open311Service>) Open311ServicesUtil.cached(getActivity()), mOnSelectIssueCategory);
    }
}
