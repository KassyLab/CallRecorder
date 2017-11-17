package com.kassylab.callrecorder.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract between the {@link CallRecordProvider} and applications.
 * Contains definitions for the supported URIs and data columns.
 */

public class CallRecordContract {

    @SuppressWarnings("WeakerAccess")
    public static final String AUTHORITY = "com.kassylab.call_record";

    @SuppressWarnings("WeakerAccess")
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    static final String PATH_CALLS = "calls";
    static final String PATH_RECORDS = "records";

    private CallRecordContract() {
    }

    /**
     * Constants and helpers for the Call table, which contains details for individual call.
     */
    public static class Call implements BaseColumns {

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(CallRecordContract.CONTENT_URI, PATH_CALLS);

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of calls.
         */
        @SuppressWarnings("WeakerAccess")
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "." + PATH_CALLS;

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * call.
         */
        @SuppressWarnings("WeakerAccess")
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "." + PATH_CALLS;

        public static final String COLUMN_NUMBER = "number";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_RECORD = "record_id";
    }

    /**
     * Constants and helpers for the Record table, which contains details for individual record.
     */
    public static class Record implements BaseColumns {

        @SuppressWarnings("WeakerAccess, unused")
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(CallRecordContract.CONTENT_URI, PATH_RECORDS);

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of records.
         */
        @SuppressWarnings("WeakerAccess")
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "." + PATH_RECORDS;

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * record.
         */
        @SuppressWarnings("WeakerAccess")
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "." + PATH_RECORDS;

        public static final String COLUMN_FILE_URI = "file_uri";
        public static final String COLUMN_DURATION = "duration";
    }
}
