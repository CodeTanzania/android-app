package com.github.codetanzania.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.github.codetanzania.Constants;
import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.event.Analytics;
import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.IssueCategoryPickerDialog;
import com.github.codetanzania.ui.fragment.ImageAttachmentFragment;
import com.github.codetanzania.ui.fragment.IssueDetailsFormFragment;
import com.github.codetanzania.ui.fragment.SelectLocationFragment;
import com.github.codetanzania.util.ImageUtils;
import com.github.codetanzania.util.LookAndFeelUtils;
import com.github.codetanzania.util.Open311ServicesUtil;
import com.github.codetanzania.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
        SelectLocationFragment.OnSelectLocation,
        IssueCategoryPickerDialog.OnSelectIssueCategory,
        IssueDetailsFormFragment.OnStartPhotoActivityForResult,
        ImageAttachmentFragment.OnRemovePreviewItemClick {

    /* the issue category dialog picker */
    private IssueCategoryPickerDialog mIssueCategoryDialogPicker;

    public static final String TAG_SELECTED_SERVICE = "selected_service";

    private static final String TAG = "ReportIssueActivity";

    /* Optimize view lookup/rendering */
    Toolbar toolbar;

    // key used to set the result flag back to the parent activity
    public static final String SUBMISSION_TICKET = "com.github.codetanzania.SUBMISSION_TICKET";

    // issue id
    private String mSubmissionTicket;

    private static final int REQUEST_ACCESS_CAMERA = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_BROWSE_MEDIA_STORE = 4;

    // TODO: Use Object Instead of Map<String, Object>
    private final Map<String, Object> mIssueBody = new HashMap<>();

    // TODO: Get rid of the array list. No need when we have Uri object
    private final ArrayList<Object> attachments = new ArrayList<>();

    private Open311Service selectedOpen311Service;

    // private Bitmap optionalBitmapAttachment;

    // uri to the photo item
    // TODO: Uncomment the following line to use a more succinct Java 8 Optional<Uri> type wrapper
    private Uri mPhotoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LookAndFeelUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorAccent));
        }

        setContentView(R.layout.activity_report_issue);

        if (savedInstanceState == null) {
            // check if service was passed through the intent
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                Open311Service open311Service = bundle.getParcelable(TAG_SELECTED_SERVICE);
                onServiceTypeSelected(open311Service);
            } else {
                loadServices();
            }
        } else {
            // restore state
            mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, "SavedFrag");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.basic_toolbar_layout);
            setSupportActionBar(toolbar);
            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                bar.setCustomView(R.layout.custom_action_bar);
                bar.setDisplayHomeAsUpEnabled(true);
            }
        }

        displayCurrentStep();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentFragment != null) {
            getSupportFragmentManager().putFragment(outState, "SavedFrag", mCurrentFragment);
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


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the file where the image should go
            File photoFile = null;
            try {
                photoFile = ImageUtils.createImageFile(this);
            } catch (IOException ioException) {
                // TODO: Use warning_io_failure when the branch is merged
                Toast.makeText(this, R.string.warning_io_failure, Toast.LENGTH_SHORT).show();
            }

            // continue only if the photo file was successfully created
            if (photoFile != null) {
                mPhotoUri = FileProvider.getUriForFile(
                        this, "com.github.codetanzania.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchBrowseMediaStoreIntent() {
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
            String authHeader = getSharedPreferences(
               Constants.Const.KEY_SHARED_PREFS, MODE_PRIVATE)
                    .getString(Constants.Const.AUTH_TOKEN, null);
            Call<ResponseBody> call = new Open311Api.ServiceBuilder(this).build(Open311Api.ServicesEndpoint.class)
                    .getAll(authHeader);
            call.enqueue(getOpen311ResponseCallback(dialog));
        } else {
            finish();
        }
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

                    // if we successfully retrieved data, we cache it to improve future loading
                    if (!list.isEmpty()) {
                        Open311ServicesUtil.cache(ReportIssueActivity.this, list);
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
        // TODO refactor so that both are within their appropriate fragment methods
        mCurrentFragment.onActivityResult(requestCode, resultCode, data);

        if (mCurrentFragment instanceof IssueDetailsFormFragment) {

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                ((IssueDetailsFormFragment) mCurrentFragment).addPreviewImageFragment(mPhotoUri);
            }

            if (requestCode == REQUEST_BROWSE_MEDIA_STORE && resultCode == RESULT_OK) {
                mPhotoUri = data.getData();
                if (mPhotoUri != null) {
                    ((IssueDetailsFormFragment) mCurrentFragment).addPreviewImageFragment(mPhotoUri);
                }
            }
        }
    }

    // when activity result is received back
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int grantResults[]) {
        mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // when service is selected
    public void onServiceTypeSelected(Open311Service open311Service) {
        setSelectedServiceType(open311Service);
        // call fetch location to.
        startLocationPickerFragment();
    }

    private void setSelectedServiceType(Open311Service open311Service) {
        // note the service id
        mIssueBody.put("service", open311Service.id);
        this.selectedOpen311Service = open311Service;
    }

    // the function is invoked to fetch the user location.
    private void startLocationPickerFragment() {
        SelectLocationFragment frag = new SelectLocationFragment();
        setCurrentFragment(R.id.frl_FragmentOutlet, TAG_LOCATION_SERVICE, frag);
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
        if (mPhotoUri != null) {
            String encoded = ImageUtils.encodeToBase64String(
                    this, mPhotoUri, Bitmap.CompressFormat.JPEG, ImageUtils.DEFAULT_JPEG_COMPRESSION_QUALITY);
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
        // TODO: Extract this string!
        builder.setMessage("Your issue has been received. The ticket ID for the issue is " + code);
        builder.setPositiveButton("View Issue Status", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle extras = new Bundle();
                extras.putString(IssueProgressActivity.KEY_TICKET_ID, code);
                Intent activityIntent = new Intent(ReportIssueActivity.this, IssueProgressActivity.class);
                activityIntent.putExtras(extras);
                startActivity(activityIntent);
                finishWithResult();
            }
        });
        builder.create().show();
        this.mSubmissionTicket = code;
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

    private void finishWithResult() {
        //TODO: Send details about submission
        Analytics.onIssueSubmitted();

        Intent intent = new Intent();
        // TODO: uncomment the following line to return captured photo
        // intent.setData(mPhotoUri);
        if (mSubmissionTicket == null) {
            setResult(Activity.RESULT_CANCELED);
        } else {
            intent.putExtra(SUBMISSION_TICKET, mSubmissionTicket);
            setResult(Activity.RESULT_OK, intent);
        }

        finish();
    }

    @Override
    public void selectLocation(double lats, double longs, String address) {
        // store current longitude and latitude and then move on to the next step
        // by committing another fragment
        mIssueBody.put("address", address == null ? "Unknown" : address);

        Map<String, Double[]> location = new HashMap<>();
        location.put("coordinates", new Double[]{lats, longs});
        mIssueBody.put("location", location);
        showOptionalDetails(null);
    }


    @Override
    public void onRemovePreviewItemClicked() {
        if (mCurrentFragment instanceof IssueDetailsFormFragment) {
            ((IssueDetailsFormFragment) mCurrentFragment).removePreviewImageFragment();
            this.mPhotoUri = null;
        }
    }

    @Override
    public List<Open311Service> getIssueCategories() {
        return Open311ServicesUtil.cached(this);
    }

    @Override
    public void initializeIssueCategoryPickerDialog() {
        // get cached issues
        if (mIssueCategoryDialogPicker == null) {
            mIssueCategoryDialogPicker = new IssueCategoryPickerDialog(
                    (ArrayList<Open311Service>) getIssueCategories(), this);
        }
        mIssueCategoryDialogPicker.show();
    }

    @Override
    public void onIssueCategorySelected(Open311Service open311Service) {
        setSelectedServiceType(open311Service);
        ((IssueDetailsFormFragment) mCurrentFragment).updateServiceType(open311Service);
    }

    @Override
    public void startCameraActivityForResult() {
        dispatchTakePictureIntent();
    }

    @Override
    public void startPhotoMediaBrowserActivityForResult() {
        dispatchBrowseMediaStoreIntent();
    }
}
