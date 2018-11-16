/*
 * Copyright (C) 2017  KassyLab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kassylab.callrecorder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kassylab.callrecorder.provider.CallRecordContract;

/**
 * A helper class to manage database creation and version management.
 */

public class CallRecorderDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CallRecord.db";
	private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_RECORD_TABLE =
            "CREATE TABLE records (" +
                    CallRecordContract.Record._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CallRecordContract.Record.COLUMN_FILE_URI + " TEXT, " +
                    CallRecordContract.Record.COLUMN_DURATION + " INTEGER)";
    private static final String SQL_CREATE_CALL_TABLE =
            "CREATE TABLE calls (" +
                    CallRecordContract.Call._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CallRecordContract.Call.COLUMN_NUMBER + " TEXT, " +
                    CallRecordContract.Call.COLUMN_TYPE + " INTEGER, " +
                    CallRecordContract.Call.COLUMN_DATE + " INTEGER, " +
                    CallRecordContract.Call.COLUMN_RECORD + " INTEGER, " +
		            CallRecordContract.Call.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0, " +
		            "FOREIGN KEY(" + CallRecordContract.Call.COLUMN_RECORD + ") " +
                    "REFERENCES record(" + CallRecordContract.Record._ID + ") ON DELETE CASCADE)";

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
