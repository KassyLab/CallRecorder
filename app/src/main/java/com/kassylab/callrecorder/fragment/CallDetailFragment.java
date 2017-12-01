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
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kassylab.callrecorder.R;
import com.kassylab.callrecorder.activity.CallDetailActivity;
import com.kassylab.callrecorder.activity.CallListActivity;
import com.kassylab.callrecorder.database.CallCursorWrapper;
import com.kassylab.callrecorder.database.RecordCursorWrapper;
import com.kassylab.callrecorder.provider.CallRecordContract;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A fragment representing a single Call detail screen.
 * This fragment is either contained in a {@link CallListActivity}
 * in two-pane mode (on tablets) or a {@link CallDetailActivity}
 * on handsets.
 */
public class CallDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
		SeekBar.OnSeekBarChangeListener, View.OnClickListener, MediaPlayer.OnCompletionListener {
	
	//TODO: release media player on destroy
	
	/**
	 * The fragment argument representing the item URI that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_URI =
			CallDetailFragment.class.getCanonicalName() + ".args.ITEM_URI";
	
	private static final int LOADER_CALL = 1;
	private static final int LOADER_RECORD = 2;
	
	private Uri mItemUri;
	private Uri mRecordUri;
	private long mDuration;
	
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
			
			currentDuration.setText(getDuration(progress * mDuration / 100));
		}
	};
	private TextView totalDuration;
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_ITEM_URI)) {
			mItemUri = savedInstanceState.getParcelable(ARG_ITEM_URI);
			
			/*Activity activity = this.getActivity();
			CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
			if (appBarLayout != null) {
				appBarLayout.setTitle(mItem.content);
			}*/
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
		
		mMediaPlayer.setOnCompletionListener(this);
		
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		seekBar.setOnSeekBarChangeListener(this);
		imageButton.setOnClickListener(this);
		currentDuration.setText(getDuration(0));
		
		getActivity().getSupportLoaderManager().initLoader(LOADER_CALL, null, this);
	}
	
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
	
	private void preparePlayer(String path) throws IOException {
		mMediaPlayer.reset();
		mMediaPlayer.setDataSource(path);
		mMediaPlayer.prepare();
		
		imageButton.setEnabled(true);
		totalDuration.setText(getDuration(mMediaPlayer.getDuration()));
		
		mPrepared = true;
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	
	}
	
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
	
	@Override
	public void onClick(View v) {
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
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		imageButton.setImageDrawable(
				getResources().getDrawable(R.drawable.ic_play_circle_filled_black_135dp));
		currentDuration.setText(getDuration(0));
		mPlaying = false;
	}
	
	private String getDuration(long milliseconds) {
		String finalTimerString = "";
		String secondsString;
		
		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}
		
		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}
		
		finalTimerString = finalTimerString + minutes + ":" + secondsString;
		
		// return timer string
		return finalTimerString;
	}
}
