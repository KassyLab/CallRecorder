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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kassylab.callrecorder.DurationHelper;
import com.kassylab.callrecorder.R;
import com.kassylab.callrecorder.activity.CallDetailActivity;
import com.kassylab.callrecorder.activity.MainActivity;
import com.kassylab.callrecorder.database.CallCursorWrapper;
import com.kassylab.callrecorder.database.RecordCursorWrapper;
import com.kassylab.callrecorder.provider.CallRecordContract;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A fragment representing a single Call detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link CallDetailActivity}
 * on handsets.
 */
public class CallDetailFragment extends Fragment /*implements View.OnClickListener, MediaPlayer.OnCompletionListener*/ {
	
	/**
	 * The fragment argument representing the item URI that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_URI
			= CallDetailFragment.class.getCanonicalName() + ".args.ITEM_URI";
	
	private static final int LOADER_CALL = 1;
	private static final int LOADER_RECORD = 2;
	
	//region Item Fields
	private Uri mItemUri;
	private Uri mRecordUri;
	private long mDuration;
	private boolean mFavorite;
	//endregion
	
	//region UI Fields
	private Timer mTimer = new Timer();
	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private boolean mPrepared = false;
	private boolean mPlaying = false;
	private ProgressBar progressBar;
	private ImageButton imageButton;
	private SeekBar seekBar;
	private TextView currentDuration;
	private Runnable timerUpdater = new Runnable() {
		@Override
		public void run() {
			int progress = progressBar.getProgress() + 1;
			seekBar.setProgress(progress);
			
			if (progress == 100) {
				stopTimer();
				seekBar.setProgress(0);
			}
			
			currentDuration.setText(DurationHelper.getDuration(progress * mDuration / 100));
		}
	};
	private TextView totalDuration;
	private Menu mOptionMenu;
	private MenuItem favoriteDisableItem;
	private MenuItem favoriteEnableItem;
	//endregion
	
	private OnCallDetailInteractionListener mListener;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CallDetailFragment() {
	}
	
	public static Fragment newInstance(Uri itemUri) {
		Fragment fragment = new CallDetailFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_ITEM_URI, itemUri);
		fragment.setArguments(args);
		return fragment;
	}
	
	//region LifeCycle
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_ITEM_URI)) {
			mItemUri = savedInstanceState.getParcelable(ARG_ITEM_URI);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.call_detail, container, false);
		
		progressBar = rootView.findViewById(R.id.progressBar);
		imageButton = rootView.findViewById(R.id.imageButton);
		seekBar = rootView.findViewById(R.id.seekBar);
		currentDuration = rootView.findViewById(R.id.currentDuration);
		totalDuration = rootView.findViewById(R.id.totalDuration);
		
		mMediaPlayer.setOnCompletionListener(mp -> {
			imageButton.setImageDrawable(
					getResources().getDrawable(R.drawable.ic_play_circle_filled_black_135dp));
			currentDuration.setText(DurationHelper.getDuration(0));
			mPlaying = false;
		});
		
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
		imageButton.setOnClickListener(v -> {
			if (mPrepared) {
				if (!mPlaying) {
					mMediaPlayer.start();
					imageButton.setImageDrawable(
							getResources().getDrawable(R.drawable.ic_pause_circle_filled_black_135dp));
					mPlaying = true;
					
					startTimer();
				} else {
					mMediaPlayer.pause();
					imageButton.setImageDrawable(
							getResources().getDrawable(R.drawable.ic_play_circle_filled_black_135dp));
					mPlaying = false;
					
					stopTimer();
				}
			}
		});
		currentDuration.setText(DurationHelper.getDuration(0));
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getActivity().getSupportLoaderManager()
				.initLoader(LOADER_CALL, null, new LoaderCallbacks());
	}
	
	@Override
	public void onDestroyView() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		super.onDestroy();
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mListener = (OnCallDetailInteractionListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString()
					+ " must implement OnCallDetailInteractionListener");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	//endregion
	
	//region OptionsMenu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		mOptionMenu = menu;
		// Inflate the mOptionMenu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.call_detail, menu);
		
		favoriteDisableItem = mOptionMenu.findItem(R.id.action_favorite_disable);
		favoriteEnableItem = mOptionMenu.findItem(R.id.action_favorite_enable);
		
		updateFavorite();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean favorite = false;
		switch (item.getItemId()) {
			case R.id.action_favorite_enable:
				favorite = true;
			case R.id.action_favorite_disable:
				ContentValues values = new ContentValues();
				values.put(CallRecordContract.Call.COLUMN_FAVORITE, favorite);
				getContext().getContentResolver()
						.update(mItemUri, values, null, null);
				
				setFavorite(favorite);
				return true;
			case R.id.action_delete:
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}
	//endregion
	
	private void preparePlayer(String path) throws IOException {
		mMediaPlayer.reset();
		mMediaPlayer.setDataSource(path);
		mMediaPlayer.prepare();
		
		imageButton.setEnabled(true);
		totalDuration.setText(DurationHelper.getDuration(mMediaPlayer.getDuration()));
		
		mPrepared = true;
	}
	
	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}
	
	private void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(timerUpdater);
			}
		}, mDuration / 100, mDuration / 100);
	}
	
	public String getContactName(final String phoneNumber) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		
		String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
		
		String contactName = PhoneNumberUtils.formatNumber(phoneNumber);
		Cursor cursor = getContext().getContentResolver()
				.query(uri, projection, null, null, null);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				contactName = cursor.getString(0);
			}
			cursor.close();
		}
		
		return contactName;
	}
	
	private void setFavorite(boolean favorite) {
		mFavorite = favorite;
		updateFavorite();
	}
	
	private void updateFavorite() {
		if (mOptionMenu != null) {
			favoriteDisableItem.setVisible(mFavorite);
			favoriteEnableItem.setVisible(!mFavorite);
		}
	}
	
	
	public interface OnCallDetailInteractionListener {
		void setActionBarTitle(String title, String subtitle);
		void onDeleteCall(Uri itemUri);
	}
	
	private class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
		
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
				case LOADER_CALL:
					return new CursorLoader(
							getContext(),
							mItemUri,
							null,
							null,
							null,
							null
					);
				case LOADER_RECORD:
					return new CursorLoader(
							getContext(),
							mRecordUri,
							null,
							null,
							null,
							null
					);
				default:
					return null;
			}
			
		}
		
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			if (data == null || data.getCount() < 1) {
				return;
			}
			data.moveToFirst();
			ContentValues values;
			
			switch (loader.getId()) {
				case LOADER_CALL:
					values = new CallCursorWrapper(data).getContentValues();
					mRecordUri = ContentUris.withAppendedId(CallRecordContract.Record.CONTENT_URI,
							values.getAsLong(CallRecordContract.Call.COLUMN_RECORD));
					getActivity().getSupportLoaderManager()
							.initLoader(LOADER_RECORD, null, this);
					
					if (mListener != null) {
						mListener.setActionBarTitle(
								getContactName(values.getAsString(CallRecordContract.Call.COLUMN_NUMBER)),
								DateFormat.getDateTimeInstance().format(
										new Date(values.getAsLong(CallRecordContract.Call.COLUMN_DATE))));
						setFavorite(values.getAsBoolean(CallRecordContract.Call.COLUMN_FAVORITE));
					}
					break;
				case LOADER_RECORD:
					values = new RecordCursorWrapper(data).getContentValues();
					mDuration = values.getAsLong(CallRecordContract.Record.COLUMN_DURATION);
					try {
						preparePlayer(values.getAsString(CallRecordContract.Record.COLUMN_FILE_URI));
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
			}
		}
		
		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
		
		}
		
	}
	
	private class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			progressBar.setProgress(progress);
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			stopTimer();
		}
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mMediaPlayer.seekTo(seekBar.getProgress() * mMediaPlayer.getDuration() / 100);
			
			startTimer();
		}
		
	}
}
