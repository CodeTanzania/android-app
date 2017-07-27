package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class IDFragment extends Fragment {

    /* Used by The Logcat */
    private static final String TAG = "IDFragment";

    private TextInputLayout tilUserName;
    private TextInputEditText etUserName;
    private TextInputEditText etAreaCode;
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;

    /*
     * Bridges communication between fragment and activity
     */
    private OnCacheReporterInfo onCacheReporterInfo;

    public interface OnCacheReporterInfo {
        void cacheReporterInfo(Reporter reporter);
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

    /* fragment lifecycle callback. create fragment's view */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        return inflater.inflate(R.layout.frag_id, group, false);
    }

    /* fragment lifecycle callback. attach events */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        // view.findViewById(R.id.til_Email).setVisibility(View.GONE);
        tilUserName = (TextInputLayout) view.findViewById(R.id.til_UserName);
        etUserName = (TextInputEditText) view.findViewById(R.id.et_userName);
        etAreaCode = (TextInputEditText) view.findViewById(R.id.et_AreaCode);
        tilPhone = (TextInputLayout) view.findViewById(R.id.til_PhoneNumber);
        etPhone = (TextInputEditText) view.findViewById(R.id.et_phoneNumber);

        // force the "user name" input to request focus
        //etUserName.requestFocus();

        // update error messages after each user input
        etUserName.addTextChangedListener(new RegistrationTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                isValidUsername(s);
            }
        });
        etPhone.addTextChangedListener(new RegistrationTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                isValidPhoneNumber(s);
            }
        });

        // save user info after use of soft input DONE and menu item
        etPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_DONE && verifyAndComplete();
            }
        });
        view.findViewById(R.id.btn_Next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAndComplete();
            }
        });
    }

    public boolean verifyAndComplete() {
        Editable userName = etUserName.getText();
        Editable phoneNumber = etPhone.getText();

        // if valid, save reporter to system and enter app
        if (userInputsAreValid(userName, phoneNumber)) {
            Reporter reporter = new Reporter();
            reporter.name = userName.toString();
            reporter.phone = formatPhoneNumber(phoneNumber.toString());
            Util.hideSoftInputMethod(getActivity());
            onCacheReporterInfo.cacheReporterInfo(reporter);
            return true;
        }
        return false;
    }

    private boolean userInputsAreValid(Editable userName, Editable phone) {
        // update both error messages
        boolean isValidUsername = isValidUsername(userName);
        boolean isValidPhone = isValidPhoneNumber(phone);

        // only return true if both are valid
        return isValidUsername && isValidPhone;
    }

    private boolean isValidUsername(Editable userName) {
        // username is required
        boolean usernameValid = !TextUtils.isEmpty(userName);
        if (usernameValid) {
            tilUserName.setErrorEnabled(false);
        } else {
            tilUserName.setError(getString(R.string.error_username_required));
        }
        return usernameValid;
    }

    private boolean isValidPhoneNumber(Editable phoneNumber) {
        // phone number is required TODO: Improve verification, potentially with OTP
        boolean phoneValid = !TextUtils.isEmpty(phoneNumber);
        if (phoneValid) {
            tilPhone.setErrorEnabled(false);
        } else {
            tilPhone.setError(getString(R.string.error_phone_required));
        }
        return phoneValid;
    }

    private String formatPhoneNumber(String phoneNumber) {
        String areaCode = etAreaCode.getText().toString().trim();
        if (TextUtils.isEmpty(areaCode)) {
            areaCode = getResources().getString(R.string.default_area_code);
        }
        return String.format("%s%s", areaCode, phoneNumber);
    }

    // Convenience class to make ui code more compact and clear
    private abstract class RegistrationTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // do nothing
        }
    }
}
