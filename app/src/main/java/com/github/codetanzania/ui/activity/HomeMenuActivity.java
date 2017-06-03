package com.github.codetanzania.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.github.codetanzania.CivilianFeedback;
import com.github.codetanzania.ui.HomeMenu;

import tz.co.codetanzania.R;

public class HomeMenuActivity extends AppCompatActivity implements HomeMenu.OnClickListener {

    private static final String TAG = "HomeMenuActivity";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Layout);
        setSupportActionBar(toolbar);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_settings:
                startActivity(new Intent(this, CivilianProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClickPerformed(HomeMenu.ClickEvent evt) {
        Log.d(TAG, String.format("Evt: %s", evt));
        if (evt.item.getId() == CivilianFeedback.CREATE_ISSUE_MENU_ITEM_POS) {
            startActivity(new Intent(this, ReportIssueActivity.class));
        } else if (evt.item.getId() == CivilianFeedback.BROWSER_ISSUES_MENU_ITEM_POS) {
            startActivity(new Intent(this, IssueTicketGroupsActivity.class));
        } else if (evt.item.getId() == CivilianFeedback.SETTINGS_MENU_ITEM_POS) {
            startActivity(new Intent(this, CivilianProfileActivity.class));
        }
    }
}