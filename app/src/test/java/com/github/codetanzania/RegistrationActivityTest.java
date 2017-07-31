package com.github.codetanzania;

import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.github.codetanzania.model.Reporter;
import com.github.codetanzania.ui.activity.RegistrationActivity;
import com.github.codetanzania.ui.activity.SplashScreenActivity;
import com.github.codetanzania.util.Util;
import com.github.codetanzania.utils.ProfileFragmentTest;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import tz.co.codetanzania.R;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * This unit test is used to test the initial registration screen
 * shown when a user first opens the app. Validation tests of user
 * inputs are contained in parent ProfileFragmentTest class.
 */

public class RegistrationActivityTest extends ProfileFragmentTest {

    @Override
    public FragmentActivity setActivity() {
        return Robolectric.buildActivity(
                RegistrationActivity.class).create().start().resume().get();
    }

    @Override
    public int getSubmitButtonResId() {
        return R.id.btn_Next;
    }

    @Test
    public void onValidSubmit_splashScreenStarts() {
        submitValidUser();

        assertEquals("Splash screen should start on submit.",
                SplashScreenActivity.class, getClassOfStartedActivity());
    }
}
