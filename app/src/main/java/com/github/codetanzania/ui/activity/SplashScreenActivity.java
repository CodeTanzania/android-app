package com.github.codetanzania.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.codetanzania.api.Open311Api;
import com.github.codetanzania.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class SplashScreenActivity extends RetrofitActivity<ResponseBody> {

    public static final String TAG = "SplashScreen";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override @Nullable
    protected Call<ResponseBody> initializeCall() {

        try {
            // check if user is running this application for the first time
            if (Util.isFirstRun(this, Util.RunningMode.FIRST_TIME_INSTALL)) {
                startActivity(new Intent(this, AppIntroActivity.class));
                finish();
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

    @Override public void onResume() {
        super.onResume();
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
}
