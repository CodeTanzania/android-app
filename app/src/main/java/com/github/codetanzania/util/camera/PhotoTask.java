package com.github.codetanzania.util.camera;

import android.os.Process;
import android.widget.FrameLayout;

public class PhotoTask extends CameraTask {

    final FrameLayout mCameraPreviewFrame;

    public PhotoTask(FrameLayout frameLayout) {
        super();
        this.mCameraPreviewFrame = frameLayout;
    }

    private void openCamera() {
        handleState(PhotoManager.OPEN_CAMERA);
    }

    private void takePicture() {
        handleState(PhotoManager.CAPTURE_PICTURE);
    }

    private void release() {
        handleState(PhotoManager.RELEASE_CAMERA);
        // kill the thread handling execution of this task if it's alive
        Thread thread = getCurrentThread();
        if (thread != null && thread.isAlive() && !thread.isAlive()) {
            thread.interrupt();
        }
    }

    private void startPreview() {
        handleState(PhotoManager.START_PREVIEW);
    }

    // passes the state to the PhotoManager
    void handleState(int state) {
        /*
         * Passes a handle to this task and the current state to the class that created
         * thread pool
         */
        getPhotoManager().handleState(this, state);
    }

    public StartCameraRunnable getStartCameraRunnable() {
        return new StartCameraRunnable();
    }

    public StopCameraRunnable getStopCameraRunnable() {
        return new StopCameraRunnable();
    }

    public CapturePictureRunnable getCapturePictureRunnable() {
        return new CapturePictureRunnable();
    }

    public StartCameraPreviewRunnable getStartCameraPreviewRunnable() {
        return new StartCameraPreviewRunnable();
    }

    final public class StartCameraPreviewRunnable implements Runnable {

        @Override
        public void run() {
            /*
             * Move the thread in the background to avoid resource competition
             * with UI-thread
             */
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            /*
             * Stores the current thread in the task
             */
            setCurrentThread(Thread.currentThread());

            /*
             * Now lets start preview
             */
            startPreview();
        }
    }

    // Runnable to start Camera
    final public class StartCameraRunnable implements Runnable {

        @Override
        public void run() {
            /*
             * Move the thread in the background to avoid resource competition
             * with UI-thread
             */
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            /*
             * Stores the current thread in the task
             */
            setCurrentThread(Thread.currentThread());

            /*
             * Now lets start camera
             */
            openCamera();
        }
    }

    // Runnable to stop and release camera
    public class StopCameraRunnable implements Runnable {

        @Override
        public void run() {
            /*
             * Move the thread in the background to avoid resource competition
             * with UI-thread
             */
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            /*
             * Stores the current thread in the task
             */
            setCurrentThread(Thread.currentThread());

            /*
             * Now lets release camera
             */
            release();
        }
    }

    // Runnable to capture picture
    public class CapturePictureRunnable implements Runnable {

        @Override
        public void run() {
            /*
             * Move the thread in the foreground. We want to capture as fast as possible
             * with UI-thread
             */
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);

            /*
             * Stores the current thread in the task
             */
            setCurrentThread(Thread.currentThread());

            /*
             * Now lets capture picture
             */
            takePicture();
        }
    }
}