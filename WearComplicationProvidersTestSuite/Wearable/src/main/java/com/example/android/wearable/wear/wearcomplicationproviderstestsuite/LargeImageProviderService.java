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
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;

/**
 * A complication provider that supports only {@link ComplicationData#TYPE_LARGE_IMAGE} and cycles
 * between a couple of images on tap.
 */
public class LargeImageProviderService extends ComplicationProviderService {

    @Override
    public void onComplicationUpdate(int complicationId, int type, ComplicationManager manager) {
        if (type != ComplicationData.TYPE_LARGE_IMAGE) {
            manager.noUpdateRequired(complicationId);
            return;
        }

        ComponentName thisProvider = new ComponentName(this, getClass());

        // On many watch faces a large image complication might not respond to taps as the
        // complication is used to provide the background for the watch. Providers should not rely
        // on tap functionality for large image complications, but the tap action is still included
        // here in case it is supported.
        PendingIntent complicationTogglePendingIntent =
                ComplicationToggleReceiver.getToggleIntent(this, thisProvider, complicationId);

        SharedPreferences preferences =
                getSharedPreferences(ComplicationToggleReceiver.PREFERENCES_NAME, 0);
        int state =
                preferences.getInt(
                        ComplicationToggleReceiver.getPreferenceKey(thisProvider, complicationId),
                        0);

        ComplicationData data = null;
        switch (state % 2) {
            case 0:
                data =
                        new ComplicationData.Builder(type)
                                .setLargeImage(Icon.createWithResource(this, R.drawable.aquarium))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 1:
                data =
                        new ComplicationData.Builder(type)
                                .setLargeImage(Icon.createWithResource(this, R.drawable.outdoors))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
        }
        manager.updateComplicationData(complicationId, data);
    }
}