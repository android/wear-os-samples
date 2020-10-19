/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.watchface.config;

import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import com.example.android.wearable.watchface.watchface.DigitalWatchFaceService;
import com.example.android.wearable.watchface.util.DigitalWatchFaceUtil;

/**
 * A {@link WearableListenerService} listening for {@link DigitalWatchFaceService} config messages
 * and updating the config {@link com.google.android.gms.wearable.DataItem} accordingly.
 */
public class DigitalWatchFaceConfigListenerService extends WearableListenerService {
    private static final String TAG = "DigitalListenerService";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onMessageReceived: " + messageEvent);
        }

        if (!messageEvent.getPath().equals(DigitalWatchFaceUtil.PATH_WITH_FEATURE)) {
            return;
        }
        byte[] rawData = messageEvent.getData();
        // It's allowed that the message carries only some of the keys used in the config DataItem
        // and skips the ones that we don't want to change.
        DataMap configKeysToOverwrite = DataMap.fromByteArray(rawData);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Received watch face config message: " + configKeysToOverwrite);
        }

        DigitalWatchFaceUtil.overwriteKeysInConfigDataMap(
                getApplicationContext(),
                configKeysToOverwrite,
                null);
    }
}
