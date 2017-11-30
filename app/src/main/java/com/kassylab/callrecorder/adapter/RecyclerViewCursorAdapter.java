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
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Simplified CursorAdapter designed for RecyclerView.
 *
 * @author Christophe Beyls
 */
public abstract class RecyclerViewCursorAdapter<ViewHolder extends RecyclerViewCursorAdapter.ViewHolder>
		extends RecyclerView.Adapter<ViewHolder> {
	
	@Nullable
	private OnItemInteractionListener mListener;
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
	@SuppressWarnings("UnusedReturnValue")
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
	
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("WeakerAccess")
	public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		holder.bind(cursor);
	}
	
	@Nullable
	@SuppressWarnings("WeakerAccess")
	public OnItemInteractionListener getOnItemInteractionListener() {
		return mListener;
	}
	
	public void setOnItemInteractionListener(@Nullable OnItemInteractionListener mListener) {
		this.mListener = mListener;
	}
	
	
	public interface OnItemInteractionListener {
		void onItemSelected(Uri uri, int position);
	}
	
	public static abstract class ViewHolder extends RecyclerView.ViewHolder {
		
		@SuppressWarnings("WeakerAccess")
		public Uri itemUri;
		
		ViewHolder(View view) {
			super(view);
			itemView.setOnClickListener(v -> onTouch(itemUri, getAdapterPosition()));
		}
		
		protected abstract void bind(Cursor cursor);
		
		protected void onTouch(Uri itemUri, int position) {
		}
	}
}