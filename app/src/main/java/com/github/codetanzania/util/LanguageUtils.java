package com.github.codetanzania.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.github.codetanzania.Constants;

import java.util.Locale;

public class LanguageUtils {

    private final Context mBaseCtx;


    public static final String ENGLISH_LANG_CODE = "en";
    public static final String SWAHILI_LANG_CODE = "sw";

    public static final String ENGLISH_LANG = "English";
    public static final String SWAHILI_LANG = "Swahili";

    public final Runnable languageChangeRunnable = new Runnable() {
        @Override
        public void run() {
            commitChanges();
        }
    };

    private LanguageUtils(Context ctx) {
        mBaseCtx = ctx;
    }

    /* withBaseContext to remind for a `base context` instead of regular `activity context` */
    public static LanguageUtils withBaseContext(Context ctx) {
        return new LanguageUtils(ctx);
    }

    public String getDefaultLanguageCode() {
        return mBaseCtx.getSharedPreferences(
                Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE)
                .getString(Constants.KEY_DEFAULT_LANGUAGE, "en");
    }

    public String getDefaultLanguageName() {
        String code = getDefaultLanguageCode();
        if ( code.equals(ENGLISH_LANG_CODE) ) {
            return ENGLISH_LANG;
        } else if ( code.equals(SWAHILI_LANG_CODE) ) {
            return SWAHILI_LANG;
        } else {
            throw new UnsupportedOperationException("Unsupported Language " + code);
        }
    }

    public LanguageUtils setDefaultLanguage(@StringRes int languageCode) {
        return setDefaultLanguage(mBaseCtx.getString(languageCode));
    }

    public LanguageUtils setDefaultLanguage(@NonNull String language) {

        String selectedLanguage;

        boolean isEnglish = language.equalsIgnoreCase(ENGLISH_LANG) || language.equalsIgnoreCase(ENGLISH_LANG_CODE),
                isSwahili = language.equalsIgnoreCase(SWAHILI_LANG) || language.equalsIgnoreCase(ENGLISH_LANG_CODE);

        if (isEnglish) {
            selectedLanguage = ENGLISH_LANG_CODE;
        } else if (isSwahili) {
            selectedLanguage = SWAHILI_LANG_CODE;
        } else {
            throw new UnsupportedOperationException("Unsupported Language " + language);
        }

        SharedPreferences prefs = mBaseCtx.getSharedPreferences(
                Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.KEY_DEFAULT_LANGUAGE, selectedLanguage).apply();

        return this;
    }

    public LanguageUtils setSwahiliAsDefaultLanguage() {
        return setDefaultLanguage(SWAHILI_LANG_CODE);
    }

    public LanguageUtils setEnglishAsDefaultLanguage() {
        return setDefaultLanguage(ENGLISH_LANG_CODE);
    }

    public void commitChanges() {

        SharedPreferences prefs =
                mBaseCtx.getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        Locale newLocale = new Locale(prefs.getString(Constants.KEY_DEFAULT_LANGUAGE, ENGLISH_LANG_CODE));
        Locale.setDefault(newLocale);

        Configuration configs = new Configuration();
        configs.setLocale(newLocale);

        mBaseCtx.getResources().updateConfiguration(configs,
                mBaseCtx.getResources().getDisplayMetrics());
    }

    public void applyChanges() {
        Handler handler = new Handler();
        handler.post(languageChangeRunnable);
    }
}