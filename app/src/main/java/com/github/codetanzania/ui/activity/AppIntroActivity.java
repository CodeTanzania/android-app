package com.github.codetanzania.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.github.codetanzania.ui.SingleItemSelectionDialog;
import com.github.codetanzania.ui.fragment.IntroSlideFragment;
import com.github.codetanzania.util.LanguageUtils;
import com.github.paolorotolo.appintro.AppIntro2;

import tz.co.codetanzania.R;

public class AppIntroActivity extends AppIntro2 implements
        SingleItemSelectionDialog.OnAcceptSelection,
        DialogInterface.OnClickListener,
        DialogInterface.OnCancelListener,
        DialogInterface.OnDismissListener {

    private String mSelectedLanguage;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //     -- they previously reported to DAWASCO.
        addSlide(IntroSlideFragment.newInstance(getString(R.string.intro_title__get_notified),
                getString(R.string.intro_desc__get_notified),
                R.drawable.ic_intro_notification,
                backgroundColor, titleColor, descriptionColor));
    }

    private void startSplashScreenActivity() {
        LanguageUtils languageUtils = LanguageUtils.withBaseContext(getBaseContext());
        if (!TextUtils.isEmpty(mSelectedLanguage) &&
                mSelectedLanguage.equals(LanguageUtils.SWAHILI_LANG)) {
            languageUtils.setSwahiliAsDefaultLanguage();
        } else {
            languageUtils.setEnglishAsDefaultLanguage();
        }
        // start splash screen
        startActivity(new Intent(this, SplashScreenActivity.class));
        // we wont come back here
        finish();
    }

    private void showLanguagePickerDialog() {
        SingleItemSelectionDialog itemSelectionDialog = SingleItemSelectionDialog.Builder.withContext(this)
            .addItems(LanguageUtils.ENGLISH_LANG, LanguageUtils.SWAHILI_LANG)
            .setActionSelectText(R.string.action_select)
            .setActionCancelText(R.string.text_cancel)
            .setOnAcceptSelection(this)
            .setOnActionListener(this)
            .setOnCancelListener(this)
            .setOnDismissListener(this)
            .setTitle(R.string.title_select_default_language)
            .build();
        itemSelectionDialog.open();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_intro;
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // show language chooser dialog
        showLanguagePickerDialog();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        onDonePressed(currentFragment);
    }

    @Override
    public void onItemSelected(String item, int position) {
        mSelectedLanguage = item;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        startSplashScreenActivity();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        startSplashScreenActivity();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        startSplashScreenActivity();
    }
}
