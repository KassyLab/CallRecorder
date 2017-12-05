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

package com.kassylab.callrecorder.fragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kassylab.callrecorder.R;
import com.kassylab.callrecorder.adapter.CallRecyclerViewCursorAdapter;
import com.kassylab.callrecorder.adapter.RecyclerViewCursorAdapter;
import com.kassylab.callrecorder.provider.CallRecordContract;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnCallSelectedListener}
 * interface.
 */
public class CallListFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor>, RecyclerViewCursorAdapter.OnItemInteractionListener {
	
	private OnCallSelectedListener mListener;
	private CallRecyclerViewCursorAdapter mAdapter;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CallListFragment() {
	}
	
	public static CallListFragment newInstance() {
		return new CallListFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_call_list, container, false);
		
		// Set the adapter
		if (view instanceof RecyclerView) {
			RecyclerView recyclerView = (RecyclerView) view;
			recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
			mAdapter = new CallRecyclerViewCursorAdapter();
			mAdapter.setOnItemInteractionListener(this);
			recyclerView.setAdapter(mAdapter);
		}
		return view;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnCallSelectedListener) {
			mListener = (OnCallSelectedListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnContactSelectedListener");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
				getContext(),
				CallRecordContract.Call.CONTENT_URI,
				null,
				null,
				null,
				CallRecordContract.Call.COLUMN_DATE + " DESC"
		);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		Log.d("TEST", mAdapter.getItemCount() + "");
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	@Override
	public void onItemSelected(Uri uri, int position) {
		if (mListener != null) {
			mListener.onCallSelected(uri, position);
		}
	}
	
	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnCallSelectedListener {
		void onCallSelected(Uri uri, int position);
	}
}