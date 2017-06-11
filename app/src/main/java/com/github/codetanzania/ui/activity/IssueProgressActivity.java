package com.github.codetanzania.ui.activity;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.github.codetanzania.model.adapter.ServiceRequests;
import com.github.codetanzania.ui.fragment.GoogleMapFragment;
import com.github.codetanzania.ui.fragment.InternalNoteFragment;
import com.github.codetanzania.ui.fragment.IssueDetailsFragment;
import com.github.codetanzania.model.Comment;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.Constants;

import java.util.ArrayList;

import tz.co.codetanzania.R;

public class IssueProgressActivity extends AppCompatActivity /*implements OnMapReadyCallback*/ {

    public static final String TAG = "IssueProgressActivity";

    public static final String KEY_TICKET_ID = "ticket_id";

    private ServiceRequest mServiceRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_progress);

        ServiceRequest request = getIntent().getExtras().getParcelable(Constants.Const.TICKET);

        if (request != null) {
            setupIssueDetails(request);
        } else {
            // fetch request from the backend
            String code = getIntent().getExtras().getString(KEY_TICKET_ID);
            if (TextUtils.isEmpty(code)) {
                finish();
            } else {
                fetchAndDisplayIssueDetails(code);
            }
        }

    }

    private void fetchAndDisplayIssueDetails(String code) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle(code);

        // TODO: fetch issue from the backend
    }

    private void setupIssueDetails(ServiceRequest request) {
        this.mServiceRequest = request;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle(request.code);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
