package com.github.codetanzania.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.codetanzania.ui.fragment.IntroSlideFragment;
import com.github.codetanzania.util.LanguageUtils;
import com.github.paolorotolo.appintro.AppIntro2;

import tz.co.codetanzania.R;

public class AppIntroActivity extends AppIntro2 {


    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // apply language changes
        LanguageUtils.withBaseContext(getBaseContext())
                .commitChanges();

        @ColorInt int backgroundColor = ContextCompat.getColor(this, R.color.introBackground);
        @ColorInt int titleColor = ContextCompat.getColor(this, R.color.colorAccent);
        @ColorInt int descriptionColor = ContextCompat.getColor(this, R.color.introText);

        // add slides that shows information to the first timers
        // [1] -- this slide introduces app to user
        addSlide(IntroSlideFragment.newInstance(getString(R.string.intro_title__open_ticket),
                getString(R.string.intro_desc__open_ticket),
                R.drawable.ic_intro_register,
                backgroundColor, titleColor, descriptionColor));

        // [2] -- this slide will also request permission to read user location, and take photos,
        //     -- and recording audio
        addSlide(IntroSlideFragment.newInstance(getString(R.string.intro_title__extended_support),
                getString(R.string.intro_desc__extended_support),
                R.drawable.ic_intro_support,
                backgroundColor, titleColor, descriptionColor));

        // [3] -- this activity introduces to the user how he/she receives updates about the issues
        //     -- they previously reported to the municipal water company.
        addSlide(IntroSlideFragment.newInstance(getString(R.string.intro_title__get_notified),
                getString(R.string.intro_desc__get_notified),
                R.drawable.ic_intro_notification,
                backgroundColor, titleColor, descriptionColor));
    }

    private void startSplashScreenActivity() {
        // start splash screen
        startActivity(new Intent(this, SplashScreenActivity.class));
        // we wont come back here
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_intro;
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // show language chooser dialog
        startSplashScreenActivity();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        onDonePressed(currentFragment);
    }
}
