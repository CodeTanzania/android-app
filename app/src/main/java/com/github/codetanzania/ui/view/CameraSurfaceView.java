package com.github.codetanzania.ui.view;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraSurfaceView(Context context) {
        super(context);


        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting but required on android versions prior to 3.0
        // noinspection deprecation
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // when this function returns, mCamera will be null
    private void stopPreviewAndFreeCamera() {
        // Surface will be destroyed when we return, so stop the
        if (mCamera != null) {
            // Call stopPreview to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open during onResume()
            mCamera.release();
            mCamera = null;
        }
    }

    public void setCamera(Camera camera) {
        if (mCamera == camera) {return;}
        stopPreviewAndFreeCamera();
        mCamera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The surface has been created, now tell the camera where to draw the preview
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
