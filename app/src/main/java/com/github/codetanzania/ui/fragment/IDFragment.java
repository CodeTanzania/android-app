package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class IDFragment extends Fragment implements TextInputEditText.OnEditorActionListener {

    /* Used by The Logcat */
    private static final String TAG = "IDFragment";

    private TextInputLayout tilUserName;
    private TextInputEditText etUserName;
    private TextInputEditText etAreaCode;
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;

    // --- the call back to execute when IMM is used to respond to
    // --- click events instead of the ordinary buttons
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean canSave =
                actionId == EditorInfo.IME_ACTION_GO   ||
                actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_DONE;
        if (canSave) {
            return isValidUserInput();
        }
        return false;
    }

    /*
     * Bridges communication between fragment and activity
     */
    public interface OnCacheReporterInfo {
        void cacheReporterInfo(Reporter reporter);
    }

    private OnCacheReporterInfo onCacheReporterInfo;

    /* fragment lifecycle callback. create fragment's view */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        return inflater.inflate(R.layout.frag_id, group, false);
    }

    /* fragment lifecycle callback. attach events */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        view.findViewById(R.id.til_Email).setVisibility(View.GONE);

        tilUserName = (TextInputLayout) view.findViewById(R.id.til_UserName);
        etUserName = (TextInputEditText) view.findViewById(R.id.et_userName);
        etAreaCode = (TextInputEditText) view.findViewById(R.id.et_AreaCode);
        tilPhone = (TextInputLayout) view.findViewById(R.id.til_PhoneNumber);
        etPhone = (TextInputEditText) view.findViewById(R.id.et_phoneNumber);

        // force the "user name" input to request focus
        etUserName.requestFocus();

        view.findViewById(R.id.btn_Next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isValidUserInput();
            }
        });

        // --- using soft input instead of the buttons to respond to user actions
        etPhone.setOnEditorActionListener(this);
        // --- using soft input instead of the buttons to respond to user actions
        etUserName.setOnEditorActionListener(this);
    }

    public boolean isValidUserInput() {
        Editable phoneNumber = etPhone.getText();
        Editable userName = etUserName.getText();

        // username is required
        boolean usernameValid = !TextUtils.isEmpty(userName);
        if (usernameValid) {
            tilUserName.setErrorEnabled(false);
        } else {
            tilUserName.setError(getString(R.string.error_username_required));
        }

        // phone number is required TODO: Improve verification, potentially with OTP
        boolean phoneValid = !TextUtils.isEmpty(phoneNumber);
        if (phoneValid) {
            tilPhone.setErrorEnabled(false);
        } else {
            tilPhone.setError(getString(R.string.error_phone_required));
        }

        // if valid, save reporter to system and enter app
        if (usernameValid && phoneValid) {
            Reporter reporter = new Reporter();
            reporter.name = userName.toString();
            reporter.phone = formatPhoneNumber(phoneNumber.toString());
            Util.hideSoftInputMethod(getActivity());
            onCacheReporterInfo.cacheReporterInfo(reporter);
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        // cast context
        try {
            onCacheReporterInfo = (OnCacheReporterInfo) ctx;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(String.format("%s must implement %s",
                    getActivity().getClass().getName(),
                    ctx.getClass().getName()));
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        String areaCode = etAreaCode.getText().toString().trim();
        if (TextUtils.isEmpty(areaCode)) {
            areaCode = getResources().getString(R.string.default_area_code);
        }
        return String.format("%s%s", areaCode, phoneNumber);
    }
}
