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
 * A complication provider that supports only {@link ComplicationData#TYPE_RANGED_VALUE} and cycles
 * through the possible configurations on tap. The value is randomised on each update.
 */
public class RangedValueProviderService extends ComplicationProviderService {

    private static final float[] MIN_VALUES = {0, -20, 57.5f, 10045};
    private static final float[] MAX_VALUES = {100, 20, 824.2f, 100000};

    @Override
    public void onComplicationUpdate(int complicationId, int type, ComplicationManager manager) {
        if (type != ComplicationData.TYPE_RANGED_VALUE) {
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

        int caseValue = state % 4;
        float minValue = MIN_VALUES[caseValue];
        float maxValue = MAX_VALUES[caseValue];
        float value = (float) Math.random() * (maxValue - minValue) + minValue;

        switch (caseValue) {
            case 0:
                data =
                        new ComplicationData.Builder(type)
                                .setMinValue(minValue)
                                .setMaxValue(maxValue)
                                .setValue(value)
                                .setShortText(
                                        ComplicationText.plainText(
                                                getString(R.string.short_text_only)))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 1:
                data =
                        new ComplicationData.Builder(type)
                                .setMinValue(minValue)
                                .setMaxValue(maxValue)
                                .setValue(value)
                                .setShortText(
                                        ComplicationText.plainText(
                                                getString(R.string.short_text_with_icon)))
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
                                .setMinValue(minValue)
                                .setMaxValue(maxValue)
                                .setValue(value)
                                .setShortText(
                                        ComplicationText.plainText(
                                                getString(R.string.short_text_with_title)))
                                .setShortTitle(
                                        ComplicationText.plainText(getString(R.string.short_title)))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
                break;
            case 3:
                data =
                        new ComplicationData.Builder(type)
                                .setMinValue(minValue)
                                .setMaxValue(maxValue)
                                .setValue(value)
                                .setIcon(
                                        Icon.createWithResource(
                                                this, R.drawable.ic_event_vd_theme_24))
                                .setTapAction(complicationTogglePendingIntent)
                                .build();
        }
        manager.updateComplicationData(complicationId, data);
    }
}