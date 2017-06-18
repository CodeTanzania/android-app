package com.github.codetanzania.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;

import com.github.codetanzania.Constants;
import com.github.codetanzania.model.ServiceRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tz.co.codetanzania.R;

public class ServiceRequestsUtil {

    public static final String TAG = "ServiceRequestsUtil";

    public static final void save(Context ctx, ServiceRequest[] requests) {
        // save the requests to the shared preferences
        SharedPreferences mPrefs = ctx.getSharedPreferences(
                Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public static ServiceRequest oneFromJson(String json) {
        return null;
    }

    public static ArrayList<ServiceRequest> fromJson(String json) throws IOException {

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat(DateFormat.FULL, DateFormat.FULL)
                .registerTypeAdapter(Date.class, new JsonDateSerializer())
                // See http://stackoverflow.com/questions/32431279/android-m-retrofit-json-cant-make-field-constructor-accessible
                // .excludeFieldsWithModifiers(Modifier.STATIC)
                // .excludeFieldsWithoutExposeAnnotation()
                .create();
        JsonElement jsElement = new JsonParser().parse(json);
        Log.d(TAG, "An Object is " + gson.toJson(jsElement));
        JsonObject  jsObject  = jsElement.getAsJsonObject();
        JsonArray   jsArray   = jsObject.getAsJsonArray("servicerequests");
        Log.d(TAG, gson.toJson(jsArray));
        ServiceRequest[] requests = gson.fromJson(jsArray, ServiceRequest[].class);
        ArrayList<ServiceRequest> list = new ArrayList<>(requests.length);
        for (int i = 0; i < requests.length; i++) {
            Log.d(TAG, requests[i].createdAt.toString());
            list.add(requests[i]);
        }
        return list;
    }
}
