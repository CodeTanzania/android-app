package com.github.codetanzania.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.github.codetanzania.util.camera.PhotoManager;

import tz.co.codetanzania.R;

public class ImageCaptureFragment extends Fragment {

    // private CameraSurfaceView mCameraSurfaceView;
    private FrameLayout mCameraPreview;
    private FloatingActionButton mShutterButton;
    private EditText mEditText;

    public static ImageCaptureFragment getNewInstance(@Nullable Bundle args) {
        ImageCaptureFragment frag = new ImageCaptureFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState ) {
        View view = inflater.inflate(R.layout.frag_issue_description, viewGroup, false);
        mCameraPreview = (FrameLayout) view.findViewById(R.id.fr_CameraPreview);
        mShutterButton = (FloatingActionButton) view.findViewById(R.id.btn_Shutter);
        mEditText      = (EditText) view.findViewById(R.id.et_Msg);
        return view;
    }


    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
    }

    @Override public void onViewCreated(
        View view, Bundle savedInstanceState) {
        // mCameraSurfaceView = new CameraSurfaceView(getActivity());
        handleUIEvents();
    }

    @Override public void onResume() {
        super.onResume();
        // It can take a while to grab camera. Good idea to launch it
        // on a separate thread to avoid bogging down UI thread.
        PhotoManager.getInstance().startCameraRoutine(mCameraPreview, mShutterButton);
    }

    @Override public void onPause() {
        super.onPause();
        PhotoManager.getInstance().stopCameraRoutine();
    }

    private void handleUIEvents() {
        // capture picture
        mShutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PhotoManager.getInstance().isPreviewActive()) {
                    // Take photo
                    PhotoManager.getInstance().capturePicture();
                } else {
                    // Restore preview
                    PhotoManager.getInstance().startPreview();
                }
                // update image
                mShutterButton.setImageResource(
                        PhotoManager.getInstance().isPreviewActive() ?
                                R.drawable.ic_close_white_24dp :
                                R.drawable.ic_add_a_photo_black_24dp
                );

            }
        });

        // when the keyboard focus changes
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                getActivity().invalidateOptionsMenu();
            }
        });
    }
}
