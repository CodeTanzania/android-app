package com.github.codetanzania.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.github.codetanzania.api.model.Open311Service;
import com.github.codetanzania.ui.IssueCategoryPickerDialog;
import com.github.codetanzania.ui.activity.ReportIssueActivity;
import com.github.codetanzania.util.Open311ServicesUtil;

import java.util.ArrayList;
import java.util.List;

import tz.co.codetanzania.R;

public class IssueDetailsFormFragment extends Fragment implements
        PopupMenu.OnMenuItemClickListener,
        DialogInterface.OnClickListener {

    /* request code for accessing device camera */
    private static final int REQUEST_ACCESS_CAMERA = 1;

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

    /* Execute appropriate callback when user chooses the source where
     * (s)he get photo attachment
     */
    private OnStartPhotoActivityForResult mStartPhotoActivityForResult;

    /* the issue category dialog picker */
    private IssueCategoryPickerDialog mIssueCategoryDialogPicker;
    /* notifies the activity when user selects issue category */
    private IssueCategoryPickerDialog
            .OnSelectIssueCategory mOnSelectIssueCategory;

    /* get new instance */
    public static IssueDetailsFormFragment getNewInstance(String selectedService) {
        Bundle args = new Bundle();
        args.putString(KEY_SELECTED_SERVICE, selectedService);
        IssueDetailsFormFragment frag = new IssueDetailsFormFragment();
        frag.setArguments(args);
        return frag;
    }

    public void addPreviewImageFragment(Uri photoUri) {
        mPreviewImageFrag = ImageAttachmentFragment.getNewInstance(photoUri);
        FragmentManager fragManager  = getChildFragmentManager();
        FragmentTransaction ft       = fragManager.beginTransaction();
        ft.add(R.id.fr_Attachment, mPreviewImageFrag)
            .disallowAddToBackStack()
            .commitAllowingStateLoss();
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

    /* request permission to take photos */
    private void askForTakePhotoAndWriteToExternalStoragePermissions() {
        // first, we need to check if we've got permission to access camera and save pictures
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(R.string.title_take_photo_permission)
                    .setMessage(R.string.text_take_photo_permission)
                    .setCancelable(true)
                    .setNegativeButton(R.string.text_cancel, null)
                    .setPositiveButton(R.string.action_allow_take_photo, this);
            alertDialogBuilder.create().show();
        }

        // request permission
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_CAMERA);
    }

    private boolean hasTakePhotoAndWriteToExternalStoragePermissions() {
        return ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /* handle user events */
    private void handleUserEvents() {
        mBtnAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // choose where to fetch images,
                // IF user has granted us the permission to both write to storage device and access the camera device
                if (hasTakePhotoAndWriteToExternalStoragePermissions()) {
                    displaySelectImageSrcPopup(v);
                } else {
                    askForTakePhotoAndWriteToExternalStoragePermissions();
                }
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

        /* allow user to change issue category */
        mEtIssueTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get cached issues
                if (mIssueCategoryDialogPicker == null) {
                    List<Open311Service> serviceList = Open311ServicesUtil.cached(getActivity());
                    mIssueCategoryDialogPicker = new IssueCategoryPickerDialog((ArrayList<Open311Service>) serviceList, mOnSelectIssueCategory);
                }
                mIssueCategoryDialogPicker.show();
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

    /* the function to display a popup that allows user to select the src of image */
    private void displaySelectImageSrcPopup(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), anchorView);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_photo_item_src, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    /* bind data to the views */
    private void bindData() {
        String textData = getArguments().getString(KEY_SELECTED_SERVICE);
        mEtIssueTitle.setText(textData);
    }

    /* update service type */
    public void updateServiceType(@NonNull Open311Service open311Service) {
        mEtIssueTitle.setText(open311Service.name);
    }

    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            mOnSelectIssueCategory = (IssueCategoryPickerDialog.OnSelectIssueCategory)ctx;
            mStartPhotoActivityForResult = (OnStartPhotoActivityForResult) ctx;
        } catch (ClassCastException cce) {
            throw new ClassCastException(String.format(
                    "%s must implement %s and %s",
                    ctx.getClass().getName(),
                    IssueCategoryPickerDialog.class.getName(),
                    OnStartPhotoActivityForResult.class.getName()
            ));
        }
    }

    @Override public View onCreateView(
        LayoutInflater inflater,
        ViewGroup group,
        Bundle savedInstanceState ) {
        return inflater.inflate(R.layout.frag_issue_description_form, group, false);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_photo_item__src_media_store:
                mStartPhotoActivityForResult
                    .startPhotoMediaBrowserActivityForResult();
                break;
            case R.id.item_photo_item__src_camera:
                /* check if we've a permission to access the camera device */
                mStartPhotoActivityForResult
                        .startCameraActivityForResult();
                break;
        }
        return false;
    }

    // when activity result is received back
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int grantResults[]) {
        // confirm the result code
        if (requestCode == REQUEST_ACCESS_CAMERA && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displaySelectImageSrcPopup(mAttachmentPreview);
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // request permission
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ACCESS_CAMERA);
    }


    public interface OnStartPhotoActivityForResult {
        void startCameraActivityForResult();
        void startPhotoMediaBrowserActivityForResult();
    }
}
