package com.github.codetanzania.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.codetanzania.Constants;
import com.github.codetanzania.model.Reporter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Util {

    public static final String TAG = "Util";

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public enum RunningMode {
        FIRST_TIME_INSTALL,
        FIRST_TIME_UPGRADE
    }


    public static boolean isFirstRun(Context mContext, RunningMode mRunningMode) throws Exception {

        int currentVersionCode, savedVersionCode;

        try {
            currentVersionCode = mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0)
                    .versionCode;

        } catch(android.content.pm.PackageManager.NameNotFoundException e) {
            Log.e(TAG, String.format("An exception is %s", e.getMessage()));
            throw new Exception(
                    String.format("Package name not found. Original exception was: %s ", e.getMessage()));
        }

        SharedPreferences sharedPrefs = mContext
                .getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);

        savedVersionCode = sharedPrefs.getInt(Constants.Const.APP_VERSION_CODE, -1);

        boolean firstTimeRun = savedVersionCode == -1;
        boolean upgradeRun   = savedVersionCode <  currentVersionCode;

        if (firstTimeRun || upgradeRun) {
            sharedPrefs.edit().putInt(
                    Constants.Const.APP_VERSION_CODE, currentVersionCode).apply();
        }

        if (mRunningMode == RunningMode.FIRST_TIME_INSTALL) {
            return firstTimeRun;
        } else {
            return mRunningMode == RunningMode.FIRST_TIME_UPGRADE && upgradeRun;
        }
    }

    public static File createImageFile(Context mContext) throws IOException {
        // Create file that avoids name collisions
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Save a file: path for use with ACTION_VIEW intents
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static Reporter getCurrentReporter(Context mContext) {
        SharedPreferences sharedPrefs = mContext.getSharedPreferences(
                Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        String phone = sharedPrefs.getString(Constants.Const.REPORTER_PHONE, null);

        // logical to use phone number which we verify through OTP
        if (phone == null) {
            return null;
        }

        String email = sharedPrefs.getString(Constants.Const.REPORTER_EMAIL, null);
        String account = sharedPrefs.getString(Constants.Const.REPORTER_DAWASCO_ACCOUNT, null);
        String fullName = sharedPrefs.getString(Constants.Const.REPORTER_NAME, null);

        Reporter reporter = new Reporter();
        reporter.account = account;
        reporter.phone = phone;
        reporter.name = fullName;
        reporter.email = email;

        return reporter;
    }

    public static void storeCurrentReporter(Context mContext, Reporter reporter) {
        SharedPreferences sharedPrefs = mContext.getSharedPreferences(
                Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        sharedPrefs.edit()
                .putString(Constants.Const.REPORTER_NAME, reporter.name)
                .putString(Constants.Const.REPORTER_PHONE, reporter.phone)
                .putString(Constants.Const.REPORTER_EMAIL, reporter.email)
                .putString(Constants.Const.REPORTER_DAWASCO_ACCOUNT, reporter.account)
                .apply();
    }

    public static void storeAuthToken(Context mContext, String mToken) {
        SharedPreferences sharedPrefs = mContext.getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
            sharedPrefs.edit()
                    .putString(Constants.Const.AUTH_TOKEN, mToken)
                    .apply();
    }

    public static String getAuthToken(Context mContext) {
        SharedPreferences mPrefs = mContext.getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        return mPrefs.getString(Constants.Const.AUTH_TOKEN, null);
    }

    public static void storeUserId(Context mContext, String mUserId) {
        SharedPreferences mPrefs = mContext.getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        mPrefs.edit()
                .putString(Constants.Const.CURRENT_USER_ID, mUserId)
                .apply();
    }

    public static String parseJWTToken(String input) throws JSONException {
        JSONObject jsObj = new JSONObject(input);
        return jsObj.getString("token");
    }

    public static String parseUserId(String input) throws JSONException {
        JSONObject jsObj = new JSONObject(input);
        return jsObj.getJSONObject("party").getString("_id");
    }

    public static String inferContentType(@NonNull String urlStr) {

        String exts[][] =
            {{"aac", "mp3", "ogg"},
             {"flv", "mp4", "webm"},
             {"jpeg", "jpg", "png"}};

        String parties[] = {"audio", "video", "image"};
        int index; String ext = "binary/octet-stream", haystack = urlStr.substring(1 + urlStr.lastIndexOf("."));
        Log.d(TAG, haystack);
        for (int i = 0; i < exts.length; ++i) {
            index = Arrays.binarySearch(exts[i], haystack);
            if (index >= 0) {
                ext = String.format("%s/%s", parties[i], exts[i][index]);
                break;
            }
        }
        return ext;
    }

    public static boolean isGPSOn(Context context) {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String formatDate(
            @NonNull Date d, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        return sdf.format(d);
    }

    public static void resetPreferences(Context mContext) {
        mContext.getSharedPreferences(
                Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
