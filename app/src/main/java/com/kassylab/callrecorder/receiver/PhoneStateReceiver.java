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
                            RecordService.EXTRA_STATE_START);
                    break;
                case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    Log.d(TAG, "state: " + state);
                    phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Log.d(TAG, "phoneNumber: " + phoneNumber);

                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        serviceIntent.putExtra(RecordService.EXTRA_STATE,
                                RecordService.EXTRA_STATE_PREPARE);
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        serviceIntent.putExtra(RecordService.EXTRA_STATE,
                                RecordService.EXTRA_STATE_START);
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        serviceIntent.putExtra(RecordService.EXTRA_STATE,
                                RecordService.EXTRA_STATE_STOP);
                    }
                    break;
                default:
                    return;
            }
            serviceIntent.putExtra(RecordService.EXTRA_PHONE_NUMBER, phoneNumber);
            context.startService(serviceIntent);
        }
    }
}