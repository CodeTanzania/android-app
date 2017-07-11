package com.github.codetanzania.ui.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.codetanzania.Constants;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.model.ServiceRequest;
import com.github.codetanzania.ui.fragment.IssueDetailsFragment;
import com.github.codetanzania.util.LookAndFeelUtils;
import com.github.codetanzania.util.ServiceRequestsUtil;
import com.github.codetanzania.util.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    // Dialog to show while we're fetching data from the server
    private Dialog mPageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LookAndFeelUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorAccent));
        }
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

    private void setupActionBar(ServiceRequest request) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Layout);
        // toolbar.setBackgroundColor(Color.parseColor(request.status.color));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        // noinspection ConstantConditions -- yes, because i know actionbar isn't null.
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(request.code);
    }

    private void fetchAndDisplayIssueDetails(String code) {

        // Get the token from the preference
        String authToken = Util.getAuthToken(this);
        // create auth header token
        String authHeader = String.format(Locale.getDefault(),
                "Bearer %s", authToken);
        // query param used to select service request from the backend
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        JSONObject jsObject = new JSONObject(map);

        Log.d(TAG, "Query string is " + jsObject.toString());

        // fetch issue from the backend
        mCall =
        (new Open311Api.ServiceBuilder(this)).build(Open311Api.ServiceRequestEndpoint.class)
                .getByTicketId(authHeader, jsObject.toString());

        // enqueue the request
        mCall.enqueue(this);

        showProgressDialog();
    }

    private void setupIssueDetails(@NonNull ServiceRequest request) {
        // setup action bar
        setupActionBar(request);

        // attach the fragment
        Bundle args = new Bundle();
        args.putParcelable(Constants.Const.TICKET, request);
        IssueDetailsFragment frag = IssueDetailsFragment.getInstance(args);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fr_IssueDetails, frag)
                .commit();
    }

    private void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loader_dialog_content_view);
        mPageLoader = builder.create();
        mPageLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPageLoader.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                ProgressBar pBar = (ProgressBar) mPageLoader.findViewById(R.id.pb_Loader);
                pBar.getIndeterminateDrawable().setColorFilter(0xffcc0000, PorterDuff.Mode.MULTIPLY);
            }
        });

        mPageLoader.setCancelable(false);
        mPageLoader.show();
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
        // hide loader
        mPageLoader.dismiss();
        if (response.isSuccessful()) {
            ArrayList<ServiceRequest> requests =
                    ServiceRequestsUtil.fromResponseBody(response);
            if (requests != null) {
                setupIssueDetails(requests.get(0));
                return;
            }
        }
        Toast.makeText(this, "Un expected error occurred while loading data from the server.", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        mPageLoader.dismiss();
        Toast.makeText(
                this, "An error occurred while loading ", Toast.LENGTH_SHORT)
                .show();
        Log.e(TAG, "An original error was: " + t.getMessage());
    }
}
