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

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

/**
 * Simplified CursorAdapter designed for RecyclerView.
 *
 * @author Christophe Beyls
 */
public abstract class RecyclerViewCursorAdapter<ViewHolder extends RecyclerView.ViewHolder>
		extends RecyclerView.Adapter<ViewHolder> {
	
	private Cursor cursor;
	private int rowIDColumn = -1;
	
	RecyclerViewCursorAdapter() {
		setHasStableIds(true);
	}
	
	/**
	 * Swap in a new Cursor, returning the old Cursor.
	 * The old cursor is not closed.
	 *
	 * @param newCursor The new cursor to swap with the old
	 * @return The previously set Cursor, if any.
	 * If the given new Cursor is the same instance as the previously set
	 * Cursor, null is also returned.
	 */
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == cursor) {
			return null;
		}
		Cursor oldCursor = cursor;
		cursor = newCursor;
		rowIDColumn = (newCursor == null) ? -1 : newCursor.getColumnIndexOrThrow("_id");
		notifyDataSetChanged();
		return oldCursor;
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	@Override
	public int getItemCount() {
		return (cursor == null) ? 0 : cursor.getCount();
	}
	
	/**
	 * @param position the position of the item to get
	 * @return The cursor initialized to the specified position.
	 */
	public Object getItem(int position) {
		if (cursor != null) {
			cursor.moveToPosition(position);
		}
		return cursor;
	}
	
	@Override
	public long getItemId(int position) {
		if ((cursor != null) && cursor.moveToPosition(position)) {
			return cursor.getLong(rowIDColumn);
		}
		return RecyclerView.NO_ID;
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		if (cursor == null) {
			throw new IllegalStateException("this should only be called when the cursor is not null");
		}
		if (!cursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}
		onBindViewHolder(holder, cursor);
	}
	
	public abstract void onBindViewHolder(ViewHolder holder, Cursor cursor);
}