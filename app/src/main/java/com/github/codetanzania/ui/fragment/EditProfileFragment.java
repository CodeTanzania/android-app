package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.SingleItemSelectionDialog;
import com.github.codetanzania.ui.activity.EditProfileActivity;
import com.github.codetanzania.util.LanguageUtils;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class EditProfileFragment extends Fragment implements
        DialogInterface.OnClickListener,
        SingleItemSelectionDialog.OnAcceptSelection {

    /* Used by The Logcat */
    private static final String TAG = "EditProfileFragment";

    /* the flag to track if user has changed language or not */
    private boolean languageChanged;

    private TextInputLayout tilUserName;
    private TextInputEditText etUserName;
    private TextInputEditText etAreaCode;
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;
    private TextInputEditText etUserDefaultLanguage;

    /*
     * Bridges communication between fragment and activity
     */
    private OnReporterSaved mListener;

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Toast.makeText(getActivity(), "The dialog has been clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(String item, int position) {

        Context context = getActivity().getBaseContext();
        LanguageUtils langUtils = LanguageUtils.withBaseContext(context);

        switch (item) {
            case LanguageUtils.SWAHILI_LANG:
                langUtils.setSwahiliAsDefaultLanguage();
                etUserDefaultLanguage.setText(LanguageUtils.SWAHILI_LANG);
                break;
            case LanguageUtils.ENGLISH_LANG:
                langUtils.setEnglishAsDefaultLanguage();
                etUserDefaultLanguage.setText(LanguageUtils.ENGLISH_LANG);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Language " + item + " is not supported");
        }

        // mark language change
        languageChanged = true;
    }

    public interface OnReporterSaved {
        void onReporterSaved(Reporter reporter);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        // cast context
        try {
            mListener = (OnReporterSaved) ctx;
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
        return inflater.inflate(R.layout.frag_edit_profile, group, false);
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
        etUserDefaultLanguage = (TextInputEditText) view.findViewById(R.id.et_DefaultUserLanguage);

        // this fragment is used by two activities. RegistrationActivity and EditProfileActivity
        // show etUserDefaultLanguage if current activity is edit profile
        if (getActivity() instanceof EditProfileActivity) {
            etUserDefaultLanguage.setVisibility(View.VISIBLE);
        }

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

        // Display a dialog to allow user to select default language
        etUserDefaultLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDefaultLanguage();
            }
        });
    }

    private void chooseDefaultLanguage() {
        SingleItemSelectionDialog.Builder dialogBuilder =
                SingleItemSelectionDialog.Builder.withContext(getActivity());
        dialogBuilder.addItems(LanguageUtils.ENGLISH_LANG, LanguageUtils.SWAHILI_LANG)
                .setTitle(R.string.title_select_default_language)
                .setActionSelectText(R.string.action_select)
                .setOnAcceptSelection(this)
                .setOnActionListener(this);
        dialogBuilder.build().open();
    }

    public void showCurrentReporter() {
        Reporter currentReporter = Util.getCurrentReporter(getContext());
        if (currentReporter != null) {
            etUserName.setText(currentReporter.name);
            String zip = currentReporter.phone.substring(0, 3);
            String phone = currentReporter.phone.substring(3);
            etAreaCode.setText(zip);
            etPhone.setText(phone);
        }
        String defaultLanguage = LanguageUtils.withBaseContext(getActivity().getBaseContext())
                .getDefaultLanguageName();
        etUserDefaultLanguage.setText(defaultLanguage);
    }

    public boolean verifyAndComplete() {
        Editable userName = etUserName.getText();
        Editable phoneNumber = etPhone.getText();

        // if valid, save reporter to system and enter app
        if (userInputsAreValid(userName, phoneNumber)) {
            Reporter reporter = new Reporter();
            reporter.name = userName.toString();
            reporter.phone = formatPhoneNumber(phoneNumber.toString());
            Util.storeCurrentReporter(getContext(), reporter);
            Util.hideSoftInputMethod(getActivity());
            mListener.onReporterSaved(reporter);
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
