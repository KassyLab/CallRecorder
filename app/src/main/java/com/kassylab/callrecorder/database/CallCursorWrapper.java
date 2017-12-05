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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;

import com.kassylab.callrecorder.provider.CallRecordContract;

/**
 * A simple {@link CursorWrapper} subclass for {@link CallRecordContract.Call} cursor.
 */

public class CallCursorWrapper extends CursorWrapper {
	
	/**
	 * Creates a cursor wrapper.
	 *
	 * @param cursor The underlying cursor to wrap.
	 */
	public CallCursorWrapper(Cursor cursor) {
		super(cursor);
	}
	
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		
		int id = getColumnIndex(CallRecordContract.Call._ID);
		if (id != -1) values.put(CallRecordContract.Call._ID, getLong(id));
		
		id = getColumnIndex(CallRecordContract.Call.COLUMN_NUMBER);
		if (id != -1) values.put(CallRecordContract.Call.COLUMN_NUMBER, getString(id));
		
		id = getColumnIndex(CallRecordContract.Call.COLUMN_TYPE);
		if (id != -1) values.put(CallRecordContract.Call.COLUMN_TYPE, getInt(id));
		
		id = getColumnIndex(CallRecordContract.Call.COLUMN_DATE);
		if (id != -1) values.put(CallRecordContract.Call.COLUMN_DATE, getLong(id));
		
		id = getColumnIndex(CallRecordContract.Call.COLUMN_RECORD);
		if (id != -1) values.put(CallRecordContract.Call.COLUMN_RECORD, getLong(id));
		
		id = getColumnIndex(CallRecordContract.Call.COLUMN_FAVORITE);
		if (id != -1) values.put(CallRecordContract.Call.COLUMN_FAVORITE, getInt(id) == 1);
		
		return values;
	}
}
