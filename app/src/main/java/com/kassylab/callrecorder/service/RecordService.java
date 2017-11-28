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

package com.kassylab.callrecorder.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.kassylab.callrecorder.Constants;
import com.kassylab.callrecorder.FileHelper;
import com.kassylab.callrecorder.R;
import com.kassylab.callrecorder.activity.CallListActivity;
import com.kassylab.callrecorder.provider.CallRecordContract;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class RecordService extends Service {

    /**
     * The lookup key used with the {@link TelephonyManager#ACTION_PHONE_STATE_CHANGED} broadcast
     * for a String containing the new call state.
     * <p>
     * <p class="note">
     * Retrieve with
     * {@link android.content.Intent#getIntExtra(String, int)}.
     *
     * @see #EXTRA_STATE_STOP
     * @see #EXTRA_STATE_PREPARE
     * @see #EXTRA_STATE_START
     */
    public static final String EXTRA_STATE = TelephonyManager.EXTRA_STATE;
    /**
     * Value used with {@link #EXTRA_STATE} corresponding to
     * {@link TelephonyManager#CALL_STATE_RINGING}.
     */
    public static final int EXTRA_STATE_PREPARE = TelephonyManager.CALL_STATE_RINGING;
    /**
     * Value used with {@link #EXTRA_STATE} corresponding to
     * {@link TelephonyManager#CALL_STATE_OFFHOOK}.
     */
    public static final int EXTRA_STATE_START = TelephonyManager.CALL_STATE_OFFHOOK;
    /**
     * Value used with {@link #EXTRA_STATE} corresponding to
     * {@link TelephonyManager#CALL_STATE_IDLE}.
     */
    public static final int EXTRA_STATE_STOP = TelephonyManager.CALL_STATE_IDLE;
    public static final String EXTRA_PHONE_NUMBER = "phoneNumber";
    private static final String TAG = Constants.TAG;
    private MediaRecorder recorder = null;
    private String phoneNumber = null;

    private String fileName;
    private boolean onForeground = false;

    private boolean prepared;
    private boolean recording = false;
    private Date dateStartRecording;
    private Date dateStopRecording;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "RecordService.onStartCommand(Intent, int, int)");

        if (intent != null) {
            switch (intent.getIntExtra(EXTRA_STATE, 0)) {
                case EXTRA_STATE_PREPARE:
                    Log.d(TAG, "RecordService STATE_PREPARE");

                    phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);

                    try {
                        prepareRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case EXTRA_STATE_START:
                    Log.d(TAG, "RecordService STATE_START");

                    if (phoneNumber == null) {
                        phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                    }

                    if (phoneNumber != null && !recording) {
                        startService();
                        startRecording();
                    }
                    break;
                case EXTRA_STATE_STOP:
                    Log.d(TAG, "RecordService STATE_STOP");

                    stopRecording();
                    stopService();

                    phoneNumber = null;
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "RecordService onDestroy");

        stopRecording();
        stopService();

        super.onDestroy();
    }

    private void startService() {
        if (!onForeground) {
            Log.d(TAG, "RecordService startService");
            Intent intent = new Intent(this, CallListActivity.class);
            // intent.setAction(Intent.ACTION_VIEW);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getBaseContext(), 0, intent, 0);

            Notification notification = new Notification.Builder(getBaseContext())
                    .setContentTitle(this.getString(R.string.notification_title))
                    .setTicker(this.getString(R.string.notification_ticker))
                    .setContentText(this.getString(R.string.notification_text))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent).setOngoing(true)
                    //.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_menu_camera, "test", pendingIntent).build())
                    .build();

            notification.flags = Notification.FLAG_NO_CLEAR;

            startForeground(1337, notification);
            onForeground = true;
        }
    }

    private void stopService() {
        Log.d(TAG, "RecordService stopService");
        stopForeground(true);
        onForeground = false;
        this.stopSelf();
    }

    private void prepareRecording() throws IllegalStateException, IOException {
        Log.d(TAG, "RecordService.prepareRecording()");

        recorder = new MediaRecorder();
        fileName = getFilesDir().getAbsolutePath() + File.separatorChar + FileHelper.getFileName(phoneNumber);
        Log.d(TAG, fileName);

        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);

        recorder.setOnErrorListener((arg0, arg1, arg2) -> {
            Log.e(TAG, "OnErrorListener " + arg1 + "," + arg2);
            terminateRecording();
        });

        recorder.setOnInfoListener((arg0, arg1, arg2) -> {
            Log.e(TAG, "OnInfoListener " + arg1 + "," + arg2);
            terminateRecording();
        });

        recorder.prepare();
        prepared = true;
    }

    private void startRecording() {
        Log.d(TAG, "RecordService.startRecording()");

        try {
            if (recorder == null || !prepared) {
                prepareRecording();
            }

            recorder.start();
            dateStartRecording = new Date();
            recording = true;

            Toast.makeText(this, getString(R.string.record_start),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(this, getString(R.string.record_impossible),
                    Toast.LENGTH_LONG).show();

            terminateRecording();
        }
    }

    private void stopRecording() {
        Log.d(TAG, "RecordService.stopRecording()");

        if (recorder == null || !recording) {
            return;
        }

        try {
            dateStopRecording = new Date();
            recorder.stop();
            recorder.reset();
            recorder.release();

            saveRecording();

            Toast toast = Toast.makeText(this,
                    this.getString(R.string.record_stop),
                    Toast.LENGTH_SHORT);
            toast.show();
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException");

            e.printStackTrace();
            deleteFile();
        }

        recorder = null;
    }

    /**
     * in case it is impossible to record
     */
    private void terminateRecording() {
        Log.d(TAG, "RecordService.terminateRecording()");

        stopRecording();
        recording = false;
        deleteFile();
    }

    private void saveRecording() {
        Log.d(TAG, "RecordService.saveRecording()");
        ContentValues recordValues = new ContentValues();

        recordValues.put(CallRecordContract.Record.COLUMN_FILE_URI, fileName);
        recordValues.put(CallRecordContract.Record.COLUMN_DURATION,
                dateStopRecording.getTime() - dateStartRecording.getTime());

        Uri record = getContentResolver().insert(CallRecordContract.Record.CONTENT_URI, recordValues);
        Log.d(TAG, "RecordService record created");

        if (record != null) {
            long recordId = ContentUris.parseId(record);
            ContentValues callValues = new ContentValues();

            callValues.put(CallRecordContract.Call.COLUMN_NUMBER, phoneNumber);
            callValues.put(CallRecordContract.Call.COLUMN_TYPE, 1);
            callValues.put(CallRecordContract.Call.COLUMN_DATE, dateStartRecording.getTime());
            callValues.put(CallRecordContract.Call.COLUMN_RECORD, recordId);

            getContentResolver().insert(CallRecordContract.Call.CONTENT_URI, callValues);
            Log.d(TAG, "RecordService call created");
        }
    }

    private void deleteFile() {
        Log.d(TAG, "RecordService.deleteFile()");

        FileHelper.deleteFile(fileName);

        fileName = null;
    }
}