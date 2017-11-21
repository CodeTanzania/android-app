package com.github.codetanzania.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.event.Analytics;
import com.github.codetanzania.ui.SingleItemSelectionDialog;
import com.github.codetanzania.util.LanguageUtils;
import com.github.codetanzania.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import tz.co.codetanzania.R;

public class SplashScreenActivity extends RetrofitActivity<ResponseBody> {

    public static final String TAG = "SplashScreen";

    private final LanguageChangeFacade languageChangeFacade = new LanguageChangeFacade();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // apply default language
        LanguageUtils.withBaseContext(getBaseContext()).commitChanges();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Start analytics and crash reporting
        Analytics.initialize(this);
    }

    @Override @Nullable
    protected Call<ResponseBody> initializeCall() {

        try {
            // check if user is running this application for the first time
            if (Util.isFirstRun(this, Util.RunningMode.FIRST_TIME_INSTALL)) {
                // ask language preference
                showLanguagePickerDialog();
                return null;
            }
        } catch (Exception e) {
            // ignore
        }

        // if token is cached then go to home screen or id activity depending
        // on the state
        if (!TextUtils.isEmpty(Util.getAuthToken(this))) {
            startNextActivity();
            return null;
        }

        // todo: remove the next hardcoded lines when the api is ready to work with phone numbers
        Map<String, String> map = new HashMap<>();
        map.put("email", "lallyelias87@gmail.com");
        map.put("password", "open311@qwerty");
        return new Open311Api
                .ServiceBuilder(this)
                .build(Open311Api.AuthEndpoint.class)
                .signIn(map);
    }

    private void showLanguagePickerDialog() {
        SingleItemSelectionDialog itemSelectionDialog = SingleItemSelectionDialog.Builder.withContext(this)
            .addItems(getResources().getStringArray(R.array.languages))
            .setActionSelectText(R.string.action_select)
            .setActionCancelText(R.string.text_cancel)
            .setOnAcceptSelection(languageChangeFacade)
            .setOnActionListener(languageChangeFacade)
            .setOnCancelListener(languageChangeFacade)
            .setOnDismissListener(languageChangeFacade)
            .setTitle(R.string.title_select_default_language)
            .build();
        itemSelectionDialog.open();
    }

    @Override
    protected ResponseBody getData(Response<ResponseBody> response) {
        return response.isSuccessful() ?
                response.body() :
                response.errorBody();
    }

    @Override
    public void onProcessResponse(ResponseBody body, int httpStatusCode) {
        storeAuthToken(body);
        startNextActivity();
        super.onProcessResponse(body, httpStatusCode);
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        super.onFailure(call, t);
        Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, ErrorActivity.memoizeClass(SplashScreenActivity.class)));
    }

    /*
     * start appropriate activity
     */
    private void startNextActivity() {
        if (Util.getCurrentReporter(this) == null) {
            // start registration activity
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        } else {
            // go home
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void storeAuthToken(ResponseBody responseBody) {
        try {
            String jsonString = responseBody.string();
            String token = Util.parseJWTToken(jsonString);
            Util.storeAuthToken(this, token);
            String userId = Util.parseUserId(jsonString);
            Util.storeUserId(this, userId);
        } catch (IOException | JSONException exception) {
            // show an error
            Toast.makeText(this, "Error authenticating user.", Toast.LENGTH_LONG)
                    .show();
        }
    }


    private class LanguageChangeFacade implements
            SingleItemSelectionDialog.OnAcceptSelection,
            DialogInterface.OnClickListener, DialogInterface.OnCancelListener,
            DialogInterface.OnDismissListener {

        private String mSelectedLanguage;

        private void startSplashScreenActivity() {

            LanguageUtils languageUtils = LanguageUtils.withBaseContext(getBaseContext());

            if (!TextUtils.isEmpty(mSelectedLanguage)) {

                String[] languages = getResources().getStringArray(R.array.languages);

                for (String lang: languages) {
                    if (lang.equals(mSelectedLanguage)) {
                        languageUtils.setDefaultLanguage(lang);
                        break;
                    }
                }
            } else {
                // set swahili as a default language
                languageUtils.setSwahiliAsDefaultLanguage();
            }

            // finish the activity. No need to be able to get back to the splash screen activity
            startActivity(new Intent(SplashScreenActivity.this, AppIntroActivity.class));
            finish();
        }

        @Override
        public void onItemSelected(String item, int position) {
            this.mSelectedLanguage = item;
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
}
