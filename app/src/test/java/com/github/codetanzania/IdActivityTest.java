package com.github.codetanzania;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.widget.Button;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.activity.IDActivity;
import com.github.codetanzania.ui.activity.SplashScreenActivity;
import com.github.codetanzania.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowIntent;

import tz.co.codetanzania.BuildConfig;
import tz.co.codetanzania.R;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * This unit test is used to test the initial registration screen
 * shown when a user first opens the app.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows = {})
public class IdActivityTest {
    private IDActivity mActivity;

    private TextInputLayout mUsername;
    private TextInputLayout mEmail;
    private TextInputLayout mAreaCode;
    private TextInputLayout mPhoneNumber;
    private Button mSubmitButton;

    @Before
    public void setup() {
        mActivity = Robolectric.buildActivity(
                IDActivity.class).create().start().resume().get();
        mUsername = (TextInputLayout) mActivity.findViewById(R.id.til_UserName);
        //mEmail = (TextInputLayout) mActivity.findViewById(R.id.til_Email);
        mAreaCode = (TextInputLayout) mActivity.findViewById(R.id.til_AreaCode);
        mPhoneNumber = (TextInputLayout) mActivity.findViewById(R.id.til_PhoneNumber);
        mSubmitButton = (Button) mActivity.findViewById(R.id.btn_Next);
    }

    @Test
    public void idActivity_starts() {
        assertNotNull(mUsername);
        //assertNotNull(mEmail);
        assertNotNull(mAreaCode);
        assertNotNull(mPhoneNumber);
        assertNotNull(mSubmitButton);
    }

    @Test
    public void idActivity_fieldsAreRequired() {
        mSubmitButton.performClick();

        assertNotNull("Username is required.", mUsername.getError());
        assertNotNull("Password is required.", mPhoneNumber.getError());

        mUsername.getEditText().setText("name");
        mSubmitButton.performClick();

        assertNull("Username error should update.", mUsername.getError());
        assertNotNull("Password is required.", mPhoneNumber.getError());

        mUsername.getEditText().setText("");
        mPhoneNumber.getEditText().setText("111111");
        mSubmitButton.performClick();

        assertNotNull("Username is required.", mUsername.getError());
        assertNull("Phone number error should update.", mPhoneNumber.getError());
    }

    @Test
    public void idActivity_areaCodeIsSetAsDefault() {
        String defaultAreaCode = mActivity.getString(R.string.default_area_code);
        String phoneNumber = "111111";

        mUsername.getEditText().setText("name");
        mPhoneNumber.getEditText().setText(phoneNumber);
        mSubmitButton.performClick();
        Reporter reporter = Util.getCurrentReporter(RuntimeEnvironment.application);
        assertEquals("Un-edited area code should use default.",
                defaultAreaCode+phoneNumber, reporter.phone);

        mAreaCode.getEditText().setText(" ");
        mSubmitButton.performClick();
        reporter = Util.getCurrentReporter(RuntimeEnvironment.application);
        assertEquals("Blank area code should use default.",
                defaultAreaCode+phoneNumber, reporter.phone);
    }

    @Test
    public void onValidSubmit_reporterIsSaved() {
        String name = "name";
        String areacode = "1";
        String number = "111111";

        mUsername.getEditText().setText(name);
        mAreaCode.getEditText().setText(areacode);
        mPhoneNumber.getEditText().setText(number);
        mSubmitButton.performClick();

        Reporter reporter = Util.getCurrentReporter(RuntimeEnvironment.application);
        assertEquals("Name should be saved.", name, reporter.name);
        assertEquals("Area code and phone number should be saved.",
                areacode+number, reporter.phone);
    }

    @Test
    public void onValidSubmit_splashScreenStarts() {
        mUsername.getEditText().setText("name");
        mPhoneNumber.getEditText().setText("111111");
        mSubmitButton.performClick();

        Intent startedIntent = shadowOf(mActivity).getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals("Splash screen should start on submit.",
                SplashScreenActivity.class, shadowIntent.getIntentClass());
    }
}
