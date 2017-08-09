package com.github.codetanzania.ui;

import android.app.Activity;

import com.github.codetanzania.api.model.Open311Service;

import java.util.ArrayList;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import tz.co.codetanzania.R;

public class IssueCategoryPickerDialog implements OnSpinerItemClick {

    private final ArrayList<Open311Service> mItems;
    private SpinnerDialog mSpinnerDialog;
    private final OnSelectIssueCategory mSelectionCallback;

    public IssueCategoryPickerDialog(
            ArrayList<Open311Service> items, OnSelectIssueCategory selectionCallback) {
        mItems = items;
        mSelectionCallback = selectionCallback;
        initDialogComponents();
    }

    private void initDialogComponents() {

        Activity activity;

        try {
            activity = (Activity) mSelectionCallback;
        } catch (ClassCastException cce) {
            throw new ClassCastException(String.format("%s must be implemented by an Activity class",
                    mSelectionCallback.getClass().getName()));
        }

        if (activity != null) {
            String text = activity.getString(R.string.action_select_issue_category);
            mSpinnerDialog = new SpinnerDialog(activity, getServices(), text);
            mSpinnerDialog.bindOnSpinerListener(this);
        }
    }

    private ArrayList<String> getServices() {
        ArrayList<String> services = new ArrayList<>();
        for (Open311Service service : mItems) {
            services.add(service.name);
        }
        return services;
    }

    public void show() {
        mSpinnerDialog.showSpinerDialog();
    }

    @Override
    public void onClick(String s, int i) {
        mSelectionCallback.onIssueCategorySelected(mItems.get(i));
    }

    public interface OnSelectIssueCategory {
        void onIssueCategorySelected(Open311Service open311Service);
    }
}
