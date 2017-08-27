package com.github.codetanzania.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.github.codetanzania.Constants;

import java.util.Locale;

public class LanguageUtils {

    private final Context mBaseCtx;

    private String mDefaultLang;

    public static final String ENGLISH_LANG_CODE = "en";
    public static final String SWAHILI_LANG_CODE = "sw";

    public static final String ENGLISH_LANG = "English";
    public static final String SWAHILI_LANG = "Swahili";

    private LanguageUtils(Context ctx) {
        mBaseCtx = ctx;
    }

    /* withBaseContext reminds for `base context` instead of regular `activity context` */
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
            return "English";
        } else if ( code.equals(SWAHILI_LANG_CODE) ) {
            return "Swahili";
        } else {
            throw new UnsupportedOperationException("Unsupported Language " + code);
        }
    }

    public LanguageUtils setDefaultLanguage(String language) {
        this.mDefaultLang = language;
        return this;
    }

    public LanguageUtils setSwahiliAsDefaultLanguage() {
        return setDefaultLanguage("sw");
    }

    public LanguageUtils setEnglishAsDefaultLanguage() {
        return setDefaultLanguage("en");
    }

    public void applyChanges() {

        SharedPreferences prefs =
                mBaseCtx.getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);

        boolean langChanged = !TextUtils.isEmpty(mDefaultLang) &&
                prefs.getString(Constants.KEY_DEFAULT_LANGUAGE, "en").equals(mDefaultLang);

        if (langChanged) {

            Locale newLocale = new Locale(mDefaultLang);
            Locale.setDefault(newLocale);

            Configuration configs = new Configuration();
            configs.setLocale(newLocale);

            mBaseCtx.getResources().updateConfiguration(configs,
                    mBaseCtx.getResources().getDisplayMetrics());

            prefs.edit()
                    .putString(Constants.KEY_DEFAULT_LANGUAGE, mDefaultLang)
                    .apply();
        }
    }
}