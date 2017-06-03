package com.github.codetanzania;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.codetanzania.ui.HomeMenu;
import com.github.codetanzania.ui.activity.ReportIssueActivity;

import tz.co.codetanzania.R;

public class CivilianFeedback extends Application {

    private static final String TAG = "CivilianFeedback";

    public static final int CREATE_ISSUE_MENU_ITEM_POS   = 0;
    public static final int BROWSER_ISSUES_MENU_ITEM_POS = 1;
    public static final int NOTIFICATIONS_MENU_ITEM_POS  = 2;
    public static final int SETTINGS_MENU_ITEM_POS       = 3;

    private HomeMenu homeMenu;

    @Override public void onCreate() {
        super.onCreate();
        // initialize home menu
        initHomeMenu();
    }

    public HomeMenu getHomeMenu() {
        return homeMenu;
    }

    /* prepares home menu */
    private void initHomeMenu() {
        final Context ctx = getApplicationContext();
        homeMenu = new HomeMenu();
        HomeMenu.HomeMenuItem createIssueMenuItem = homeMenu.getNewMenuItemBuilder()
            .setId(CREATE_ISSUE_MENU_ITEM_POS)
            .setTitle(ctx.getString(R.string.text_new_issue))
            .setIconResourceId(R.drawable.ic_note_add_black_48dp)
            .create();

        HomeMenu.HomeMenuItem listIssuesMenuItem = homeMenu.getNewMenuItemBuilder()
            .setId(BROWSER_ISSUES_MENU_ITEM_POS)
            .setTitle(ctx.getString(R.string.text_view_issues))
            .setIconResourceId(R.drawable.ic_format_list_bulleted_black_48dp)
            .create();

        /*HomeMenu.HomeMenuItem notificationsMenuItem = homeMenu.getNewMenuItemBuilder()
            .setId(NOTIFICATIONS_MENU_ITEM_POS)
            .setTitle(ctx.getString(R.string.text_notifications))
            .setIconResourceId(R.drawable.ic_notifications_black_48dp)
            .create();*/

//        HomeMenu.HomeMenuItem settingsMenuItem = homeMenu.getNewMenuItemBuilder()
//            .setId(SETTINGS_MENU_ITEM_POS)
//            .setTitle(ctx.getString(R.string.text_settings))
//            .setIconResourceId(R.drawable.ic_settings_black_48dp)
//            .create();

        // now add our items . Order matters
        homeMenu.addMenuItem(createIssueMenuItem);
        homeMenu.addMenuItem(listIssuesMenuItem);
        // homeMenu.addMenuItem(notificationsMenuItem);
        // homeMenu.addMenuItem(settingsMenuItem);
    }
}
