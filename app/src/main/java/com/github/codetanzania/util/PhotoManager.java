package com.github.codetanzania.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.codetanzania.ui.view.CameraSurfaceView;

/**
 * PhotoManager does photo-related activities that takes longer to complete
 * and therefore, they would significantly degrade app performance by running
 * on the UI-thread.
 *
 * Here we do things such as requesting access to camera, decoding (byte[] -> Drawable),
 * resizing or compressing captured photos and saving them on device storage
 *
 * The manager maintain communication with the UI-components through messaging
 * protocol facilitated by Handler
 */
public class PhotoManager {

    public static final int CAMERA_READY = 0x200;
    public static final int OPEN_CAMERA  = 0x300;
    public static final int RELEASE_CAMERA = 0x400;
    public static final int CAMERA_ERROR   = 0x500;

    private final Handler mHandler;

    private static PhotoManager self;

    // singleton instance
    public static PhotoManager getInstance() {
        if (self == null) {
            self = new PhotoManager();
        }
        return self;
    }

    private PhotoManager() {
        // by instantiating Handler with Looper, we Connect it to the UI-thread.
        // In this way therefore, the handler runs on the same thread as the Looper
        this.mHandler = new Handler(Looper.getMainLooper()) {

            // release the camera
            private void releaseCamera(OpenCameraTask camTask) {
                if (camTask.mCamera != null) {
                    camTask.mCamera.release();
                    camTask.mCamera = null;
                }
            }

            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new message to process
             */
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RELEASE_CAMERA:
                        try {
                            OpenCameraTask camTask = (OpenCameraTask) msg.obj;
                            releaseCamera(camTask);
                        } catch (ClassCastException classCastException) {
                            break;
                        }
                        break;
                    case CAMERA_READY:
                        try {
                            // prepare the UI to start receiving frames
                            OpenCameraTask camTask = (OpenCameraTask) msg.obj;
                            CameraSurfaceView surfaceView = new CameraSurfaceView(
                                    camTask.mCameraPreviewFrame.getContext());
                            surfaceView.setCamera(camTask.mCamera);
                            camTask.mCameraPreviewFrame.addView(surfaceView);
                            break;
                        } catch (ClassCastException classCastException) {
                            break;
                        }
                    default:
                        /*
                         * Pass along other messages from the UI
                         */
                        super.handleMessage(msg);
                        break;
                }
            }
        };
    }

    public void handleState(OpenCameraTask camTask, int state) {
        switch(state) {
            case CAMERA_READY:
                /*
                 * Creates a message for the Handler with the state and the task object
                 */
                Message msg = mHandler.obtainMessage(state, camTask);
                msg.sendToTarget();
                break;
            default:
                break;
        }
    }
}
