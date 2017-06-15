package com.github.codetanzania.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.github.codetanzania.Constants;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.fragment.IssueDetailsFragment;

import id.arieridwan.lib.PageLoader;
import tz.co.codetanzania.R;

public class IssueProgressActivity extends AppCompatActivity {

    public static final String TAG = "IssueProgressActivity";

    public static final String KEY_TICKET_ID = "ticket_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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


    private void setupActionBar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Layout);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        // noinspection ConstantConditions -- yes, because i know actionbar isn't null.
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);
    }

    private void fetchAndDisplayIssueDetails(String code) {

        // TODO: fetch issue from the backend
        final PageLoader loader = (PageLoader) findViewById(R.id.pageLoader);
        loader.startProgress();
        loader.setOnRetry(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.startProgress();
            }
        });
    }

    private void setupIssueDetails(@NonNull ServiceRequest request) {
        // setup action bar
        setupActionBar(request.code);

        // attach the fragment
        Bundle args = new Bundle();
        args.putParcelable(Constants.Const.TICKET, request);
        IssueDetailsFragment frag = IssueDetailsFragment.getInstance(args);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fr_IssueDetails, frag)
                .commit();
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
