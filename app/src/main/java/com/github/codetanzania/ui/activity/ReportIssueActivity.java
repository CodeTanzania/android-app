package com.github.codetanzania.ui.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.codetanzania.Constants;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.event.OnFragmentInteractionListener;
import com.github.codetanzania.model.Open311Service;
import com.github.codetanzania.ui.fragment.ImageCaptureFragment;
import com.github.codetanzania.ui.fragment.LocationSelectorFragment;
import com.github.codetanzania.ui.fragment.OpenIssueTicketFragment;
import com.github.codetanzania.ui.fragment.ServiceSelectorFragment;
import com.github.codetanzania.util.Open311ServicesUtil;
import com.github.codetanzania.util.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tz.co.codetanzania.R;

public class ReportIssueActivity extends BaseAppFragmentActivity implements
        ServiceSelectorFragment.OnSelectOpen311Service,
        LocationSelectorFragment.OnSelectLocation,
        OpenIssueTicketFragment.OnPostIssue {

    private static final String TAG = "ReportIssueActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0x23;
    private static final int REQUEST_ACCESS_CAMERA = 0x24;

    // location
    private Map<String, Double[]> mLocationMap;
    // Service
    private String mServiceId;
    // store current longitude and latitudes
    private double mIssueLongitude;
    private double mIssueLatitude;
    private String mLocationAddress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_report_issue);
        if (savedInstanceState == null) {

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Layout);
            if(toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.custom_action_bar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            loadServices();
        } else {
            // restore state
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    private void loadServices() {
        // Use cached data whenever necessary
        List<Open311Service> cachedData = Open311ServicesUtil.cached(this);

        // if no data was previously cached, then fetch
        if (cachedData.isEmpty()) {
            // show progress-dialog while we're loading data from the server.
            ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.title_loading_services), getString(R.string.text_loading_services), true);
            String authHeader = getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, MODE_PRIVATE)
                    .getString(Constants.Const.AUTH_TOKEN, null);
            Call<ResponseBody> call = new Open311Api.ServiceBuilder(this).build(Open311Api.ServicesEndpoint.class)
                    .getAll(authHeader);
            call.enqueue(getOpen311ResponseCallback(dialog));
        }

        // otherwise, commit the fragment -- to let user select issue category
        else {
            displayServiceCategories(cachedData);
        }
    }

    private void displayServiceCategories(List<Open311Service> list) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.Const.SERVICE_LIST, (ArrayList<? extends Parcelable>) list);
        Fragment fragment = ServiceSelectorFragment.getNewInstance(bundle);
        setCurrentFragment(R.id.frl_FragmentOutlet, TAG_OPEN311_SERVICES, fragment);
    }

    public Callback<ResponseBody> getOpen311ResponseCallback(final ProgressDialog dialog) {
        return new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // in any case, hide the dialog if it's still shown
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.isSuccessful()) {

                    List<Open311Service> list = new ArrayList<>();

                    try {
                        String servicesJson = response.body().string();
                        list.addAll(Open311Service.fromJson(servicesJson));
                    } catch(IOException | JSONException exception) {
                        Log.e(TAG, exception.getMessage());
                        Toast.makeText(ReportIssueActivity.this, "Error parsing data", Toast.LENGTH_SHORT)
                            .show();
                    }

                    // if we successfully retrieved data, we cache it to improve future loadings
                    if (!list.isEmpty()) {
                        Open311ServicesUtil.cache(ReportIssueActivity.this, list);
                        displayServiceCategories(list);
                    }
                } else {
                    showNetworkError(getString(R.string.text_http_error),
                            getString(R.string.text_cancel),
                            getString(R.string.text_report),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO: start report error routine
                                }
                            });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                // display an error to the user
                showNetworkError(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do it again
                        loadServices();
                    }
                });
            }
        };
    }

    // when activity result is received back
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int grantResults[]) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // change the fragment.
                    // LocationSelectorFragment allows user to select the location
                    LocationSelectorFragment frag = LocationSelectorFragment.getNewInstance(null);
                    setCurrentFragment(R.id.frl_FragmentOutlet, LOCATION_SERVICE, frag);
                }
                break;
            case REQUEST_ACCESS_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImageCaptureFragment frag = ImageCaptureFragment.getNewInstance(null);
                    setCurrentFragment(R.id.frl_FragmentOutlet, frag.getClass().getName(), frag);
                }
                break;
        }
    }

    // when service is selected
    @Override
    public void onOpen311ServiceSelected(Open311Service open311Service) {
        // note the service id
        mServiceId = open311Service.id;
        // call fetch location to.
        fetchLocation();
    }

    // the function is invoked to fetch the user location.
    private void fetchLocation() {
        // first, check to see if we're allowed to fetch location by the user
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // we're allowed to access the location.
            // so just commit the fragment
            LocationSelectorFragment frag = LocationSelectorFragment.getNewInstance(null);
            setCurrentFragment(R.id.frl_FragmentOutlet, TAG_LOCATION_SERVICE, frag);
        } else {
            // we're not allowed to access the location. Request permission from the user
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void showOptionalDetails() {

        // first, we need to check if we've got permission to access camera and save pictures
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // we can write to external storage
            // commit the fragment
            ImageCaptureFragment frag = ImageCaptureFragment.getNewInstance(null);
            setCurrentFragment(R.id.frl_FragmentOutlet, frag.getClass().getName(), frag);
        } else {
            // request permission
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_CAMERA);
        }
    }




    @Override
    public void doPost(Map<String, Object> issueMap) {
        // first thing first, check if user has provided location details
        if (mLocationMap.isEmpty()) {
            Toast.makeText(this, R.string.warning_empty_issue_location, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mServiceId)) {
            Toast.makeText(this, R.string.warning_empty_service_id, Toast.LENGTH_SHORT).show();
            return;
        }

        issueMap.put("location", mLocationMap);

        // pack location address if it's available
        if (!TextUtils.isEmpty(mLocationAddress)) {
            issueMap.put("address", mLocationAddress);
        }

        // pack service id
        issueMap.put("service", mServiceId);

        // Prepare the dialog
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.text_opening_ticket));
        dialog.setIndeterminate(true);


        // do the posting
        new Open311Api.ServiceBuilder(this).build(Open311Api.ServiceRequestEndpoint.class)
                .openTicket("Bearer " + Util.getAuthToken(this),issueMap)
                .enqueue(getPostIssueCallback(dialog));

        // show the dialog
        dialog.show();
    }

    private void displayMessage(String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Received Ticket ID: " + code);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    private Callback<ResponseBody> getPostIssueCallback(final ProgressDialog dialog) {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dialog.dismiss();
                if (response.isSuccessful()) {
                    // TODO: Get issue Ticket and display it to the user
                    try {
                        String str = response.body().string();
                        JSONObject jsonObject = new JSONObject(str);
                        String code = jsonObject.getString("code");
                        displayMessage(code);
                    } catch (IOException | JSONException ioException) {

                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
                // show error message
                Toast.makeText(ReportIssueActivity.this, R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    public void selectLocation(double lats, double longs) {
        // store current longitude and latitude and then move on to the next step
        // by committing another fragment
        this.mIssueLatitude = lats;
        this.mIssueLongitude = longs;
        showOptionalDetails();
    }
}
