package com.github.codetanzania.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import tz.co.codetanzania.R;

public class ImageUtils {

    public static final int DEFAULT_MAX_BITMAP_WIDTH  = 1280;
    public static final int DEFAULT_MAX_BITMAP_HEIGHT = 960;

    public static Bitmap browseMediaStore(Context ctx, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException ignore) {
            Toast.makeText(ctx, ctx.getString(R.string.text_missing_resource), Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static Bitmap byteArrayToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static Bitmap resized(Context ctx, Uri uri, int width, int height) {
        try {
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds    = true;
            BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bitmapOptions);
            int photoWidth  = bitmapOptions.outWidth;
            int photoHeight = bitmapOptions.outHeight;

            int scaleFactor = Math.min(photoWidth/ width, photoHeight/height);
            bitmapOptions.inJustDecodeBounds = false;
            bitmapOptions.inSampleSize = scaleFactor;
            // TODO: find a way to replace inPurgeable because it is deprecated
            // bitmapOptions.inPurgeable = true;

            return BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bitmapOptions);
        } catch (FileNotFoundException nfException) {
            Toast.makeText(ctx, ctx.getString(R.string.text_missing_resource), Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
