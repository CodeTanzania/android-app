package com.github.codetanzania.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

import tz.co.codetanzania.R;

public class AppIntroActivity extends AppIntro2 {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add slides that shows information to the first timers
        // [1] -- this slide introduces app to user
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_title__open_ticket),
                getString(R.string.intro_desc__open_ticket), R.drawable.ic_phonelink_ring_white_128dp,
                ContextCompat.getColor(this, R.color.colorAccent)));

        // [2] -- this slide will also request permission to read user location, and take photos,
        //     -- and recording audio
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.into_title__extended_support),
                getString(R.string.intro_desc__extended_support), R.drawable.ic_power_white_128dp,
                ContextCompat.getColor(this, R.color.colorAccent)));

        // [3] -- this activity introduces to the user how he/she receives updates about the issues
        //     -- they previously reported to DAWASCO.
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_title__get_notified),
                getString(R.string.intro_desc__get_ntofied), R.drawable.ic_notifications_active_white_128dp,
                ContextCompat.getColor(this, R.color.colorAccent)));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // start splash screen
        startActivity(new Intent(this, SplashScreenActivity.class));
        // we wont come back here
        finish();
    }
}
