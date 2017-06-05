package com.github.codetanzania.ui.fragment;


import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.github.codetanzania.ui.view.CameraSurfaceView;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class ImageCaptureFragment extends Fragment {

    // We're using deprecated API because we're also targeting lower android devices
    // (pre-lollipop devices)
    private Camera mCamera;
    private CameraSurfaceView mCameraSurfaceView;
    private FrameLayout mCameraPreview;

    private Button mCaptureButton;

    public static ImageCaptureFragment getNewInstance(@Nullable Bundle args) {
        ImageCaptureFragment frag = new ImageCaptureFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // It can take a while to grab camera. Good idea to launch it
        // on a separate thread to avoid bogging down UI thread.

    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState ) {
        View view = inflater.inflate(R.layout.frag_issue_description, viewGroup, false);
        mCameraPreview = (FrameLayout) view.findViewById(R.id.fr_CameraPreview);
        mCaptureButton = (Button) view.findViewById(R.id.btn_CaptureMoment);
        return view;
    }


    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
    }

    @Override public void onViewCreated(
        View view, Bundle savedInstanceState) {
        mCameraSurfaceView = new CameraSurfaceView(getActivity());
        mCameraPreview.addView(mCameraSurfaceView);
        handleUIEvents();
    }

    @Override public void onResume() {
        super.onResume();
        // [1] -- check if camera is available and we can open it
        if (Util.isCameraAvailable(getActivity()) && openCamera()) {
            // [2] -- start preview
            mCameraSurfaceView.setCamera(mCamera);
        }
    }

    @Override public void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    private void handleUIEvents() {

    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean openCamera() {
        boolean opened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open();
            opened = (mCamera != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return opened;
    }
}
