package com.github.codetanzania.ui.fragment;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.codetanzania.ui.activity.ReportIssueActivity;

import tz.co.codetanzania.R;

public class IssueDetails2Fragment extends Fragment {

    /* qualifier key for the selected service */
    private static final String KEY_SELECTED_SERVICE = "selected_service";

    /* reference to the views */
    private ImageButton mBtnAddAttachment;
    private EditText mEtIssueTitle;
    private EditText mEtIssueDescription;
    private View     mAttachmentPreview;
    private Button   mBtnSubmitIssue;

    /* the fragment used to preview images before uploading to the server */
    private ImageAttachmentFragment mPreviewImageFrag;

    /* the flag helps to manage the UI state depending on whether there is an attachment or not */
    private boolean attachmentVisible;

    /* get new instance */
    public static IssueDetails2Fragment getNewInstance(String selectedService) {
        Bundle args = new Bundle();
        args.putString(KEY_SELECTED_SERVICE, selectedService);
        IssueDetails2Fragment frag = new IssueDetails2Fragment();
        frag.setArguments(args);
        return frag;
    }

    public void addPreviewImageFragment(Bitmap bitmap) {
        mPreviewImageFrag = ImageAttachmentFragment.getNewInstance(bitmap);
        FragmentManager fragManager  = getChildFragmentManager();
        FragmentTransaction ft       = fragManager.beginTransaction();
        ft.add(R.id.fr_Attachment, mPreviewImageFrag).disallowAddToBackStack().commitAllowingStateLoss();
        mAttachmentPreview.setVisibility(View.VISIBLE);
        mAttachmentPreview.setAlpha(0.0f);
        mAttachmentPreview.animate().alpha(1.0f);
    }

    public void removePreviewImageFragment() {
        if (mPreviewImageFrag != null) {
            FragmentManager fragManager = getChildFragmentManager();
            fragManager.beginTransaction().remove(mPreviewImageFrag).commit();
            mPreviewImageFrag = null;
            mAttachmentPreview.setVisibility(View.GONE);
        }
    }

    /* handle user events */
    private void handleUserEvents() {
        mBtnAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // choose where to fetch images from
                displaySelectImageSrcDialog();
            }
        });

        /* handle proper IME scanning and positioning */
        mEtIssueDescription.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEtIssueDescription.requestFocus();
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
                return false;
            }
        });

        /* when the submit issue button is clicked */
        mBtnSubmitIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEtIssueDescription.getText().toString();
                ((ReportIssueActivity)getActivity()).doPost(text);
            }
        });
    }

    /* the function to display the dialog that allows user to select the src of image */
    private void displaySelectImageSrcDialog() {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_image_src_selector_content, null);
        alertBuilder.setView(view)
            .setNegativeButton(R.string.text_cancel, null)
            .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleDynamicUserEvents((RadioGroup)view.findViewById(R.id.rg_ImageSourceGroupOptions));
                    }
                });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /* handle user events on dynamically generated events */
    private void handleDynamicUserEvents(final RadioGroup view) {
        int checkedId = view.getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radioBtn_BrowseOption:
                ((ReportIssueActivity)getActivity()).dispatchBrowseMediaStoreIntent();
                break;
            case R.id.radioBtn_StartCameraOption:
                ((ReportIssueActivity)getActivity()).dispatchTakePictureIntent();
                break;
        }
    }

    /* bind data to the views */
    private void bindData() {
        String textData = getArguments().getString(KEY_SELECTED_SERVICE);
        mEtIssueTitle.setText(textData);
    }

    @Override public View onCreateView(
        LayoutInflater inflater,
        ViewGroup group,
        Bundle savedInstanceState ) {
        return inflater.inflate(R.layout.frag_issue_description_alt, group, false);
    }

    @Override public void onViewCreated (
        View view,
        Bundle savedInstanceState ) {
        mBtnAddAttachment = (ImageButton) view.findViewById(R.id.btn_addImage);
        mEtIssueTitle = (EditText) view.findViewById(R.id.et_IssueTitle);
        mEtIssueDescription = (EditText) view.findViewById(R.id.et_IssueDescription);
        mAttachmentPreview = view.findViewById(R.id.fr_Attachment);
        mBtnSubmitIssue = (Button) view.findViewById(R.id.btn_SubmitIssue);
        mEtIssueDescription.requestFocus();
        bindData();
        handleUserEvents();
    }
}