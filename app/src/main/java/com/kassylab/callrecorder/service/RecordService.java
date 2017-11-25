package com.kassylab.callrecorder.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.kassylab.callrecorder.Constants;
import com.kassylab.callrecorder.FileHelper;
import com.kassylab.callrecorder.R;
import com.kassylab.callrecorder.activity.CallListActivity;

import java.io.IOException;
import java.util.Date;

public class RecordService extends Service {

    public static final String EXTRA_COMMAND_TYPE = "commandType";
    public static final int EXTRA_COMMAND_TYPE_RECORDING_ENABLED = Constants.RECORDING_ENABLED;
    public static final int EXTRA_COMMAND_TYPE_RECORDING_DISABLED = Constants.RECORDING_DISABLED;
    public static final int EXTRA_COMMAND_TYPE_STATE_INCOMING_NUMBER = Constants.STATE_INCOMING_NUMBER;
    public static final int EXTRA_COMMAND_TYPE_STATE_CALL_START = Constants.STATE_CALL_START;
    public static final int EXTRA_COMMAND_TYPE_STATE_CALL_END = Constants.STATE_CALL_END;
    public static final int EXTRA_COMMAND_TYPE_STATE_START_RECORDING = Constants.STATE_START_RECORDING;
    public static final int EXTRA_COMMAND_TYPE_STATE_STOP_RECORDING = Constants.STATE_STOP_RECORDING;

    /**
     * The lookup key used with the {@link TelephonyManager#ACTION_PHONE_STATE_CHANGED} broadcast
     * for a String containing the new call state.
     * <p>
     * <p class="note">
     * Retrieve with
     * {@link android.content.Intent#getIntExtra(String, int)}.
     *
     * @see #EXTRA_STATE_IDLE
     * @see #EXTRA_STATE_RINGING
     * @see #EXTRA_STATE_OFFHOOK
     */
    public static final String EXTRA_STATE = TelephonyManager.EXTRA_STATE;

    /**
     * Value used with {@link #EXTRA_STATE} corresponding to
     * {@link TelephonyManager#CALL_STATE_IDLE}.
     */
    public static final int EXTRA_STATE_IDLE = TelephonyManager.CALL_STATE_IDLE;

    /**
     * Value used with {@link #EXTRA_STATE} corresponding to
     * {@link TelephonyManager#CALL_STATE_RINGING}.
     */
    public static final int EXTRA_STATE_RINGING = TelephonyManager.CALL_STATE_RINGING;

    /**
     * Value used with {@link #EXTRA_STATE} corresponding to
     * {@link TelephonyManager#CALL_STATE_OFFHOOK}.
     */
    public static final int EXTRA_STATE_OFFHOOK = TelephonyManager.CALL_STATE_OFFHOOK;

    public static final String EXTRA_PHONE_NUMBER = "phoneNumber";
    public static final String EXTRA_SILENT_MODE = "silentMode";
    public static final String TAG = Constants.TAG;


    private MediaRecorder recorder = null;
    private String phoneNumber = null;

    private String fileName;
    private boolean onCall = false;
    private boolean recording = false;
    private boolean onForeground = false;

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
                case EXTRA_STATE_RINGING:
                    break;
                case EXTRA_STATE_OFFHOOK:
                    Log.d(TAG, "RecordService STATE_CALL_START");
                    onCall = true;

                    if (phoneNumber == null) {
                        phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
                    }

