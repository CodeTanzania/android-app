package com.github.codetanzania;

/**
 * Created by anon on 3/16/17.
 */

public final class Constants {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.github.codetanzania";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String CACHED_IMG_URI = PACKAGE_NAME + ".CACHED_IMG_URI";
    public static final String IMG_CACHE_DIR = ".tmp_cache_" + PACKAGE_NAME.replace(".", "_");
    public static final String KEY_DEFAULT_LANGUAGE = "default          _language";

    public static final class Const {
        public static final String APP_VERSION_CODE = "version_code";
        public static final String KEY_SHARED_PREFS = "shared_prefs";
        public static final String REPORTER_NAME    = "reporter.full_name";
        public static final String REPORTER_EMAIL   = "reporter.email";
        public static final String REPORTER_PHONE   = "reporter.phone";
        public static final String REPORTER_DAWASCO_ACCOUNT = "reporter.dawasco.account";
        public static final String AUTH_TOKEN = "auth.token";
        public static final String CURRENT_USER_ID = "user._id";

        public static final String TICKET = "app.ticket";
        public static final String ISSUE_COMMENTS = "app.ticket.comments";

        public static final String DATE_LAST_MODIFIED = "date_last_modified";
        public static final String SERVICE_LIST = "app.open311Services";
    }
}
