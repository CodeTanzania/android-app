package com.github.codetanzania.util.camera;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.codetanzania.ui.view.CameraSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tz.co.codetanzania.R;

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

    private static final String TAG = "PhotoManager";

    static final int OPEN_CAMERA = 2;
    static final int STOP_PRIVIEW = 3;
    public static final int RELEASE_CAMERA = 4;
    static final int START_PREVIEW = 5;
    public static final int CAPTURE_PICTURE = 6;

    private final PhotoManagerHandler mHandler;
    private static PhotoManager self;

    private PhotoTask mPhotoTask;

    private OnPhotoCapture onPhotoCapture;

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
        mHandler = new PhotoManagerHandler(Looper.getMainLooper());
    }

    public boolean isPreviewActive() {
        return mHandler.isPreviewActive();
    }

    public void startPreview() {
        if (mPhotoTask != null) {
            Thread thread = new Thread(mPhotoTask.getStartCameraPreviewRunnable());
            thread.start();
        }
    }


    // Called by the Context Class to initialize camera
    public void startCameraRoutine(
            @NonNull FrameLayout previewFrame, @NonNull OnPhotoCapture onPhotoCapture) {
        mPhotoTask = new PhotoTask(previewFrame);
        this.onPhotoCapture = onPhotoCapture;
        Thread thread = new Thread(mPhotoTask.getStartCameraRunnable());
        thread.start();
    }

    // Called by the Context Class to stop camera
    public void stopCameraRoutine() {
        if (mPhotoTask != null) {
            Thread thread = new Thread(mPhotoTask.getStopCameraRunnable());
            thread.start();
        }
    }

    // Called to capture picture
    public void capturePicture() {
        if (mPhotoTask != null && mHandler.isPreviewActive()) {
            mPhotoTask.takePicture();
        }
    }

    public void handleState(CameraTask camTask, int state) {
        /*
         * Creates a message for the Handler with the state and the task object
         */
        Message msg = mHandler.obtainMessage(state, camTask);
        msg.sendToTarget();
    }

    // Android App Performance has two rules:
    // [1] -- do not block the UI-thread
    // [2] -- do not access the Android UI toolkit from outside the UI thread
    private final class PhotoManagerHandler extends Handler {

        // the flag to indicate if preview is active
        private volatile boolean mPreviewActive;

        // Name of the imageFile
        private String mCapturePath;

        // reference to the views
        private FrameLayout mPreviewFrame;
        private CameraSurfaceView mSurfaceView;

        // We're using deprecated API because we're also targeting lower android devices
        // (pre-lollipop devices)
        private Camera mCamera;

        // The Shutter Callback. used to play capture sound
        private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {

            private void playShootSound() {
                AudioManager manager = (AudioManager) mPreviewFrame.getContext()
                        .getSystemService(Context.AUDIO_SERVICE);
                int volume = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                if (volume != 0) {
                    MediaPlayer player = MediaPlayer.create(
                            mPreviewFrame.getContext(),
                            Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
                    if (player != null) {
                        player.start();
                    }
                }
            }

            @Override
            public void onShutter() {
                playShootSound();
            }
        };

        // The callback to execute when user captures a photo
        private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

            private boolean isExternalStorageWritable() {
                String state = Environment.getExternalStorageState();
                return (Environment.MEDIA_MOUNTED.equals(state));
            }

            private File getAlbumsStorageDir() {
                File file = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                return file;
            }

            private String getUniqueJPEGFileName() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());
                return String.format("%s.jpeg",sdf.format(new Date()));
            }

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (isExternalStorageWritable()) {
                    File pictureFile = new File(getAlbumsStorageDir(),
                            getUniqueJPEGFileName());
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.flush();
                        fos.close();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found exception: " + e.getMessage());
                    } catch (IOException e) {
                        Log.e(TAG, "Error accessing file: " + e.getMessage());
                    }

                    mCapturePath = pictureFile.getPath();
                    mCamera.stopPreview();
                    mPreviewActive = false;
                    onPhotoCapture.onPhotoCaptured(mCapturePath);
                } else {
                   Toast.makeText(mPreviewFrame.getContext(),
                           R.string.text_media_unmounted, Toast.LENGTH_SHORT).show();
                }
            }
        };

        public PhotoManagerHandler(Looper looper) {
            super(looper);
        }

        public boolean isPreviewActive() {
            return mPreviewActive;
        }

        public String getCapturePath() {
            return mCapturePath;
        }

        private synchronized boolean safeCameraOpen() {
            boolean qOpened = false;
            int numCameras = Camera.getNumberOfCameras(), i, id = -1;
            Camera.CameraInfo info;
            for (i = 0; i < numCameras; i++) {
                info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    id = i;
                    break;
                }
            }
            // did we get an id?
            if (id != -1) {
                try {
                    releaseCameraAndPreview();
                    mCamera = Camera.open(id);
                    qOpened = (mCamera != null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return qOpened;
        }

        private synchronized void takePicture() {
            if (mCamera != null) {
                // on some devices -- it seems we need to do this or we wont be able
                // to capture pictures
                mCamera.setPreviewCallback(null);
                mCamera.takePicture(mShutterCallback, null, null, mPictureCallback);
            }
        }

        private synchronized void startCameraPreview() {
            if (mCamera != null) {
                mCamera.startPreview();
                mPreviewActive = true;
            }
        }

        private synchronized void releaseCameraAndPreview() {
            if (mSurfaceView != null) {
                mSurfaceView.setCamera(null);
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }

        @Override public void handleMessage(Message msg) {
            PhotoTask task = (PhotoTask) msg.obj;
            switch (msg.what) {
                case OPEN_CAMERA:
                    if (safeCameraOpen()) {
                        mPreviewFrame = task.mCameraPreviewFrame;
                        mSurfaceView = new CameraSurfaceView(mPreviewFrame.getContext());
                        mPreviewFrame.addView(mSurfaceView);
                        // enable sound
                        mCamera.enableShutterSound(true);
                        mSurfaceView.setCamera(mCamera);
                        mPreviewActive = true;
                    }
                    break;
                case CAPTURE_PICTURE:
                    takePicture();
                    break;
                case START_PREVIEW:
                    startCameraPreview();
                    break;
                case RELEASE_CAMERA:
                    releaseCameraAndPreview();
                    break;
                default:
                    // pass along other messages
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public interface OnPhotoCapture {
        void onPhotoCaptured(String mCapturePath);
    }
}
