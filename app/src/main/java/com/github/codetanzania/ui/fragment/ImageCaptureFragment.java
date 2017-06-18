package com.github.codetanzania.ui.fragment;


import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.codetanzania.ui.activity.ReportIssueActivity;
import com.github.codetanzania.util.camera.PhotoManager;
import com.github.codetanzania.util.camera.PhotoTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import tz.co.codetanzania.R;

public class ImageCaptureFragment extends Fragment {

    public static final String KEY_CAPTURED_IMAGE = "CAPTURE_PATH";

    private String mCapturePath;

    // private CameraSurfaceView mCameraSurfaceView;
    private FrameLayout mCameraPreview;
    private FloatingActionButton mShutterButton;
    private EditText mEditText;
    private Button mPostIssueButton;

    // The callback to execute when photo is captured
    private PhotoManager.OnPhotoCapture cOnPhotoCapture;

    // The callback to execute when user whats to submit an issue
    private OnPostIssue cOnPostIssue;

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
        mEditText      = (EditText) view.findViewById(R.id.et_Msg);
        mPostIssueButton = (Button) view.findViewById(R.id.btn_OpenIssue);
        return view;
    }


    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            cOnPhotoCapture = (PhotoManager.OnPhotoCapture) ctx;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement PhotoManager.OnPhotoCapture",
                    ctx.getClass().getName()));
        }
        try {
            cOnPostIssue = (OnPostIssue) ctx;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s",
                    ctx.getClass().getName(), OnPostIssue.class.getName()));
        }
    }

    @Override public void onViewCreated(
        View view, Bundle savedInstanceState) {
        // mCameraSurfaceView = new CameraSurfaceView(getActivity());
        handleUIEvents();
    }

    @Override public void onResume() {
        super.onResume();

        // Check if user has passed captured image
        // mCapturePath = getArguments().getString(KEY_CAPTURED_IMAGE);

        // It can take a while to grab camera. Good idea to launch it
        // on a separate thread to avoid bogging down UI thread.
        PhotoManager.getInstance().startCameraRoutine(
                mCameraPreview, cOnPhotoCapture);
    }

    @Override public void onPause() {
        super.onPause();
        PhotoManager.getInstance().stopCameraRoutine();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // restore fragments state
            mCapturePath = savedInstanceState.getString(KEY_CAPTURED_IMAGE);
        }
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save fragment state
        outState.putString(KEY_CAPTURED_IMAGE, mCapturePath);
    }

    private void handleUIEvents() {
        // capture picture
        mShutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // didn't we start photo manager ?
                if (PhotoManager.getInstance().isPreviewActive()) {
                    // Take photo
                    PhotoManager.getInstance().capturePicture();
                } else {
                    // Restore preview
                    PhotoManager.getInstance().startPreview();
                }

                // update shutter accordingly
                mShutterButton.setImageResource(
                        PhotoManager.getInstance().isPreviewActive() ?
                                R.drawable.ic_close_white_24dp :
                                R.drawable.ic_add_a_photo_black_24dp
                );

            }
        });

        // when the keyboard focus changes
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // update action bar
                    ((ReportIssueActivity)getActivity()).forceRepaintActionBar();
                }
            }
        });

        // open issue when the button is clicked
        mPostIssueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = mEditText.getText();
                String   text = editable == null ? null : editable.toString();
                // hide the input method
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                // execute the callback to post an issue
                cOnPostIssue.doPost(text);
            }
        });
    }

    public interface OnPostIssue {
        void doPost(String text);
    }
}
