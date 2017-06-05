package com.github.codetanzania.ui.fragment;


import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;
import java.util.List;

import tz.co.codetanzania.R;

public class ImageCaptureFragment extends Fragment {

    // We're using deprecated API because we're also targeting lower android devices
    private Camera mCamera;
    private Preview mPreview;
    private List<Camera.Size> mSupportedPreviewSizes;

    private TextureView mCameraPreview;
    private Button mCaptureButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // It can take a while to grab camera. Good idea to launch it
        // on a separate thread to avoid bogging down UI thread.

    }

    @Override public View onCreateView(
            LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState ) {
        return inflater.inflate(R.layout.frag_image_capture, viewGroup, false);
    }


    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
    }

    @Override public void onViewCreated(
        View view, Bundle savedInstanceState) {
        mCameraPreview = (TextureView) view.findViewById(R.id.img_CameraPreview);
        mCaptureButton = (Button) view.findViewById(R.id.btn_CaptureMoment);
        handleUIEvents();
    }

    private void handleUIEvents() {

    }

    private boolean safeCameraOpen(int id) {
        boolean opened = false;
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            opened = (mCamera != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return opened;
    }

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    class Preview extends ViewGroup implements SurfaceHolder.Callback {

        SurfaceView mSurefaceView;
        SurfaceHolder mHolder;

        public Preview(Context context) {
            super(context);

            mSurefaceView = new SurfaceView(context);
            addView(mSurefaceView);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed
            mHolder = mSurefaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // when this function returns, mCamera will be null
        private void stopPreviewAndFreeCamera() {}

        public void setCamera(Camera camera) {
            if (mCamera == camera) {return;}

            stopPreviewAndFreeCamera();

            mCamera = camera;

            if (mCamera != null) {
                List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
                mSupportedPreviewSizes = localSizes;
                requestLayout();

                try {
                    mCamera.setPreviewDisplay(mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Important: Call startPreview() to start updating the preview
                // surface. Preview must be started before you can take a picture
                mCamera.startPreview();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Now that the size is known, set up the camera parameter and begin the preview
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(mPreview.getWidth(), mPreview.getHeight());
            requestLayout();
            mCamera.setParameters(params);

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {

        }
    }
}
