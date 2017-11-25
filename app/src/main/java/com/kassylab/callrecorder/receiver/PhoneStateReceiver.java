package com.kassylab.callrecorder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.kassylab.callrecorder.Constants;
import com.kassylab.callrecorder.service.RecordService;

public class PhoneStateReceiver extends BroadcastReceiver {

    public static final String TAG = Constants.TAG;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action != null) {
            Log.d(TAG, "action: " + action);
            String phoneNumber;
            Intent serviceIntent = new Intent(context, RecordService.class);
            switch (action) {
                case Intent.ACTION_NEW_OUTGOING_CALL:
                    phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Log.d(TAG, "phoneNumber: " + phoneNumber);

                    serviceIntent.putExtra(RecordService.EXTRA_STATE,
                            RecordService.EXTRA_STATE_OFFHOOK);
                    break;
                case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    Log.d(TAG, "state: " + state);
                    phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Log.d(TAG, "phoneNumber: " + phoneNumber);

                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        serviceIntent.putExtra(RecordService.EXTRA_STATE,
                                RecordService.EXTRA_STATE_RINGING);
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        serviceIntent.putExtra(RecordService.EXTRA_STATE,
                                RecordService.EXTRA_STATE_OFFHOOK);
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        serviceIntent.putExtra(RecordService.EXTRA_STATE,
                                RecordService.EXTRA_STATE_IDLE);
                    }
                    break;
                default:
                    return;
            }
            serviceIntent.putExtra(RecordService.EXTRA_PHONE_NUMBER, phoneNumber);
            context.startService(serviceIntent);
        }

        /*String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        Log.d(Constants.TAG, "MyPhoneReciever phoneNumber " + phoneNumber);

        if (CallListActivity.updateExternalStorageState() == Constants.MEDIA_MOUNTED) {
            try {
                SharedPreferences settings = context.getSharedPreferences(
                        Constants.LISTEN_ENABLED, 0);

                boolean silent = settings.getBoolean("silentMode", true);

                if (extraState != null) {
                    Intent myIntent = new Intent(context, RecordService.class);

                    if (extraState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        myIntent.putExtra(RecordService.EXTRA_COMMAND_TYPE,
                                RecordService.EXTRA_COMMAND_TYPE_STATE_CALL_START);
                    } else if (extraState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        myIntent.putExtra(RecordService.EXTRA_COMMAND_TYPE,
                                RecordService.EXTRA_COMMAND_TYPE_STATE_CALL_END);
                    } else if (extraState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        if (phoneNumber == null) {
                            phoneNumber = intent
                                    .getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        }
                        myIntent.putExtra(RecordService.EXTRA_COMMAND_TYPE,
                                RecordService.EXTRA_COMMAND_TYPE_STATE_INCOMING_NUMBER);
                        myIntent.putExtra(RecordService.EXTRA_PHONE_NUMBER, phoneNumber);
                        myIntent.putExtra(RecordService.EXTRA_SILENT_MODE, silent);
                    }

                    context.startService(myIntent);

                } else if (phoneNumber != null) {
                    Intent myIntent = new Intent(context, RecordService.class);
                    myIntent.putExtra(RecordService.EXTRA_COMMAND_TYPE,
                            RecordService.EXTRA_COMMAND_TYPE_STATE_INCOMING_NUMBER);
                    myIntent.putExtra(RecordService.EXTRA_PHONE_NUMBER, phoneNumber);
                    myIntent.putExtra(RecordService.EXTRA_SILENT_MODE, silent);
                    context.startService(myIntent);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "Exception");
                e.printStackTrace();
            }
        }*/
    }
}