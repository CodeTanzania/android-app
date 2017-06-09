package com.github.codetanzania.util;

import android.hardware.Camera;
import android.widget.FrameLayout;

import com.github.codetanzania.ui.view.CameraSurfaceView;

public class CameraTask {

    // singleton instance
    private PhotoManager sPhotoManager = PhotoManager.getInstance();

    // We're using deprecated API because we're also targeting lower android devices
    // (pre-lollipop devices)
    Camera mCamera;
    // CameraSurfaceView mCameraSurfaceView;
    final FrameLayout mCameraPreviewFrame;

    public CameraTask(FrameLayout frameLayout) {
        this.mCameraPreviewFrame = frameLayout;
    }

    public void open() {
        try {
            // release();
            mCamera = Camera.open();
            if (mCamera != null) {
                handleState(PhotoManager.CAMERA_READY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleState(PhotoManager.CAMERA_ERROR);
        }
    }

    public void release() {
        handleState(PhotoManager.RELEASE_CAMERA);
    }

    // passes the state to the PhotoManager
    void handleState(int state) {
            /*
             * Passes a handle to this task and the current state to the class that created
             * thread pool
             */
        sPhotoManager.handleState(this, state);
    }
}