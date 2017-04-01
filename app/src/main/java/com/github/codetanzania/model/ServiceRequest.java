package com.github.codetanzania.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// @Table(name = "service_request", id = BaseColumns._ID)
public class ServiceRequest implements Parcelable {

    private static final String TAG = "ServiceRequest";

    public ServiceRequest(Parcel in) {
        jurisdiction = in.readParcelable(Jurisdiction.class.getClassLoader());
        service = in.readParcelable(Service.class.getClassLoader());
        reporter = in.readParcelable(Reporter.class.getClassLoader());
        address = in.readString();
        longitude = in.readString();
        latitude = in.readString();
        attachments = in.createStringArrayList();
        comments = in.createTypedArrayList(Comment.CREATOR);
        status = in.readParcelable(Status.class.getClassLoader());
        resolvedAt = new Date(in.readLong());
    }

    public ServiceRequest() {}

    public static final Creator<ServiceRequest> CREATOR = new Creator<ServiceRequest>() {
        @Override
        public ServiceRequest createFromParcel(Parcel in) {
            return new ServiceRequest(in);
        }

        @Override
        public ServiceRequest[] newArray(int size) {
            return new ServiceRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(jurisdiction, i);
        parcel.writeParcelable(service, i);
        parcel.writeParcelable(reporter, i);
        parcel.writeString(address);
        parcel.writeString(longitude);
        parcel.writeString(latitude);
        parcel.writeStringList(attachments);
        parcel.writeTypedList(comments);
        parcel.writeParcelable(status, i);
        if (resolvedAt != null) {
            parcel.writeLong(resolvedAt.getTime());
        }
    }

    public static class Status implements Parcelable {
        public String name;
        public float weight;
        public String color;
        public Date updatedAt;
        @NonNull
        public Date createdAt;

        protected Status(Parcel in) {
            name = in.readString();
            weight = in.readFloat();
            color = in.readString();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
                updatedAt = sdf.parse(in.readString());
                createdAt = sdf.parse(in.readString());
            } catch (Exception e) {
                Log.e(TAG, "ERROR: " + e.getMessage());
            }
        }

        public static final Creator<Status> CREATOR = new Creator<Status>() {
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
            parcel.writeString(name);
            parcel.writeFloat(weight);
            parcel.writeString(color);
            parcel.writeString(updatedAt.toString());
            parcel.writeString(createdAt.toString());
        }
    }

   /* public enum Priority {
        LOW, NORMAL, HIGH
    }*/

    // @Column(name = "jurisdiction")
    public Jurisdiction jurisdiction;

    // @Column(name = "service")
    public Service service;

    // @Column(name = "reporter")
    public Reporter reporter;

    // @Column(name = "address")
    public String address;

    // @Column(name = "longitude")
    public String longitude;

    // @Column(name = "latitude")
    public String latitude;

    // @Column(name = "status")
    public Status status;

    // @Column(name = "priority")
    // public Priority      priority;

    public List<String>  attachments;
    public List<Comment> comments;

    // @Column(name = "resolved_at")
    public Date resolvedAt;

    // take comma separated strings and convert into an array of strings
    // public void setAttachments(String...attachments) {
    //    this.attachments = Arrays.asList(attachments);
    // }

    // List<Comment> getComments() {
    //    return getMany(Comment.class, "comment");
    // }


    @Override
    public String toString() {
        return "ServiceRequest{" +
                "jurisdiction=" + jurisdiction +
                ", service=" + service +
                ", reporter=" + reporter +
                ", address='" + address + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", status=" + status +
                ", attachments=" + attachments +
                ", comments=" + comments +
                ", resolvedAt=" + resolvedAt +
                '}';
    }
}