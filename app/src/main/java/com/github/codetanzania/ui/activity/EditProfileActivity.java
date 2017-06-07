package com.github.codetanzania.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class EditProfileActivity extends AppCompatActivity {

    // reference to the views
    private EditText etAccountNumber;
    private EditText etZipCode;
    private EditText etPhoneNumber;
    private EditText etUserEmail;
    private EditText etUserName;

    private Reporter mReporter;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set content view
        setContentView(R.layout.activity_edit_profile);
        // retrieve current reporter
        bindDataToViews();
        // bind events
        bindEventsToViews();
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

    private void bindDataToViews() {
        mReporter = Util.getCurrentReporter(this);
        // EditText etMeterNumber = (EditText) findViewById(R.id.et_MeterNumber);
        etAccountNumber = (EditText) findViewById(R.id.et_AccountNumber);
        etZipCode = (EditText) findViewById(R.id.et_AreaCode);
        etPhoneNumber = (EditText) findViewById(R.id.et_phoneNumber);
        etUserEmail = (EditText) findViewById(R.id.et_UserEmail);
        etUserName = (EditText) findViewById(R.id.et_userName);

        assert mReporter != null;
        // now ...
        if (!TextUtils.isEmpty(mReporter.account)) {
            etAccountNumber.setText(mReporter.account);
        }

        if (!TextUtils.isEmpty(mReporter.phone)) {
            if (mReporter.phone.startsWith("255")) {
                etZipCode.setText(mReporter.phone.substring(0, 3));
                etPhoneNumber.setText(mReporter.phone.substring(3));
            } else {
                etPhoneNumber.setText(mReporter.phone);
            }
        }

        if (!TextUtils.isEmpty(mReporter.name)) {
            etUserName.setText(mReporter.name);
        }

        if (!TextUtils.isEmpty(mReporter.email)) {
            etUserEmail.setText(mReporter.email);
        }
    }

    // bind event
    public void bindEventsToViews () {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        View fab = findViewById(R.id.fab_EditProfile);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mReporter.account = etAccountNumber.getText().toString();
                mReporter.email   = etUserEmail.getText().toString();
                mReporter.name    = etUserName.getText().toString();

                // phone number is made up of country's dial up code + msisdn
                if (etPhoneNumber.getText() != null) {
                    if (etZipCode.getText() != null) {
                        mReporter.phone = etZipCode.getText().toString().concat(etPhoneNumber.getText().toString());
                    } else {
                        mReporter.phone = etPhoneNumber.getText().toString();
                    }
                }

                Util.storeCurrentReporter(EditProfileActivity.this, mReporter);
                Toast.makeText(EditProfileActivity.this, getString(R.string.text_item_saved), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
