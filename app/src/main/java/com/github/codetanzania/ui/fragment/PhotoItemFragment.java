package com.github.codetanzania.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import tz.co.codetanzania.R;

public class PhotoItemFragment extends Fragment {

    public static final String TAG =  "PhotoItemFragment";

    public static final String KEY_PHOTO_DATA = PhotoItemFragment.class.getSimpleName() + "/photo_data";

    private Uri mPhotoUri;

    public static final PhotoItemFragment getNewInstance(@NonNull Bundle args) {
        PhotoItemFragment frag = new PhotoItemFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
        // Uri is also parcelable [public abstract class Uri implements Parcelable, Comparable<Uri>]
        mPhotoUri = bundle.getParcelable(KEY_PHOTO_DATA);
        // debug
        Log.d(TAG, String.format("%s", mPhotoUri));
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_PHOTO_DATA, mPhotoUri);
        super.onSaveInstanceState(outState);
    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.card_view_issue_image, viewGroup, false);
        ImageView imgView = (ImageView) fragView.findViewById(R.id.imgView_Picture);
        Picasso.with(imgView.getContext())
                .load(mPhotoUri)
                .into(imgView);
        return fragView;
    }
}
