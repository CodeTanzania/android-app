package com.github.codetanzania.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.codetanzania.api.model.Open311Service;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the type of service offered.
 */

public class Service implements Parcelable {
    public String code;
    public String name;
    public String color;

    public Service(String code, String name, String color) {
        this.code = code;
        this.name = name;
        this.color = color;
    }

    private Service(Parcel in) {
        code = in.readString();
        name = in.readString();
        color = in.readString();
    }

    public static final Parcelable.Creator<Service> CREATOR =
            new Parcelable.Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeString(color);
    }

    @Override
    public String toString() {
        return "Service{" +
                "code='" + code + '\'' +
                ", type='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
