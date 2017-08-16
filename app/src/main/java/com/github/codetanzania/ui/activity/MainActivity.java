package com.github.codetanzania.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.ui.IssueCategoryPickerDialog;
import com.github.codetanzania.ui.fragment.RecentMediaItemsFragment;
import com.github.codetanzania.ui.fragment.SliderItemFragment;
import com.github.codetanzania.ui.fragment.SliderItemsFragment;
import com.github.codetanzania.util.LookAndFeelUtils;
import com.github.codetanzania.util.Open311ServicesUtil;
import com.github.codetanzania.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import tz.co.codetanzania.R;

public class MainActivity extends RetrofitActivity<ResponseBody>
        implements SliderItemFragment.OnStartReportIssueActivity,
        IssueCategoryPickerDialog.OnSelectIssueCategory {

    private static final String TAG = "MainActivity";

    private static final String TAG_RECENT_MEDIA_ITEMS_FRAG = "recent_media_items_fragment";

    // the request code used to report an issue
    private static final int REQUEST_CODE_REPORT_ISSUE = 1;

    // prevent the fragment from fetching data from the server every time
    // the activity is restored by saving it's state
    private RecentMediaItemsFragment mRecentMediaItemsFragment;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_menu);

        if (savedInstanceState == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LookAndFeelUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorAccent));
            }
            setContentView(R.layout.activity_home_menu);
        } else {
            // restore fragments state
            mRecentMediaItemsFragment = (RecentMediaItemsFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, TAG_RECENT_MEDIA_ITEMS_FRAG);
        }

        setupRecentMediaItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar_layout);
        setSupportActionBar(toolbar);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save the fragment's instance
        getSupportFragmentManager().putFragment(
                outState, TAG_RECENT_MEDIA_ITEMS_FRAG, mRecentMediaItemsFragment);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    protected Call<ResponseBody> initializeCall() {
        List<Open311Service> cached = Open311ServicesUtil.cached(this);
        boolean mayInitCall = cached == null || cached.isEmpty();

        if (mayInitCall) {
            String authToken = Util.getAuthToken(this);
            return (new Open311Api.ServiceBuilder(this)
                    .build(Open311Api.ServicesEndpoint.class).getAll(authToken));
        }

        setupIssuesSlider(cached);

        return null;
    }

    @Override
    protected ResponseBody getData(Response<ResponseBody> response) {
        return response.body();
    }

    @Override
    public void onProcessResponse(ResponseBody body, int status) {
        if (status == 200) {

            List<Open311Service> services = null;

            try {
                String strBody = body.string();
                services = Open311Service.fromJson(strBody);
            } catch (IOException | JSONException exception) {
                exception.printStackTrace();
            }

            if (services != null) {
                Open311ServicesUtil.cache(this, services);
                setupIssuesSlider(services);
            }
        }
    }

    private void setupIssuesSlider(List<Open311Service> services) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SliderItemsFragment sliderFragment = new SliderItemsFragment();
        transaction.replace(R.id.frame_ReportIssue, sliderFragment)
                .disallowAddToBackStack().commit();
    }

    private void setupRecentMediaItems() {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        // boolean isFromBackStack = mRecentMediaItemsFragment != null;
        // if (!isFromBackStack) {
        mRecentMediaItemsFragment = new RecentMediaItemsFragment();
        // }

        transaction.replace(R.id.frame_RecentMediaItemsOutlet, mRecentMediaItemsFragment)
                .disallowAddToBackStack().commitAllowingStateLoss();
    }

    @Override
    public void startReportIssueActivity(Open311Service service) {
        Intent intent = new Intent(this, ReportIssueActivity.class);
        Bundle extras = new Bundle();
        extras.putParcelable(ReportIssueActivity.TAG_SELECTED_SERVICE, service);
        intent.putExtras(extras);
        startActivityForResult(intent, REQUEST_CODE_REPORT_ISSUE);
    }

    @Override public void onActivityResult(int requestCode, int result, Intent data) {
        if (requestCode == REQUEST_CODE_REPORT_ISSUE) {
            if (result == Activity.RESULT_OK) {
                // refresh the activity
                // TODO: extract issue ticket and use it to fetch a single item instead of all items
                if (mRecentMediaItemsFragment == null) {
                    setupRecentMediaItems();
                } else {
                    mRecentMediaItemsFragment.fetchItems();
                }
            }
        }
    }

    @Override
    public void onIssueCategorySelected(Open311Service service) {
        startReportIssueActivity(service);
    }
}