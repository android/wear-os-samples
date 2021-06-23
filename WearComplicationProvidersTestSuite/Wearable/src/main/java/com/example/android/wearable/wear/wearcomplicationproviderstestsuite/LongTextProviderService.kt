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
 * A complication provider that supports only [ComplicationData.TYPE_LONG_TEXT] and cycles
 * through the possible configurations on tap.
 */
class LongTextProviderService : ComplicationProviderService() {
    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_LONG_TEXT) {
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
        when (state % 6) {
            0 -> data = ComplicationData.Builder(type)
                .setLongText(
                    ComplicationText.plainText(
                        getString(R.string.long_text_only)))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            1 -> data = ComplicationData.Builder(type)
                .setLongText(
                    ComplicationText.plainText(
                        getString(R.string.long_text_with_icon)))
                .setIcon(
                    Icon.createWithResource(
                        this, R.drawable.ic_face_vd_theme_24))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            2 ->                 // Unlike for short text complications, if the long title field is supplied then it
                // should always be displayed by the watch face. This means that when a long text
                // provider supplies both title and icon, it is expected that both are displayed.
                data = ComplicationData.Builder(type)
                    .setLongText(
                        ComplicationText.plainText(
                            getString(R.string.long_text_with_icon_and_title)))
                    .setLongTitle(
                        ComplicationText.plainText(getString(R.string.long_title)))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_battery))
                    .setBurnInProtectionIcon(
                        Icon.createWithResource(
                            this, R.drawable.ic_battery_burn_protect))
                    .setTapAction(complicationTogglePendingIntent)
                    .build()
            3 -> data = ComplicationData.Builder(type)
                .setLongText(
                    ComplicationText.plainText(
                        getString(R.string.long_text_with_title)))
                .setLongTitle(
                    ComplicationText.plainText(getString(R.string.long_title)))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            4 -> data = ComplicationData.Builder(type)
                .setLongText(
                    ComplicationText.plainText(
                        getString(R.string.long_text_with_image)))
                .setSmallImage(Icon.createWithResource(this, R.drawable.outdoors))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            5 -> data = ComplicationData.Builder(type)
                .setLongText(
                    ComplicationText.plainText(
                        getString(R.string.long_text_with_image_and_title)))
                .setLongTitle(
                    ComplicationText.plainText(getString(R.string.long_title)))
                .setSmallImage(Icon.createWithResource(this, R.drawable.aquarium))
                .setTapAction(complicationTogglePendingIntent)
                .build()
        }
        manager.updateComplicationData(complicationId, data)
    }
}