package com.github.codetanzania.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.codetanzania.util.ImageUtils;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import tz.co.codetanzania.R;

public class ImageAttachmentFragment extends Fragment {

    /* instance to the picture item */
    private static final String THUMBNAIL_DATA = "image.path";

    /* image view used to render captured image */
    private ImageView mPreviewItem;

    /* button to remove captured item */
    private ImageButton mBtnRemovePreviewItem;

    /* the reference will be initialized when the fragment is attached to the activity */
    private OnRemovePreviewItemClick mOnRemovePreviewItemClick;

    public static ImageAttachmentFragment getNewInstance(Bitmap thumbnail) {
        Bundle args = new Bundle();
        args.putByteArray(THUMBNAIL_DATA, ImageUtils.bitmapToByteArray(thumbnail));
        ImageAttachmentFragment frag = new ImageAttachmentFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override public void onAttach(Context ctx) {
        super.onAttach(ctx);
        try {
            mOnRemovePreviewItemClick = (OnRemovePreviewItemClick) ctx;
        } catch (ClassCastException cce) {
            throw new ClassCastException(String.format(Locale.getDefault(),
                    "%s must implement %s", getActivity().getClass().getName(),
                     OnRemovePreviewItemClick.class.getName()));
        }
    }

    /* invoked by android to create view for the fragment */
    @Override public View onCreateView(
            LayoutInflater inflater,
            ViewGroup group,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_image_attachment, group, false);
        mPreviewItem = (ImageView) view.findViewById(R.id.img_AttachmentPreview);
        mBtnRemovePreviewItem = (ImageButton) view.findViewById(R.id.btn_RemoveItem);
        return view;
    }

    /* the callback to execute when the view is created */
    @Override public void onViewCreated(View fragView, Bundle savedInstanceState) {
        renderImage();
        handleEvents();
    }

    /* render image item */
    private void renderImage() {
        byte[] bytes = getArguments().getByteArray(THUMBNAIL_DATA);
        mPreviewItem.setImageBitmap(ImageUtils.byteArrayToBitmap(bytes));
    }

    /* attach events */
    private void handleEvents() {
        /* when the remove item is clicked */
        mBtnRemovePreviewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnRemovePreviewItemClick.onRemovePreviewItemClicked();
            }
        });
    }

    /* respond to "remove item" events */
    public interface OnRemovePreviewItemClick {
        void onRemovePreviewItemClicked();
    }
}
