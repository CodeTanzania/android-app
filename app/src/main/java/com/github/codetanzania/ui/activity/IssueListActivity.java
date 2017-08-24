package com.github.codetanzania.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.codetanzania.adapter.OnItemClickListener;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.model.adapter.ServiceRequests;
import com.github.codetanzania.ui.IssueCategoryPickerDialog;
import com.github.codetanzania.ui.fragment.EmptyIssuesFragment;
import com.github.codetanzania.ui.fragment.ErrorFragment;
import com.github.codetanzania.ui.fragment.ProgressBarFragment;
import com.github.codetanzania.ui.fragment.ServiceRequestsTabFragment;
import com.github.codetanzania.util.LookAndFeelUtils;
import com.github.codetanzania.util.Open311ServicesUtil;
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
public class IssueListActivity extends RetrofitActivity<ResponseBody>
    implements ErrorFragment.OnReloadClickListener,
        Callback<ResponseBody>,
        OnItemClickListener<ServiceRequest>,IssueCategoryPickerDialog.OnSelectIssueCategory {

    /* used by the logcat */
    private static final String TAG = "TicketGroupsActivity";

    /* qualifying token */
    public static final String KEY_ITEMS_QUALIFIER = "items";

    /* Fab to make a new issue */
    private FloatingActionButton mFab;

    /* An error flag */
    private boolean isErrorState = false;

    /* A menu flag */
    private boolean showMenu = true;

    private boolean startRefresh = false;

    /* IssuePicker Dialog */
    private IssueCategoryPickerDialog pickerDialog;

    /*
     * TODO: Add search and profile.
     * Menu items will be hidden when different fragment
     * than ServiceRequestsListFragment is committed
     */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LookAndFeelUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorAccent));
        }

        setContentView(R.layout.activity_issue_tickets_group);
        mFab = (FloatingActionButton) findViewById(R.id.fab_ReportIssue);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIssueCategoryPickerDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LookAndFeelUtils.setupActionBar(this, R.string.title_issue_list, true);
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
            case R.id.item_refresh:
                System.out.println("Refresh clicked");
                startRefresh = true;
                refreshData(true, false);
                showLoadingFragment();
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

        ProgressBarFragment mProgressBarFrag = ProgressBarFragment.getInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frl_TicketsActivity, mProgressBarFrag)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();
    }

    private void displayError() {
        // hide dialog
        hideProgressDialog();

        if (isErrorState) {
            return;
        }

        isErrorState = true;

        // hide controls. no need to show them for server error
        showMenuItems(false);

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
        // hide dialog
        hideProgressDialog();
        // hide controls. no need to show them while data is being loaded
        showMenuItems(false);

        EmptyIssuesFragment frag = EmptyIssuesFragment.getNewInstance(null);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frl_TicketsActivity, frag)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();
    }

    private void showIssueCategoryPickerDialog() {
        if (pickerDialog == null) {
            ArrayList<Open311Service> list = (ArrayList<Open311Service>)
                    Open311ServicesUtil.cached(this);
            pickerDialog =
                    new IssueCategoryPickerDialog(list, this);
        }
        pickerDialog.show();
    }

    private void showListTabs(ArrayList<ServiceRequest> requests) {
        // hide progress dialog
        hideProgressDialog();
        // show service requests grouped into tabs
        ServiceRequestsTabFragment fragment = ServiceRequestsTabFragment.getNewInstance(requests);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frl_TicketsActivity, fragment)
                .disallowAddToBackStack()
                .commitAllowingStateLoss();

        // show menu items after items are displayed
        showMenuItems(true);
    }

    //TODO Centralize this call in one place, so that it can be used in Home activity and List fragment
    @Nullable
    @Override
    protected Call<ResponseBody> initializeCall() {
        // showLoadingFragment();

        Bundle extras =
                getIntent().getExtras();

        if (extras != null && !startRefresh) {
            ArrayList<ServiceRequest> requests =
                    extras.getParcelableArrayList(KEY_ITEMS_QUALIFIER);
            boolean requestsExists = requests != null;
            if (requestsExists) {
                showListTabs(requests);
                return null;
            }
        }
        // reload has started, clear flag
        startRefresh = false;

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
            ServiceRequestsUtil.sort(requests);

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
        startActivity(getIntent());
        finish();
    }

    @Override
    public void onItemClick(ServiceRequest theItem) {
        // preview the item which was clicked
        Util.startPreviewIssueActivity(this, theItem);
    }

    @Override
    public void onIssueCategorySelected(Open311Service open311Service) {
        Intent intent = new Intent(this, ReportIssueActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable(ReportIssueActivity.TAG_SELECTED_SERVICE, open311Service);
        intent.putExtras(extras);
        startActivityForResult(intent, 1);
        Toast.makeText(this, "KNOWN BUG: Reported issue will not appear in a list of recently reported issues", Toast.LENGTH_SHORT).show();
    }
}
