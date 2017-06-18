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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;
import tz.co.codetanzania.R;

public class ServiceRequestsUtil {

    public static final String TAG = "ServiceRequestsUtil";

    public static void save(Context ctx, ServiceRequest[] requests) {
        // save the requests to the shared preferences
        SharedPreferences mPrefs = ctx.getSharedPreferences(
                Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public static void sort(List<ServiceRequest> requests) {
        Collections.sort(requests, NewestFirstComparator);
    }

    public static ArrayList<ServiceRequest> fromResponseBody(Response<ResponseBody> response) {
        if (response.isSuccessful()) {
            ResponseBody data = response.body();
            if (data != null) {
                try {
                    return fromJson(data.string());
                } catch (IOException e) {
                    Log.e(TAG, String.format("An error was %s", e.getMessage()));
                }
            }
        }
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
        Collections.addAll(list, requests);
        return list;
    }

    public static int daysBetween(Date date1, Date date2){
        Calendar dayOne = Calendar.getInstance(),
                dayTwo = Calendar.getInstance();
        dayOne.setTime(date1);
        dayTwo.setTime(date2);

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays ;
        }
    }

    private static Comparator<ServiceRequest> NewestFirstComparator
            = new Comparator<ServiceRequest>() {

        public int compare(ServiceRequest request1, ServiceRequest request2) {
            if (request1 == null || request2 == null) {
                return -1;
            }
            Date firstDate = request1.createdAt;
            Date secondDate = request2.createdAt;

            return secondDate.compareTo(firstDate);
        }

    };
}
