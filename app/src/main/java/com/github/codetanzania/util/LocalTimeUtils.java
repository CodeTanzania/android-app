package com.github.codetanzania.util;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalTimeUtils {

    private static final String FMT_FULL_DATE_TIME  = "yyyy-MM-dd HH:mm:ss";
    private static final String FMT_SHORT_DATE_TIME = "MMM dd HH:mm";

    private static String formatDate(
            @NonNull Date d, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        return sdf.format(d);
    }

    public static String formatShortDate(@NonNull Date date) {
        return formatDate(date, FMT_SHORT_DATE_TIME);
    }
}
