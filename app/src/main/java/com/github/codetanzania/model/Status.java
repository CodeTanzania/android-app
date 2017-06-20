package com.github.codetanzania.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is used to mark issue status.
 */

public class Status implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OPEN, CLOSED})
    public @interface Type {}
    public static final int OPEN = 0;
    public static final int CLOSED = 1;

    @Type
    public int type;

    public String color;

    public Status(@Type int type, String color){
        this.type = type;
        this.color = color;
    }

    @SuppressWarnings("ResourceType")
    private Status(Parcel in) {
        type = in.readInt();
        color = in.readString();
    }

    public static final Parcelable.Creator<Status> CREATOR =
            new Parcelable.Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(type);
        parcel.writeString(color);
    }
}
