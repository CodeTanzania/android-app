package com.github.codetanzania.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.codetanzania.Constants;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.fragment.ImageAttachmentFragment;
import com.github.codetanzania.ui.fragment.IssueDetailsFormFragment;
import com.github.codetanzania.ui.fragment.LocationSelectorFragment;
import com.github.codetanzania.ui.fragment.ServiceSelectorFragment;
import com.github.codetanzania.util.ImageUtils;
import com.github.codetanzania.util.LookAndFeelUtils;
import com.github.codetanzania.util.Open311ServicesUtil;
import com.github.codetanzania.util.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
        ImageAttachmentFragment.OnRemovePreviewItemClick {

    public static final String TAG_SELECTED_SERVICE = "selected_service";

    private static final String TAG = "ReportIssueActivity";

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_ACCESS_CAMERA = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_BROWSE_MEDIA_STORE = 4;

    private final Map<String, Object> mIssueBody = new HashMap<>();
    private final ArrayList<Object> attachments = new ArrayList<>();

    private Open311Service selectedOpen311Service;

    private Bitmap optionalBitmapAttachment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LookAndFeelUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorAccent));
        }

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LookAndFeelUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorGray));
        }

        setContentView(R.layout.activity_report_issue);

        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.basic_toolbar_layout);
            if(toolbar != null) {
                setSupportActionBar(toolbar);
                ActionBar bar = getSupportActionBar();
                if (bar != null) {
                    bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    bar.setCustomView(R.layout.custom_action_bar);
                    bar.setDisplayHomeAsUpEnabled(true);
                }
                // displayCurrentStep();
            }

            // check if service was passed through the intent
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                Open311Service open311Service = bundle.getParcelable(TAG_SELECTED_SERVICE);
                onOpen311ServiceSelected(open311Service);
            } else {
                loadServices();
            }
        } else {
            // restore state
            mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "SavedFrag");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "SavedFrag", mCurrentFragment);
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

    public void forceRepaintActionBar() {

    }


    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void dispatchBrowseMediaStoreIntent() {
        Intent mediaStoreIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(mediaStoreIntent, REQUEST_BROWSE_MEDIA_STORE);
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

    /* When we receive back result from activity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCurrentFragment instanceof IssueDetailsFormFragment) {

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ((IssueDetailsFormFragment) mCurrentFragment).addPreviewImageFragment(imageBitmap);
                this.optionalBitmapAttachment = imageBitmap;
            }

            if (requestCode == REQUEST_BROWSE_MEDIA_STORE && resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Bitmap bitmap = ImageUtils.browseMediaStore(this, uri);
                if (bitmap != null) {
                    ((IssueDetailsFormFragment) mCurrentFragment).addPreviewImageFragment(bitmap);
                    this.optionalBitmapAttachment = bitmap;
                }
            }
        }
    }

    // when activity result is received back
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int grantResults[]) {
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
                    IssueDetailsFormFragment frag = IssueDetailsFormFragment.getNewInstance(selectedOpen311Service.id);
                    setCurrentFragment(R.id.frl_FragmentOutlet, frag.getClass().getName(), frag);
                }
                break;
        }
    }

    // when service is selected
    @Override
    public void onOpen311ServiceSelected(Open311Service open311Service) {
        // note the service id
        mIssueBody.put("service", open311Service.id);
        this.selectedOpen311Service = open311Service;
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

    private void showOptionalDetails(Open311Service selectedService) {
        Open311Service service = selectedService == null ? this.selectedOpen311Service : selectedService;
        // first, we need to check if we've got permission to access camera and save pictures
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // we can write to external storage
            // commit the fragment
            // Bundle args = new Bundle();
            // ImageCaptureFragment frag = ImageCaptureFragment.getNewInstance(args);
            IssueDetailsFormFragment frag = IssueDetailsFormFragment.getNewInstance(service.name);
            setCurrentFragment(R.id.frl_FragmentOutlet, frag.getClass().getName(), frag);
        } else {
            // request permission
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_CAMERA);
        }
    }

    public void doPost(String text) {

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, R.string.warning_empty_issue_body, Toast.LENGTH_SHORT).show();
            return;
        }

        // put optional attachment (image)
        if (optionalBitmapAttachment != null) {
            String encoded = ImageUtils.encodeToBase64(optionalBitmapAttachment, Bitmap.CompressFormat.JPEG, 100);
            Map<String, Object> imageAttachment = new HashMap<>();
            imageAttachment.put("name", "Issue_" + (new Date()).getTime());
            imageAttachment.put("caption", text);
            imageAttachment.put("mime", "image/jpeg");
            imageAttachment.put("content", encoded);
            attachments.add(imageAttachment);
        }

        if (!attachments.isEmpty()) {
            mIssueBody.put("attachments", attachments);
        }

        // set description
        mIssueBody.put("description", text);

        // set reporter
        Reporter reporter = Util.getCurrentReporter(this);
        if (reporter == null) {
            Toast.makeText(this, R.string.warning_no_reporter, Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, String> reporterData = new HashMap<>();
        reporterData.put(Reporter.NAME, reporter.name);
        reporterData.put(Reporter.PHONE, reporter.phone);
        mIssueBody.put("reporter", reporterData);

        // set address -- todo: do reverse geo-coding to get the address
        mIssueBody.put("address", "Unknown");

        // Prepare the dialog
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.text_opening_ticket));
        dialog.setIndeterminate(true);

        //if (mIssueBody.get("pictureFile") != null) {
            // load picture and post data to the server
        //}

        // do the posting
        new Open311Api.ServiceBuilder(this).build(Open311Api.ServiceRequestEndpoint.class)
                .openTicket("Bearer " + Util.getAuthToken(this),mIssueBody)
                .enqueue(getPostIssueCallback(dialog));

        // show the dialog
        dialog.show();

        // hide IME
        Util.hideSoftInputMethod(this);
    }

    private void displayMessage(final String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your issue has been received by DAWASCO. The ticket ID for the issue is " + code);
        builder.setPositiveButton("View Issue Status", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle extras = new Bundle();
                extras.putString(IssueProgressActivity.KEY_TICKET_ID, code);
                Intent activityIntent = new Intent(ReportIssueActivity.this, IssueProgressActivity.class);
                activityIntent.putExtras(extras);
                startActivity(activityIntent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();
    }

    private Callback<ResponseBody> getPostIssueCallback(final ProgressDialog dialog) {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dialog.dismiss();

                Log.d(TAG, response.message());

                if (response.isSuccessful()) {
                    try {
                        String str = response.body().string();
                        JSONObject jsonObject = new JSONObject(str);
                        String code = jsonObject.getString("code");
                        displayMessage(code);
                    } catch (IOException | JSONException ioException) {
                        Log.e(TAG, "The exception is " + ioException.getMessage());
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
        Map<String, Double[]> location = new HashMap<>();
        location.put("Unknown", new Double[]{lats, longs});
        mIssueBody.put("location", location);
        showOptionalDetails(null);
    }


    @Override
    public void onRemovePreviewItemClicked() {
        if (mCurrentFragment instanceof IssueDetailsFormFragment) {
            ((IssueDetailsFormFragment) mCurrentFragment).removePreviewImageFragment();
            this.optionalBitmapAttachment = null;
        }
    }
}
