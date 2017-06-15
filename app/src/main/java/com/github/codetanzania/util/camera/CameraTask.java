package com.github.codetanzania.util.camera;

public abstract class CameraTask {

    private final PhotoManager sPhotoManager;

    private Thread mCurrentThread;

    CameraTask() {
        this.sPhotoManager = PhotoManager.getInstance();
    }

    public PhotoManager getPhotoManager() {
        return sPhotoManager;
    }

    // Enables the task to send back response to the UI-thread
    abstract void handleState(int state);

    public void setCurrentThread(Thread thread) {
        mCurrentThread = thread;
    }

    protected Thread getCurrentThread() {
        return mCurrentThread;
    }
}
