package com.github.codetanzania.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// @Table(name = "open311Service", _id = BaseColumns._ID)
public class Open311Service implements Parcelable {

    private transient static final String ID   = "_id";
    private transient static final String CODE = "code";
    private transient static final String NAME = "name";
    private transient static final String DESCRIPTION = "description";
    private transient static final String COLOR = "color";

    public String id;

    // @Column(name = "code")
    public String code;

    // @Column(name = "name")
    public String name;

    // @Column(name = "description")
    public String description;

    // @Column(name = "color")
    public String color;

    public Open311Service() {}

    protected Open311Service(Parcel in) {
        id   = in.readString();
        code = in.readString();
        name = in.readString();
        description = in.readString();
        color = in.readString();
    }

    // God I hate reflection mechanism. I better do this!
    public static List<Open311Service> fromJson(String jsonStr) throws JSONException {
        List<Open311Service> res = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONArray jsonArray   = jsonObject.getJSONArray("services");

        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Open311Service open311Service = new Open311Service();
            open311Service.id   = obj.getString(ID);
            open311Service.name = obj.getString(NAME);
            open311Service.code = obj.getString(CODE);
            open311Service.description = obj.getString(DESCRIPTION);
            open311Service.color = obj.getString(COLOR);
            res.add(open311Service);
        }

        return res;
    }

    private static void deserializeJSON(JsonArray jsArray) {

    }

    public static final Creator<Open311Service> CREATOR = new Creator<Open311Service>() {
        @Override
        public Open311Service createFromParcel(Parcel in) {
            return new Open311Service(in);
        }

        @Override
        public Open311Service[] newArray(int size) {
            return new Open311Service[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(color);
    }

    @Override
    public String toString() {
        return "Open311Service{" +
                "id='" + id + '\'' +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}