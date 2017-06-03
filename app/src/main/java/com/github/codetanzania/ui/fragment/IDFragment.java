package com.github.codetanzania.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.activity.SplashScreenActivity;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class IDFragment extends Fragment {

    /* Used by The Logcat */
    private static final String TAG = "IDFragment";

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
        // view.findViewById(R.id.ll_UserAccount).setVisibility(View.GONE);
        final EditText etUserName = (EditText) view.findViewById(R.id.et_userName);
        final EditText etPhone = (EditText) view.findViewById(R.id.et_phoneNumber);
        view.findViewById(R.id.btn_Next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable phoneNumber = etPhone.getText();
                Editable userName    = etUserName.getText();
                boolean hasError = false;

                if (TextUtils.isEmpty(userName)) {
                    ((TextInputLayout) view.findViewById(R.id.til_UserName)).setError("User name is required");
                    hasError = true;
                }

                if (TextUtils.isEmpty(phoneNumber)) {
                    ((TextInputLayout) view.findViewById(R.id.til_PhoneNumber)).setError("Phone number is required");
                    hasError = true;
                }

                if (!hasError) {
                    // TODO: start OTP VERIFICATION INSTEAD
                    Reporter reporter = new Reporter();
                    reporter.phone = String.format("%s%s","255",phoneNumber.toString());
                    reporter.name  = userName.toString();
                    Util.storeCurrentReporter(getActivity(), reporter);
                    startActivity(new Intent(getActivity(), SplashScreenActivity.class));
                }
            }
        });
    }
}
