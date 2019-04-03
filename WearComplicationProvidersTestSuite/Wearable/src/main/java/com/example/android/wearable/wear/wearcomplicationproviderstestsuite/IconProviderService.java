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
 * A complication provider that supports only {@link ComplicationData#TYPE_ICON} and cycles through
 * a few different icons on each tap.
 */
public class IconProviderService extends ComplicationProviderService {

    @Override
    public void onComplicationUpdate(int complicationId, int type, ComplicationManager manager) {
        if (type != ComplicationData.TYPE_ICON) {
            manager.noUpdateRequired(complicationId);
            return;
        }

        ComponentName thisProvider = new ComponentName(this, getClass());
        PendingIntent complicationTogglePendingIntent =
                ComplicationToggleReceiver.getToggleIntent(this, thisProvider, complicationId);

        SharedPreferences preferences =
                getSharedPreferences(ComplicationToggleReceiver.PREFERENCES_NAME, 0);

        int state =
                preferences.getInt(
                        ComplicationToggleReceiver.getPreferenceKey(thisProvider, complicationId),
                        0);

        ComplicationData data = null;
        switch (state % 3) {
            case 0:
                data =
                        new ComplicationData.Builder(type)
                                .setIcon(
                                        Icon.createWithResource(
                                                this, R.drawable.ic_face_vd_theme_24))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 1:
                // This case includes a burn-in protection icon. If the screen uses burn-in
                // protection, that icon (which avoids solid blocks of color) should be shown in
                // ambient mode.
                data =
                        new ComplicationData.Builder(type)
                                .setIcon(Icon.createWithResource(this, R.drawable.ic_battery))
                                .setBurnInProtectionIcon(
                                        Icon.createWithResource(
                                                this, R.drawable.ic_battery_burn_protect))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 2:
                data =
                        new ComplicationData.Builder(type)
                                .setIcon(
                                        Icon.createWithResource(
                                                this, R.drawable.ic_event_vd_theme_24))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
        }
        manager.updateComplicationData(complicationId, data);
    }
}