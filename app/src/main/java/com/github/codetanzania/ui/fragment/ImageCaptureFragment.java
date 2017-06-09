package com.github.codetanzania.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.codetanzania.util.CameraTask;
import com.github.codetanzania.util.Util;

import tz.co.codetanzania.R;

public class ImageCaptureFragment extends Fragment {

    // private CameraSurfaceView mCameraSurfaceView;
    private FrameLayout mCameraPreview;
    private CameraTask mCamTask;
    private FloatingActionButton mShutterButton;

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
        return view;
    }


    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
    }

    @Override public void onViewCreated(
        View view, Bundle savedInstanceState) {
        // mCameraSurfaceView = new CameraSurfaceView(getActivity());
        mCamTask = new CameraTask(mCameraPreview);
        handleUIEvents();
    }

    @Override public void onResume() {
        super.onResume();
        // It can take a while to grab camera. Good idea to launch it
        // on a separate thread to avoid bogging down UI thread.
        if (mCamTask == null) {
            mCamTask = new CameraTask(mCameraPreview);
        }
        Thread thread = new Thread(new OpenCameraTaskRunnable(mCamTask));
        thread.start();
    }

    @Override public void onPause() {
        super.onPause();
        if (mCamTask != null) {
            Handler handler = new Handler();
            handler.post(new ReleaseCameraTaskRunnable(mCamTask));
            mCamTask = null;
        }
    }

    private void handleUIEvents() {

    }

    private class OpenCameraTaskRunnable implements Runnable {

        private final CameraTask mCamTask;

        public OpenCameraTaskRunnable(CameraTask camTask) {
            this.mCamTask = camTask;
        }

        @Override
        public void run() {
            if (Util.isCameraAvailable(getActivity())) {
                mCamTask.open();
            }
        }
    }

    private class ReleaseCameraTaskRunnable implements Runnable {

        private final CameraTask mCamTask;

        public ReleaseCameraTaskRunnable(CameraTask camTask) {
            this.mCamTask = camTask;
        }

        @Override
        public void run() {
            mCamTask.release();
        }
    }
}
