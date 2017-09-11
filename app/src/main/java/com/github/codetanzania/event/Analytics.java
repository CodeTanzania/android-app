package com.github.codetanzania.event;

import android.app.Activity;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.github.codetanzania.model.ServiceRequest;

import io.fabric.sdk.android.Fabric;

/**
 * This is used to centralize analytics logic.
 */

public class Analytics {
    public static final String TITLE_ISSUE_SUBMIT = "Issue Submitted";
    public static final String TITLE_SUCCESS = "Submitted successfully";
    public static final String TITLE_ADDRESS = "Address";
    public static final String TITLE_LATITUDE = "Latitude";
    public static final String TITLE_LONGITUDE = "Longitude";
    public static final String TITLE_INCLUDES_PHOTO = "Includes photo";
    public static final String TITLE_TYPE = "Issue Type";

    public static void initialize(Context context) {
        Fabric.with(context, new Crashlytics());
    }

    public static void onIssueSubmitted() {
        Answers.getInstance().logCustom(new CustomEvent(TITLE_ISSUE_SUBMIT));
    }

    public static void onIssueSubmitted(boolean isSuccess, ServiceRequest request) {
        onIssueSubmitted(isSuccess, request.service.name, request.address, request.latitude,
                request.longitude, request.hasPhotoAttachment());
    }

    public static void onIssueSubmitted(boolean isSuccess, String type, String address,
                                        double lat, double lng, boolean hasPhoto) {
        Answers.getInstance().logCustom(new CustomEvent(TITLE_ISSUE_SUBMIT)
                .putCustomAttribute(TITLE_SUCCESS, isSuccess ? "true" : "false")
                .putCustomAttribute(TITLE_TYPE, type)
                .putCustomAttribute(TITLE_ADDRESS, address)
                .putCustomAttribute(TITLE_LATITUDE, lat)
                .putCustomAttribute(TITLE_LONGITUDE, lng)
                .putCustomAttribute(TITLE_INCLUDES_PHOTO, hasPhoto ? "true" : "false"));
    }
}
