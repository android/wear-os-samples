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
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

import android.app.PendingIntent
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService

/**
 * A complication provider that supports only [ComplicationData.TYPE_ICON] and cycles through
 * a few different icons on each tap.
 */
class IconProviderService : ComplicationProviderService() {
    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_ICON) {
            manager.noUpdateRequired(complicationId)
            return
        }
        val thisProvider = ComponentName(this, javaClass)
        val complicationTogglePendingIntent: PendingIntent =
            ComplicationToggleReceiver.Companion.getToggleIntent(this, thisProvider, complicationId)
        val preferences = getSharedPreferences(ComplicationToggleReceiver.Companion.PREFERENCES_NAME, 0)
        val state = preferences.getInt(
            ComplicationToggleReceiver.Companion.getPreferenceKey(thisProvider, complicationId),
            0)
        var data: ComplicationData? = null
        when (state % 3) {
            0 -> data = ComplicationData.Builder(type)
                .setIcon(
                    Icon.createWithResource(
                        this, R.drawable.ic_face_vd_theme_24))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            1 ->                 // This case includes a burn-in protection icon. If the screen uses burn-in
                // protection, that icon (which avoids solid blocks of color) should be shown in
                // ambient mode.
                data = ComplicationData.Builder(type)
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_battery))
                    .setBurnInProtectionIcon(
                        Icon.createWithResource(
                            this, R.drawable.ic_battery_burn_protect))
                    .setTapAction(complicationTogglePendingIntent)
                    .build()
            2 -> data = ComplicationData.Builder(type)
                .setIcon(
                    Icon.createWithResource(
                        this, R.drawable.ic_event_vd_theme_24))
                .setTapAction(complicationTogglePendingIntent)
                .build()
        }
        manager.updateComplicationData(complicationId, data)
    }
}