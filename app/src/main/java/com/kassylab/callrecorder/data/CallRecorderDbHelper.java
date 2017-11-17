package com.kassylab.callrecorder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kassylab.callrecorder.provider.CallRecordContract;

/**
 * Created by damien on 16/11/2017.
 */

public class CallRecorderDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CallRecordContract.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_RECORD_TABLE =
            "CREATE TABLE records (" +
                    CallRecordContract.Record._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CallRecordContract.Record.COLUMN_FILE_URI + "TEXT, " +
                    CallRecordContract.Record.COLUMN_DURATION + " INTEGER)";
    private static final String SQL_CREATE_CALL_TABLE =
            "CREATE TABLE calls (" +
                    CallRecordContract.Call._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CallRecordContract.Call.COLUMN_NUMBER + "TEXT, " +
                    CallRecordContract.Call.COLUMN_TYPE + " INTEGER, " +
                    CallRecordContract.Call.COLUMN_DATE + " INTEGER, " +
                    CallRecordContract.Call.COLUMN_RECORD + " INTEGER, " +
                    " FOREIGN KEY (" + CallRecordContract.Call.COLUMN_RECORD + ") " +
                    "REFERENCES record (" + CallRecordContract.Record._ID + "))";

    private static final String SQL_DELETE_RECORD_TABLE = "DROP TABLE IF EXISTS records";
    private static final String SQL_DELETE_CALL_TABLE = "DROP TABLE IF EXISTS calls";

    public CallRecorderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RECORD_TABLE);
        db.execSQL(SQL_CREATE_CALL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_CALL_TABLE);
        db.execSQL(SQL_DELETE_RECORD_TABLE);

        onCreate(db);
    }
}
