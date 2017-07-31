package com.github.codetanzania.util;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import tz.co.codetanzania.R;

public class LookAndFeelUtils {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, int color) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }

    public static void setupActionBar(Activity activity, boolean displayUpAsHomeEnabled) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.home_toolbar_layout);
        setupActionBar(activity, toolbar, displayUpAsHomeEnabled);
    }

    public static void setupActionBar(Activity activity, Toolbar actionBar, boolean displayUpAsHomeEnabled) {
        ((AppCompatActivity) activity).setSupportActionBar(actionBar);
        // noinspection ConstantConditions
        ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(displayUpAsHomeEnabled);
    }
}
