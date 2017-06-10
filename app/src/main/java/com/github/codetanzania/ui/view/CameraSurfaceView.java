package com.github.codetanzania.ui.view;

import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Display mDisplay;

    public CameraSurfaceView(Context context) {
        super(context);


        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting but required on android versions prior to 3.0
        // noinspection deprecation
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    // when this function returns, mCamera will be null
    private void stopPreviewAndFreeCamera() {
        // Surface will be destroyed when we return, so stop the
        if (mCamera != null) {
            // Call stopPreview to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-openCamera during onResume()
            mCamera.release();
        }
    }

    public void setCamera(Camera camera) {
        if (mCamera == camera) {return;}
        stopPreviewAndFreeCamera();
        mCamera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mHolder.getSurface() == null || mCamera == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // start preview with new settings
        try {

            // set preview size and make any resize, rotate or
            // reformatting changes here
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(0, info);

            int orientation = getCorrectCameraOrientation(info, mCamera);
            mCamera.setDisplayOrientation(orientation);
            mCamera.getParameters().setRotation(orientation);

            // resize the image to aspect ratio
            Camera.Parameters params = mCamera.getParameters();
            Camera.Size previewSize  = mCamera.getParameters().getSupportedPreviewSizes().get(0);
            params.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(params);

            int size = Math.min(mDisplay.getHeight(), mDisplay.getWidth());
            double ratio = (double) previewSize.width / previewSize.height;
            mHolder.setFixedSize((int)(size * ratio), size);

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

    private int getCorrectCameraOrientation(Camera.CameraInfo info, Camera camera) {
        int rotation = mDisplay.getRotation(),

        degrees, result;

        switch (rotation) {
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
                break;
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
