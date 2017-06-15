package com.github.codetanzania.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.codetanzania.Constants;
import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.service.FetchAddressIntentService;
import com.github.codetanzania.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import tz.co.codetanzania.R;

public class OpenIssueTicketFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "OITF";

    public static final int REQUEST_FINE_LOCATION_PERMISSION = 1;

    // the Google client APIs
    private GoogleApiClient mGoogleApiClient;

    // flag used to resolve async conflicts
    private boolean mAddressRequested = false;

    // reference to the views
    private TextView mTextViewCurrentLocation;
    private Button mBtnGetLocation;
    private ProgressDialog pDialog;
    private Location mLastLocation;
    private AddressResultReceiver mAddressReceiver;

    public interface OnSelectAddress {
        void selectAddress(Bundle args);
    }

    public interface OnPostIssue {
        /**
         * Opens new issue to DAWASCO.
         * @param issueMap a map without location. The implementation must
         *                 add location before posting
         */
        void doPost(Map<String, Object> issueMap);
    }

    private OnSelectAddress onSelectAddress;

    private OnPostIssue onPostIssue;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to google map");

        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        Log.d(TAG, "Connected " + mLastLocation);
        // make sure the location was found by activity before starting fetch-address-service
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(getActivity(), R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                startFetchAddressService();
            }
        }
    }

    public void fetchAddressButtonHandler(View view) {
        // Only start the service to fetch the address if GoogleApiClient is
        // connected.
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startFetchAddressService();
        }
        // If GoogleApiClient isn't connected, process the user's request by
        // setting mAddressRequested to true. Later, when GoogleApiClient connects,
        // launch the service to fetch the address. As far as the user is
        // concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;
        updateUIWidgets(true);
    }

    // update widgets
    private void updateUIWidgets(boolean requesting) {
        mBtnGetLocation.setEnabled(!requesting);
        if (requesting) {
            mBtnGetLocation.setText(R.string.text_fetching_location);
        } else {
            mBtnGetLocation.setText(R.string.text_select_location);
        }
    }

    private void startFetchAddressService() {

        Log.d(TAG, "start fetching");

        mAddressReceiver = new AddressResultReceiver(new Handler());
        Intent fetchAddressIntent = new Intent(getActivity(), FetchAddressIntentService.class);
        fetchAddressIntent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        fetchAddressIntent.putExtra(Constants.RECEIVER, mAddressReceiver);
        getActivity().startService(fetchAddressIntent);

        // show dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.text_fetching_location));
        pDialog.setIndeterminate(true);
        pDialog.show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended.");
        pDialog.dismiss();
        // onSelectAddress.selectAddress(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to googleMapConnect.");
        pDialog.dismiss();
        // onSelectAddress.selectAddress(null);
    }


    public static OpenIssueTicketFragment getNewInstance(Bundle args) {
        OpenIssueTicketFragment frag = new OpenIssueTicketFragment();
        frag.setArguments(args);
        return frag;
    }

    private void googleMapConnect() {
        Log.d(TAG, "Connecting google map");
        // fetch last known location which is also the current location
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);

        if (!(mContext instanceof OnSelectAddress)) {
            throw new IllegalStateException("OnOpenIssueTicketFragment's attached context must also implement OnOpenIssueTicketFragment#OnSelectAddress interface");
        }

        if (!(mContext instanceof OnPostIssue)) {
            throw new IllegalStateException("OnOpenIssueTicketFragment's attached context must also implement OnOpenIssueTicketFragment#OnPostIssue interface");
        }

        onSelectAddress = (OnSelectAddress) mContext;
        onPostIssue = (OnPostIssue) mContext;
    }

    @Override
    public void onStop() {
        // disconnect from location service if possible
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override public void onResume() {
        super.onResume();
        // check if we've got the permission to read user's location
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Accessing location");
            // should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for user response. after the user sees the message,
                // try to request the permission
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setMessage(R.string.text_request_location)
                        .setPositiveButton(R.string.text_confirm_location, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                }, REQUEST_FINE_LOCATION_PERMISSION);
                            }
                        })
                        .setNegativeButton(R.string.text_deny_location, null);
                dialogBuilder.show();
            }
        } else {
            // Gets the best and most recent location currently available,
            // which may be null in rare cases when a location is not available.
            googleMapConnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION_PERMISSION:
                // if request is cancelled, the grantResults array is empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ha ha ha! yup! permission granted!
                    // Gets the best and most recent location currently available,
                    // which may be null in rare cases when a location is not available.
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    googleMapConnect();
                } else {
                    // why dear user! you leave me no choice! My life depends on this!
                    getActivity().finish();
                }
        }
    }

    @Override public void onViewCreated(View fragView, Bundle savedInstanceState) {
        this.mTextViewCurrentLocation = (TextView) fragView.findViewById(R.id.tv_CurrentLocation);
        this.mTextViewCurrentLocation.setText(R.string.text_empty_location);
        this.mBtnGetLocation = (Button) fragView.findViewById(R.id.btn_GetLocation);
        final EditText editTextMsg = (EditText) fragView.findViewById(R.id.et_Msg);
        this.mBtnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAddressButtonHandler(v);
            }
        });
        fragView.findViewById(R.id.btn_OpenIssue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> issueBody = new HashMap<>();

                // reporter
                Reporter reporter = Util.getCurrentReporter(getActivity());
                Map<String, String> reporterData = new HashMap<String, String>();
                reporterData.put(Reporter.NAME, reporter.name);
                reporterData.put(Reporter.PHONE, reporter.phone);
                issueBody.put("reporter", reporterData);

                // issue description
                String issueDescription = editTextMsg.getText().toString();
                // avoid posting issue without descriptions
                if (TextUtils.isEmpty(issueDescription)) {
                    Toast.makeText(getActivity(), R.string.warning_empty_issue_body, Toast.LENGTH_SHORT).show();
                    return;
                }
                // pack description into the issue's body
                issueBody.put("description", issueDescription);

                // method used to post the issue
                issueBody.put("method", "Mobile");

                // call the implementation. Note that, the implementation is liable for
                // pre-filling extra details such as address & location where the issue has occurred,
                // image data or sound data describing the issue
                onPostIssue.doPost(issueBody);
            }
        });
    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_open_issue_ticket, group, false);
    }


    private void displayAddressOutput(Bundle data) {
        String address = data.getString(Constants.RESULT_DATA_KEY);
        String strData = getString(R.string.text_curr_location, address,
                String.format(Locale.getDefault(),"%.2f", mLastLocation.getLatitude()),
                String.format(Locale.getDefault(), "%.2f", mLastLocation.getLongitude()));
        mTextViewCurrentLocation.setText(strData);
        data.putParcelable(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        onSelectAddress.selectAddress(data);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                pDialog.dismiss();
                updateUIWidgets(false);
                displayAddressOutput(resultData);
                Toast.makeText(getActivity(), R.string.address_found, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.text_fetch_location_error, Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
                updateUIWidgets(false);
            }
        }
    }
}
