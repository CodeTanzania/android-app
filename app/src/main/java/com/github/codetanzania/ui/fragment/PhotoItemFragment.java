package com.github.codetanzania.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class PhotoItemFragment extends Fragment {

    public static final String KEY_PHOTO_DATA = PhotoItemFragment.class.getSimpleName() + "/photo_data";

    private String mPhotoData;

    public static final PhotoItemFragment getNewInstance(@NonNull Bundle args) {
        PhotoItemFragment frag = new PhotoItemFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
        mPhotoData = bundle.getString(KEY_PHOTO_DATA);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_PHOTO_DATA, mPhotoData);
        super.onSaveInstanceState(outState);
    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.card_view_issue_image, viewGroup, false);
        ImageView imgView = (ImageView) fragView.findViewById(R.id.imgView_Picture);
        imgView.setImageBitmap(Util.decodeBase64(mPhotoData));
        return fragView;
    }
}
