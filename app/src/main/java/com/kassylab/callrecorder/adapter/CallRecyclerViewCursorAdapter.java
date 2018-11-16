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

package com.kassylab.callrecorder.adapter;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kassylab.callrecorder.DurationHelper;
import com.kassylab.callrecorder.R;
import com.kassylab.callrecorder.database.CallCursorWrapper;
import com.kassylab.callrecorder.database.RecordCursorWrapper;
import com.kassylab.callrecorder.provider.CallRecordContract;
import com.kassylab.recyclerviewcursoradapter.RecyclerViewCursorAdapter;

import java.text.DateFormat;
import java.util.Date;

/**
 * {@link RecyclerViewCursorAdapter} that can display an Item.
 */

public class CallRecyclerViewCursorAdapter
		extends RecyclerViewCursorAdapter<CallRecyclerViewCursorAdapter.ViewHolder> {
	
	public CallRecyclerViewCursorAdapter() {
		super();
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item_call, parent, false);
		return new ViewHolder(view);
	}
	
	class ViewHolder extends RecyclerViewCursorAdapter.ViewHolder {
		
		final TextView label;
		final TextView date;
		final TextView duration;
		//final ImageView type;
		
		ViewHolder(View view) {
			super(view);
			label = view.findViewById(R.id.label);
			date = view.findViewById(R.id.date);
			duration = view.findViewById(R.id.duration);
			//type = view.findViewById(R.id.type);
		}
		
		@SuppressLint("SetTextI18n")
		protected void bind(Cursor cursor) {
			ContentValues values = new CallCursorWrapper(cursor).getContentValues();
			
			long id = values.getAsLong(CallRecordContract.Call._ID);
			itemUri = ContentUris.withAppendedId(CallRecordContract.Call.CONTENT_URI, id);
			
			label.setText(getContactName(values.getAsString(CallRecordContract.Call.COLUMN_NUMBER),
					itemView.getContext()));
			
			date.setText(DateFormat.getDateTimeInstance().format(
					new Date(values.getAsLong(CallRecordContract.Call.COLUMN_DATE))));
			
			date.setCompoundDrawablesWithIntrinsicBounds(
					(values.getAsInteger(CallRecordContract.Call.COLUMN_TYPE) == 1)
							? R.drawable.ic_call_received : R.drawable.ic_call_made,
					0, 0, 0);
			
			Cursor record = itemView.getContext().getContentResolver().query(
					ContentUris.withAppendedId(CallRecordContract.Record.CONTENT_URI,
							values.getAsLong(CallRecordContract.Call.COLUMN_RECORD)),
					null,
					null,
					null,
					null);
			if (record != null) {
				if (record.moveToFirst()) {
					ContentValues recordValues = new RecordCursorWrapper(record).getContentValues();
					duration.setText(DurationHelper.getDuration(
							recordValues.getAsLong(CallRecordContract.Record.COLUMN_DURATION)));
				}
				record.close();
			} else {
				duration.setText("");
			}
		}
		
		@Override
		protected void onTouch(Uri itemUri, int position) {
			OnItemInteractionListener listener = getOnItemInteractionListener();
			if (listener != null) {
				listener.onItemSelected(itemUri, position);
			}
		}
		
		String getContactName(final String phoneNumber, Context context) {
			Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
			
			String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
			
			String contactName = phoneNumber;
			Cursor cursor = context.getContentResolver()
					.query(uri, projection, null, null, null);
			
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					contactName = cursor.getString(0);
				}
				cursor.close();
			}
			
			return contactName;
		}
	}
}
