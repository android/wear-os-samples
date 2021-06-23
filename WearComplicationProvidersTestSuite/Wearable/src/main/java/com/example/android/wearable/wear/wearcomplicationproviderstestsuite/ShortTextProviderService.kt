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
import android.support.wearable.complications.ComplicationText

/**
 * A complication provider that supports only [ComplicationData.TYPE_SHORT_TEXT] and cycles
 * through the possible configurations on tap.
 */
class ShortTextProviderService : ComplicationProviderService() {
    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_SHORT_TEXT) {
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
        when (state % 4) {
            0 -> data = ComplicationData.Builder(type)
                .setShortText(
                    ComplicationText.plainText(
                        getString(R.string.short_text_only)))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            1 -> data = ComplicationData.Builder(type)
                .setShortText(
                    ComplicationText.plainText(
                        getString(R.string.short_text_with_icon)))
                .setIcon(
                    Icon.createWithResource(
                        this, R.drawable.ic_face_vd_theme_24))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            2 -> data = ComplicationData.Builder(type)
                .setShortText(
                    ComplicationText.plainText(
                        getString(R.string.short_text_with_title)))
                .setShortTitle(
                    ComplicationText.plainText(getString(R.string.short_title)))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            3 ->                 // When short text includes both short title and icon, the watch face should only
                // display one of those fields.
                data = ComplicationData.Builder(type)
                    .setShortText(
                        ComplicationText.plainText(
                            getString(R.string.short_text_with_both)))
                    .setShortTitle(
                        ComplicationText.plainText(getString(R.string.short_title)))
                    .setIcon(
                        Icon.createWithResource(
                            this, R.drawable.ic_face_vd_theme_24))
                    .setTapAction(complicationTogglePendingIntent)
                    .build()
        }
        manager.updateComplicationData(complicationId, data)
    }
}