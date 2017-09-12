package com.github.codetanzania.ui;

import android.app.Activity;

import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.ui.view.OnSpinnerItemClick;
import com.github.codetanzania.ui.view.SpinnerDialog;

import java.util.ArrayList;
import java.util.List;

import tz.co.codetanzania.R;

public class IssueCategoryPickerDialog implements OnSpinnerItemClick {

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
            mSpinnerDialog.bindOnSpinnerListener(this);
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
        mSpinnerDialog.showSpinnerDialog();
    }

    @Override
    public void onClick(String s, int i) {
        mSelectionCallback.onIssueCategorySelected(mItems.get(i));
    }

    public interface OnSelectIssueCategory {
        List<Open311Service> getIssueCategories();
        void initializeIssueCategoryPickerDialog();
        void onIssueCategorySelected(Open311Service open311Service);
    }
}
