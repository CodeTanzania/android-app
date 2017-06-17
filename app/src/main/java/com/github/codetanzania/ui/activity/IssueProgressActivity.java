package com.github.codetanzania.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.github.codetanzania.Constants;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.fragment.IssueDetailsFragment;
import com.github.codetanzania.util.ServiceRequestsUtil;
import com.github.codetanzania.util.Util;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.arieridwan.lib.PageLoader;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tz.co.codetanzania.R;

public class IssueProgressActivity extends AppCompatActivity implements Callback<ResponseBody> {

    public static final String TAG = "IssueProgressActivity";

    public static final String KEY_TICKET_ID = "ticket_id";

    // We need to be able to utilize the android lifecycle callback
    // with the async nature of the Call<T> API
    private Call<ResponseBody> mCall;

    // PageLoader is shown when user did not specify code to fetch
    // from the server.
    private PageLoader mPageLoader;

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

    /*
     * Callback to execute when the activity is getting destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // cancel the request if we're still executing
        if (mCall != null && mCall.isExecuted() && !mCall.isCanceled()) {
            mCall.cancel();
            mCall = null;
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

        // Get the token from the preference
        String authToken = Util.getAuthToken(this);
        // create auth header token
        String authHeader = String.format(Locale.getDefault(),
                "Bearer %s", authToken);
        // query param used to select service request from the backend
        Map<String, String> map = new HashMap();
        map.put("code", code);
        JSONObject jsObject = new JSONObject(map);

        Log.d(TAG, "Query string is " + jsObject.toString());

        // fetch issue from the backend
        mCall =
        (new Open311Api.ServiceBuilder(this)).build(Open311Api.ServiceRequestEndpoint.class)
                .getByTicketId(authHeader, jsObject.toString());

        // enqueue the request
        mCall.enqueue(this);

        // show loader
        mPageLoader = (PageLoader) findViewById(R.id.pageLoader);
        mPageLoader.startProgress();

        // when loading fails and page loader seats there looking at us,
        // it is time to re-issue another request upon user interaction
        mPageLoader.setOnRetry(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageLoader.startProgress();
                // enqueue the request again if it was not cancelled intentionally
                if (!mCall.isCanceled()) {
                    mCall.enqueue(IssueProgressActivity.this);
                }
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

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        // hide the loader
        if (response.isSuccessful()) {
            mPageLoader.stopProgress();
            String jsonBody = null;
            try {
                jsonBody = response.body().string();
                if (jsonBody != null) {
                    Log.d(TAG, jsonBody);
                    ServiceRequest mRequest = ServiceRequestsUtil.fromJson(jsonBody).get(0);
                    setupIssueDetails(mRequest);
                }
            } catch (IOException ioException) {
                mPageLoader.setTextError("Error processing data");
                mPageLoader.stopProgressAndFailed();
            }
        } else {
            mPageLoader.setTextError("An error occurred while fetching opened ticket from the server.");
            mPageLoader.stopProgressAndFailed();
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        if (mPageLoader != null) {
            mPageLoader.setTextError(getString(R.string.msg_network_error));
            mPageLoader.stopProgressAndFailed();
        }
    }
}
