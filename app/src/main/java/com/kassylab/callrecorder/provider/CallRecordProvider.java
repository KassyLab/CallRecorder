package com.kassylab.callrecorder.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kassylab.callrecorder.data.CallRecorderDbHelper;

public class CallRecordProvider extends ContentProvider {

    public static final String EQUALS = "=?";
    private static final String LOG_TAG = CallRecordProvider.class.getCanonicalName();
    private static final int CALLS = 1000;
    private static final int CALL_ID = 1001;
    private static final int RECORDS = 2000;
    private static final int RECORD_ID = 2001;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(CallRecordContract.AUTHORITY, CallRecordContract.PATH_CALLS, CALLS);
        uriMatcher.addURI(CallRecordContract.AUTHORITY, CallRecordContract.PATH_CALLS + "/#", CALL_ID);
        uriMatcher.addURI(CallRecordContract.AUTHORITY, CallRecordContract.PATH_RECORDS, RECORDS);
        uriMatcher.addURI(CallRecordContract.AUTHORITY, CallRecordContract.PATH_RECORDS + "/#", RECORD_ID);
    }

    private SQLiteOpenHelper sqLiteOpenHelper;

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CALLS:
                return CallRecordContract.Call.CONTENT_TYPE;
            case CALL_ID:
                return CallRecordContract.Call.CONTENT_ITEM_TYPE;
            case RECORDS:
                return CallRecordContract.Record.CONTENT_TYPE;
            case RECORD_ID:
                return CallRecordContract.Record.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();

        long id;
        switch (uriMatcher.match(uri)) {
            case CALLS:
                id = database.insert(CallRecordContract.PATH_CALLS, null, values);
                break;
            case RECORDS:
                id = database.insert(CallRecordContract.PATH_RECORDS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Insertion not allowed for " + uri);
        }

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public boolean onCreate() {
        sqLiteOpenHelper = new CallRecorderDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = sqLiteOpenHelper.getReadableDatabase();

        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case CALLS:
                cursor = database.query(CallRecordContract.PATH_CALLS, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CALL_ID:
                selection = CallRecordContract.Call._ID + EQUALS;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(CallRecordContract.PATH_CALLS, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case RECORDS:
                cursor = database.query(CallRecordContract.PATH_RECORDS, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case RECORD_ID:
                selection = CallRecordContract.Record._ID + EQUALS;
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(CallRecordContract.PATH_RECORDS, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
