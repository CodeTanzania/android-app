package com.github.codetanzania.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.codetanzania.adapter.OnItemClickListener;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.ui.fragment.EmptyIssuesFragment;
import com.github.codetanzania.ui.fragment.ErrorFragment;
import com.github.codetanzania.ui.fragment.ProgressBarFragment;
import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.Constants;
import com.github.codetanzania.ui.fragment.ServiceRequestsTabFragment;
import com.github.codetanzania.util.ServiceRequestsUtil;
import com.github.codetanzania.util.Util;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tz.co.codetanzania.R;


/* tickets activity. load and display tickets from the server */
public class IssueTicketGroupsActivity extends RetrofitActivity<ResponseBody>
    implements ErrorFragment.OnReloadClickListener,
        Callback<ResponseBody>,
        OnItemClickListener<ServiceRequest> {

    /* used by the logcat */
    private static final String TAG = "TicketGroupsActivity";

    /* TODO: Floating Action bar button */
    // private FloatingActionButton mFab;

    /* Frame layout */
    private FrameLayout mFrameLayout;

    /* An error flag */
    private boolean isErrorState = false;

    /* A menu flag */
    private boolean showMenu = false;

    /*
     * TODO: Add search and profile.
     * Menu items will be hidden when different fragment
     * than ServiceRequestsFragment is committed
     */
    // private MenuItem mSearchMenuItem;
    // private MenuItem mUserProfileMenuItem;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_tickets_group);
        mFrameLayout = (FrameLayout) findViewById(R.id.frl_TicketsActivity);
        // mFab = (FloatingActionButton) findViewById(R.id.fab_ReportIssue);

        // show previous button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.issues, menu);

        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(showMenu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* show or hide menu items */
    private void showMenuItems(boolean show) {
        showMenu = show;
        // trigger call to `onCreateOptionsMenu`
        invalidateOptionsMenu();
    }

    private void showLoadingFragment() {
        // hide controls. no need to show them while data is being loaded
        showMenuItems(false);

        // loading fragment should be centered
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();
        lp.gravity = Gravity.CENTER;

        ProgressBarFragment mProgressBarFrag = ProgressBarFragment.getInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frl_TicketsActivity, mProgressBarFrag)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();
    }

    private void displayError() {
        if (isErrorState) {
            return;
        }
        isErrorState = true;

        // hide controls. no need to show them for server error
        showMenuItems(false);

        // error fragment should be centered
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();
        lp.gravity = Gravity.CENTER;

        ErrorFragment mErrorFrag = ErrorFragment.getInstance(
                getString(R.string.msg_server_error), R.drawable.ic_cloud_off_48x48);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frl_TicketsActivity, mErrorFrag)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();

        // show toast to inform user that there was a server error
        Toast.makeText(this, R.string.msg_server_error, Toast.LENGTH_LONG).show();
    }

    private void showEmptyFragment() {
        // hide controls. no need to show them while data is being loaded
        showMenuItems(false);

        // empty fragment should be centered
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();
        lp.gravity = Gravity.CENTER;

        EmptyIssuesFragment frag = EmptyIssuesFragment.getNewInstance(null);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frl_TicketsActivity, frag)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();
    }

    private void showListTabs(ArrayList<ServiceRequest> requests) {
        // list fragment should be oriented at top of screen
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();
        lp.gravity = Gravity.TOP;

        ServiceRequestsTabFragment fragment = ServiceRequestsTabFragment.getNewInstance(requests);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frl_TicketsActivity, fragment)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();

        // show menu items after items are displayed
        showMenuItems(true);
    }

    @Override
    protected Call<ResponseBody> initializeCall() {
        showLoadingFragment();

        // get reporter information
        String token = Util.getAuthToken(this);
        assert token != null;
        Reporter reporter = Util.getCurrentReporter(this);
        assert reporter != null;

        // load data from the server
        Open311Api.ServiceBuilder api = new Open311Api.ServiceBuilder(this);
        return api.getIssuesByUser(token, reporter.phone, this);
    }

    @Override
    protected ResponseBody getData(Response<ResponseBody> response) {
        return response == null ? null : response.body();
    }

    @Override
    public void onResponse(
        Call<ResponseBody> call, Response<ResponseBody> response) {
        super.onResponse(call, response);
        if (response.isSuccessful()) {
            ArrayList<ServiceRequest> requests =
                    ServiceRequestsUtil.fromResponseBody(response);

            if (requests == null || requests.size() == 0) {
                showEmptyFragment();
            } else {
                showListTabs(requests);
            }
        }
        else {
            displayError();

            try {
                Log.e(TAG, response.code() + ". " + response.message());
                Log.e(TAG, response.errorBody().string());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        super.onFailure(call, t);
        displayError();

        // debug
        if ( t != null ) {
            Log.e(TAG, "ERROR: " + t.getMessage());
        }
    }

    @Override
    public void onReloadClicked() {
        initializeCall();
    }

    @Override
    public void onItemClick(ServiceRequest theItem) {
        // preview the item which was clicked
        Intent theIntent = new Intent(this, IssueProgressActivity.class);
        Bundle theBundle = new Bundle();
        theBundle.putParcelable(Constants.Const.TICKET, theItem);
        theIntent.putExtras(theBundle);
        // bundle the intent
        // start the activity
        startActivity(theIntent);
    }
}
