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
import android.support.wearable.complications.ComplicationText;

/**
 * A complication provider that supports only {@link ComplicationData#TYPE_SHORT_TEXT} and cycles
 * through the possible configurations on tap.
 */
public class ShortTextProviderService extends ComplicationProviderService {

    @Override
    public void onComplicationUpdate(int complicationId, int type, ComplicationManager manager) {
        if (type != ComplicationData.TYPE_SHORT_TEXT) {
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
        switch (state % 4) {
            case 0:
                data =
                        new ComplicationData.Builder(type)
                                .setShortText(
                                        ComplicationText.plainText(
                                                getString(R.string.short_text_only)))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 1:
                data =
                        new ComplicationData.Builder(type)
                                .setShortText(
                                        ComplicationText.plainText(
                                                getString(R.string.short_text_with_icon)))
                                .setIcon(
                                        Icon.createWithResource(
                                                this, R.drawable.ic_face_vd_theme_24))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 2:
                data =
                        new ComplicationData.Builder(type)
                                .setShortText(
                                        ComplicationText.plainText(
                                                getString(R.string.short_text_with_title)))
                                .setShortTitle(
                                        ComplicationText.plainText(getString(R.string.short_title)))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 3:
                // When short text includes both short title and icon, the watch face should only
                // display one of those fields.
                data =
                        new ComplicationData.Builder(type)
                                .setShortText(
                                        ComplicationText.plainText(
                                                getString(R.string.short_text_with_both)))
                                .setShortTitle(
                                        ComplicationText.plainText(getString(R.string.short_title)))
                                .setIcon(
                                        Icon.createWithResource(
                                                this, R.drawable.ic_face_vd_theme_24))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
        }
        manager.updateComplicationData(complicationId, data);
    }
}