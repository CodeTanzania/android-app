package com.github.codetanzania.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.fragment.EditProfileFragment;
import com.github.codetanzania.util.LanguageUtils;
import com.github.codetanzania.util.LookAndFeelUtils;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class SettingsActivity extends AppCompatActivity {

    private static final int EDIT_PROFILE_REQUEST = 0;
    private TextView tvUsername;
    private TextView tvPhoneNumber;
    private TextView tvDefaultLanguage;
    //    private TextView tvEmail;
    //    private TextView tvLocation;
    //    private TextView tvMeterNumber;

    // Current default language
    private String mCurrentLanguage;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_PROFILE_REQUEST) {
            updateUserProfile();
            // handle scenarios where user may use back arrow to navigate one level up
            if (data != null) {
                mayChangeDefaultLanguage(data
                        .getBooleanExtra(EditUserProfileActivity.FLAG_LANGUAGE_CHANGED, false));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvUsername = (TextView) findViewById(R.id.tv_UserName);
        tvPhoneNumber = (TextView) findViewById(R.id.tv_UserPhone);
        tvDefaultLanguage = (TextView) findViewById(R.id.tv_DefaultLanguage);
        //        tvEmail = (TextView) findViewById(R.id.tv_UserEmail);
        //        tvLocation = (TextView) findViewById(R.id.tv_UserLocation);
        //        tvMeterNumber = (TextView) findViewById(R.id.tv_UserMeterNumber);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_EditProfile);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(
                    SettingsActivity.this, EditUserProfileActivity.class), EDIT_PROFILE_REQUEST);
            }
        });

        //        View btnLogout = findViewById(R.id.btn_Logout);
        //        btnLogout.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                confirmExit();
        //            }
        //        });

        // read current default language
        mCurrentLanguage = LanguageUtils.withBaseContext(getBaseContext())
                .getDefaultLanguageName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupToolbar();
        updateUserProfile();
    }

    private void mayChangeDefaultLanguage(boolean languageChanged) {
        // detect if language was changed before updating user profile
        if (languageChanged) {
            startActivity(new Intent(this, SplashScreenActivity.class));
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        LookAndFeelUtils.setupActionBar(this, toolbar, true);
    }

    private void updateUserProfile() {
        Reporter reporter = Util.getCurrentReporter(this);
        assert reporter != null;

        tvUsername.setText(reporter.name);

        tvDefaultLanguage.setText(mCurrentLanguage);

        if (TextUtils.isEmpty(reporter.phone)) {
            tvPhoneNumber.setText(R.string.text_empty_phone);
        } else {
            tvPhoneNumber.setText(reporter.phone);
        }

        //        if (TextUtils.isEmpty(reporter.email)) {
        //            tvEmail.setText(R.string.text_empty_email);
        //        } else {
        //            tvEmail.setText(reporter.email);
        //        }
        //        tvLocation.setText(R.string.text_empty_location);
        //        tvMeterNumber.setText(R.string.text_empty_meter_number);
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.text_confirm_logout)
                .setPositiveButton(R.string.action_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Util.resetPreferences(SettingsActivity.this);
                        startActivity(new Intent(SettingsActivity.this, RegistrationActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_stay, null)
                .show();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
