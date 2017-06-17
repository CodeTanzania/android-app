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
import com.github.codetanzania.ui.fragment.ServiceRequestsFragment;
import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.Constants;
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
    implements ErrorFragment.OnReloadClickListener, Callback<ResponseBody>, OnItemClickListener<ServiceRequest> {

    /* used by the logcat */
    private static final String TAG = "TicketGroupsActivity";

    /* TODO: Floating Action bar button */
    // private FloatingActionButton mFab;

    /* Frame layout */
    private FrameLayout mFrameLayout;

    /* An error flag */
    private boolean isErrorState = false;

    /*
     * Menu items will be hidden when different fragment
     * than ServiceRequestsFragment is committed
     */
    private MenuItem mSearchMenuItem;
    private MenuItem mSwitchCompat;
    private MenuItem mUserProfileMenuItem;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_tickets_group);
        mFrameLayout = (FrameLayout) findViewById(R.id.frl_TicketsActivity);

        // show previous button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //loadServiceRequests();
    }

    @Override public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // mFab = (FloatingActionButton) findViewById(R.id.fab_ReportIssue);
        // mFab.setAlpha(0.0f);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.issues, menu);
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
    }

    private Bundle packForError(String msg, int icn) {
        Bundle args = new Bundle();
        args.putString(ErrorFragment.ERROR_MSG, msg);
        args.putInt(ErrorFragment.ERROR_ICN, icn);
        return args;
    }

    private void showLoadingFragment(int frameToReplace) {
        // hide controls. no need to show them while data is being loaded
        showMenuItems(false);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        ProgressBarFragment mProgressBarFrag = ProgressBarFragment.getInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(frameToReplace, mProgressBarFrag)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();
    }

    private void displayServiceRequests(List<ServiceRequest> requests) {
        Bundle args = new Bundle();

        EmptyIssuesFragment frag = EmptyIssuesFragment.getNewInstance(null);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();

        if (requests.size() == 0) {
            // show empty issues message
            lp.gravity = Gravity.CENTER;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frl_TicketsActivity, frag)
                    .disallowAddToBackStack()
                    .commitAllowingStateLoss();
        } else {
            args.putParcelableArrayList(
                    ServiceRequestsFragment.SERVICE_REQUESTS, (ArrayList<? extends Parcelable>) requests);
            ServiceRequestsFragment mServiceRequestsFrag = ServiceRequestsFragment.getNewInstance(args);
            lp.gravity = Gravity.TOP;

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frl_TicketsActivity, mServiceRequestsFrag)
                    .disallowAddToBackStack()
                    .commitAllowingStateLoss();

            // show menu items only when we have more than
            showMenuItems(true);
        }

        // show the fab
        // mFab.animate().alpha(1.0f);
    }

    private void displayError() {
        if (isErrorState) {
            return;
        }

        // hide controls. no need to show them here
        showMenuItems(false);

        Bundle args = packForError(
            getString(R.string.msg_server_error), R.drawable.ic_cloud_off_48x48);

        ErrorFragment mErrorFrag = ErrorFragment.getInstance(args);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFrameLayout.getLayoutParams();
        lp.gravity = Gravity.CENTER;

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.frl_TicketsActivity, mErrorFrag)
            .disallowAddToBackStack()
            .commitAllowingStateLoss();

        Toast.makeText(this, R.string.msg_server_error, Toast.LENGTH_LONG).show();

        isErrorState = true;

        // disable the fab
        // .animate().alpha(0.0f);
    }

    @Override
    protected Call<ResponseBody> initializeCall() {
        showLoadingFragment(R.id.frl_TicketsActivity);

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
            displayServiceRequests(ServiceRequestsUtil.fromResponseBody(response));
        }
        else {
            // show error
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
        // reload the activity
        startActivity(new Intent(this, IssueTicketGroupsActivity.class));
        finish();
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
