package com.github.codetanzania.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import tz.co.codetanzania.R;

public class BaseAppFragmentActivity extends AppCompatActivity {

    protected static final String TAG_OPEN311_SERVICES = "openCamera-311-services-tag";
    protected static final String TAG_LOCATION_SERVICE = "location-service";

    protected Fragment mCurrentFragment;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // restore the fragment
            // restoring is just a matter of requesting the fragment from fragment manager
            // mCurrentFragmentTag = savedInstanceState.getString(TAG_NAME);
            // mContainerId = savedInstanceState.getInt(CONTAINER_ID);
            // mCurrentFragment = getSupportFragmentManager()
            //        .getFragment(savedInstanceState, mCurrentFragmentTag);
            // set current fragment
        } else {
            final FragmentManager fragManager = getSupportFragmentManager();
            fragManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if (fragManager.getBackStackEntryCount() == 0) {
                        finish();
                    } else {
                        displayCurrentStep();
                    }
                }
            });
        }
    }

    /*@Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // remember the current fragment's tag
        outState.putString(TAG_NAME, mCurrentFragmentTag);
        // remember the container id
        outState.putInt(CONTAINER_ID, mContainerId);
        // save the fragment instance
        getSupportFragmentManager().putFragment(outState, mCurrentFragmentTag, mCurrentFragment);
    }*/

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
        displayCurrentStep();
    }

    protected void setCurrentFragment(
            int containerId, @NonNull String fragTag, @NonNull Fragment frag) {

        FragmentManager fragManager = getSupportFragmentManager();
        // no fragment? (first time we're adding)
        Fragment oldFrag = fragManager.findFragmentById(containerId);
        if (oldFrag == null) {
            // add the fragment for the first time
            fragManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(containerId, frag, fragTag)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            // replace the existing fragment
            fragManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .hide(oldFrag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(containerId, frag, fragTag)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }

        // store the current fragment
        mCurrentFragment = frag;
    }

    protected void showNetworkError(
            String msg, String btnCancelText, String btnConfirmText, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(msg).setNegativeButton(btnCancelText, null).setPositiveButton(btnConfirmText, clickListener)
                .create().show();
    }

    protected void showNetworkError(DialogInterface.OnClickListener clickListener) {
        showNetworkError(
                getString(R.string.msg_network_error),
                getString(R.string.text_cancel),
                getString(R.string.text_retry),
                clickListener
        );
    }

    private void displayCurrentStep() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        final FragmentManager fragManager = getSupportFragmentManager();
        String currentStep = String.format(Locale.getDefault(),"Step %d of %d...",
                fragManager.getBackStackEntryCount(), 3);
        if (actionBar != null) {
            View view = actionBar.getCustomView();
            if (view != null) {
                ((TextView)view.findViewById(R.id.tv_ActionBarTitle)).setText(currentStep);
            }
        }
    }
}