package com.github.codetanzania.util;

import android.content.Context;
import android.content.Intent;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tz.co.codetanzania.R;

public class ImageUtils {

    /**
     * This method has a potential of causing OOM when a large photo is
     * loaded. Use a safer method {@code ImageUtils#resized} instead.
     *
     * @see ImageUtils#resized(Context, Uri, int, int)
     */
    @Deprecated
    // used by logcat
    private static final String TAG = "ImageUtils";

    public static final int DEFAULT_JPEG_COMPRESSION_QUALITY = 70;
    public static final int MAX_COMPRESSION_QUALITY = 100;
    public static final String TMP_PHOTO_DIR = "tmp_photos";
    public static final String IMAGE_TYPE_TOKEN_SEPARATOR = ":";

    private static final Pattern[] SUPPORTED_IMAGE_PATTERNS = {
            Pattern.compile("(?:image/jpeg|image/jpg)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("image/png", Pattern.CASE_INSENSITIVE)
    };

    // -- must match the SUPPORTED
    private static final Bitmap.CompressFormat[] SUPPORTED_IMAGE_COMPRESS_FORMAT = {
            Bitmap.CompressFormat.JPEG,
            Bitmap.CompressFormat.PNG
    };

    public static final int DEFAULT_MAX_BITMAP_WIDTH  = 480;
    public static final int DEFAULT_MAX_BITMAP_HEIGHT = 320;

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

    public static String encodeToBase64String(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static String encodeToBase64String(Context ctx, Uri photoUri, Bitmap.CompressFormat compressFormat, int quality) {
        Bitmap bitmap = resized(ctx, photoUri, DEFAULT_MAX_BITMAP_WIDTH, DEFAULT_MAX_BITMAP_HEIGHT);
        return encodeToBase64String(bitmap, compressFormat, quality);
    }

    public static Bitmap decodeFromBase64String(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static File createImageFile(Context ctx) throws IOException {
        String timestamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFile = "JPEG_" + timestamp + "_";
        // allow media scanner to index the captured issues
        File  storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFile, ".jpg", storageDir);
    }

    public static void indexPhoto(Context ctx, Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(uri.getPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
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

    public static File getTemporaryAlbumStorageDir(Context ctx, String albumName) {
        // Get the directory for the user's public pictures directory.
        ContextWrapper cw = new ContextWrapper(ctx);
        File file = cw.getDir(albumName, Context.MODE_PRIVATE);

        // automatically delete when app exits
        file.deleteOnExit();

        // debug
        Log.d(TAG, file.getPath());

        return file;
    }

    private static Bitmap.CompressFormat getCompressionFormat(String contentType) {
        for (int i = 0; i < SUPPORTED_IMAGE_PATTERNS.length; ++i) {
            Matcher matcher = SUPPORTED_IMAGE_PATTERNS[i].matcher(contentType);
            if (matcher.find()) {
                return SUPPORTED_IMAGE_COMPRESS_FORMAT[i];
            }
        }
        return null;
    }

    public static ArrayMap<String, String> compressAndCacheBitmaps(
            File album, ArrayMap<String, Bitmap> files, int quality) throws IOException {

        FileOutputStream out = null;
        ArrayMap<String, String> uris = new ArrayMap<>();

        try {
            File file;
            for (int i = 0; i < files.size(); i++) {

                String[] parts = files.keyAt(i).split(IMAGE_TYPE_TOKEN_SEPARATOR);

                file = new File(album, parts[0]);
                // debug
                Log.d(TAG, file.getPath());
                out  = new FileOutputStream(file);
                Bitmap.CompressFormat compressFormat = getCompressionFormat(parts[1]);

                if (compressFormat == null) {
                    /*
                     * ignore the formats which android does not recognize
                     */
                    continue;
                }

                // store file
                files.valueAt(i).compress(compressFormat, quality, out);
                uris.put(parts[0], Uri.fromFile(file).toString());
            }

        } catch(IOException ioException) {
            ioException.printStackTrace();
            throw new IOException(
                    "Error Caching Photo(s).\nOriginal Exception was:\n" + ioException.getMessage());
        } catch(ArrayIndexOutOfBoundsException aio) {
            aio.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioException) {
                    // ignore
                }
            }
        }

        // debug
        Log.d(TAG, String.format("%s", uris));

        return uris;
    }
}