                    if (phoneNumber != null && !recording) {
                        startService();
                        startRecording(intent);
                    }
                    break;
                case EXTRA_STATE_IDLE:
                    Log.d(TAG, "RecordService STATE_CALL_END");
                    onCall = false;
                    phoneNumber = null;
                    stopAndReleaseRecorder();
                    recording = false;
                    stopService();
                    break;
            }
        }

        /*if (intent != null) {
            int commandType = intent.getIntExtra(EXTRA_COMMAND_TYPE, 0);
            if (commandType != 0) {
                if (commandType == EXTRA_COMMAND_TYPE_RECORDING_ENABLED) {
                    Log.d(TAG, "RecordService RECORDING_ENABLED");
                    silentMode = intent.getBooleanExtra("silentMode", true);
                    if (!silentMode && phoneNumber != null && onCall
                            && !recording)
                        commandType = EXTRA_COMMAND_TYPE_STATE_START_RECORDING;

                } else if (commandType == EXTRA_COMMAND_TYPE_RECORDING_DISABLED) {
                    Log.d(TAG, "RecordService RECORDING_DISABLED");
                    silentMode = intent.getBooleanExtra(EXTRA_SILENT_MODE, true);
                    if (onCall && phoneNumber != null && recording)
                        commandType = EXTRA_COMMAND_TYPE_STATE_STOP_RECORDING;
                }

                switch (commandType) {
                    case EXTRA_COMMAND_TYPE_STATE_INCOMING_NUMBER:
                        Log.d(TAG, "RecordService STATE_INCOMING_NUMBER");
                        startService();
                        if (phoneNumber == null)
                            phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);

                        silentMode = intent.getBooleanExtra("silentMode", true);
                        break;
                    case EXTRA_COMMAND_TYPE_STATE_CALL_START:
                        Log.d(TAG, "RecordService STATE_CALL_START");
                        onCall = true;

                        if (!silentMode && phoneNumber != null && !recording) {
                            startService();
                            startRecording(intent);
                        }
                        break;
                    case EXTRA_COMMAND_TYPE_STATE_CALL_END:
                        Log.d(TAG, "RecordService STATE_CALL_END");
                        onCall = false;
                        phoneNumber = null;
                        stopAndReleaseRecorder();
                        recording = false;
                        stopService();
                        break;
                    case EXTRA_COMMAND_TYPE_STATE_START_RECORDING:
                        Log.d(TAG, "RecordService STATE_START_RECORDING");
                        if (!silentMode && phoneNumber != null && onCall) {
                            startService();
                            startRecording(intent);
                        }
                        break;
                    case EXTRA_COMMAND_TYPE_STATE_STOP_RECORDING:
                        Log.d(TAG, "RecordService STATE_STOP_RECORDING");
                        stopAndReleaseRecorder();
                        recording = false;
                        break;
                }
            }
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * in case it is impossible to record
     */
    private void terminateAndEraseFile() {
        Log.d(TAG, "RecordService terminateAndEraseFile");
        stopAndReleaseRecorder();
        recording = false;
        deleteFile();
    }

    private void stopService() {
        Log.d(TAG, "RecordService stopService");
        stopForeground(true);
        onForeground = false;
        this.stopSelf();
    }

    private void deleteFile() {
        Log.d(TAG, "RecordService deleteFile");
        FileHelper.deleteFile(fileName);
        fileName = null;
    }

    private void stopAndReleaseRecorder() {
        if (recorder == null)
            return;
        Log.d(TAG, "RecordService stopAndReleaseRecorder");
        boolean recorderStopped = false;
        boolean exception = false;

        try {
            recorder.stop();
            recorderStopped = true;
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException");
            e.printStackTrace();
            exception = true;
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException");
            exception = true;
        } catch (Exception e) {
            Log.e(TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }
        try {
            recorder.reset();
        } catch (Exception e) {
            Log.e(TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }
        try {
            recorder.release();
        } catch (Exception e) {
            Log.e(TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }

        recorder = null;
        if (exception) {
            deleteFile();
        }
        if (recorderStopped) {
            Toast toast = Toast.makeText(this,
                    this.getString(R.string.receiver_end_call),
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "RecordService onDestroy");
        stopAndReleaseRecorder();
        stopService();
        super.onDestroy();
    }

    private void startRecording(Intent intent) {
        Log.d(TAG, "RecordService startRecording");
        boolean exception = false;
        recorder = new MediaRecorder();

        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            fileName = getFilesDir().getAbsolutePath() + "/" + getFilename(phoneNumber);
            Log.d(TAG, fileName);
            recorder.setOutputFile(fileName);

            OnErrorListener errorListener = (arg0, arg1, arg2) -> {
                Log.e(TAG, "OnErrorListener " + arg1 + "," + arg2);
                terminateAndEraseFile();
            };
            recorder.setOnErrorListener(errorListener);

            OnInfoListener infoListener = (arg0, arg1, arg2) -> {
                Log.e(TAG, "OnInfoListener " + arg1 + "," + arg2);
                terminateAndEraseFile();
            };
            recorder.setOnInfoListener(infoListener);

            recorder.prepare();
            // Sometimes prepare takes some time to complete
            Thread.sleep(2000);
            recorder.start();
            recording = true;
            Log.d(TAG, "RecordService recorderStarted");
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException");
            e.printStackTrace();
            exception = true;
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            e.printStackTrace();
            exception = true;
        } catch (Exception e) {
            Log.e(TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }

        if (exception) {
            terminateAndEraseFile();
        }

        if (recording) {
            Toast toast = Toast.makeText(this,
                    this.getString(R.string.receiver_start_call),
                    Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this,
                    this.getString(R.string.record_impossible),
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private String getFilename(String phoneNumber) throws Exception {
        if (phoneNumber == null) {
            throw new Exception("Phone number can't be empty");
        }
        String date = (String) DateFormat.format("yyyyMMddkkmmss", new Date());

        phoneNumber = phoneNumber.replaceAll("[\\*\\+-]", "");
        if (phoneNumber.length() > 10) {
            phoneNumber = phoneNumber.substring(phoneNumber.length() - 10, phoneNumber.length());
        }

        return "d" + date + "p" + phoneNumber + ".3gp";
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

            Notification notification = new Notification.Builder(
                    getBaseContext())
                    .setContentTitle(
                            this.getString(R.string.notification_title))
                    .setTicker(this.getString(R.string.notification_ticker))
                    .setContentText(this.getString(R.string.notification_text))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent).setOngoing(true)
                    .build();

            notification.flags = Notification.FLAG_NO_CLEAR;

            startForeground(1337, notification);
            onForeground = true;
        }
    }
}