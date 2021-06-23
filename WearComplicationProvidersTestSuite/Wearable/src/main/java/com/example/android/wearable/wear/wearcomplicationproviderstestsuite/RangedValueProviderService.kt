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
 * A complication provider that supports only [ComplicationData.TYPE_RANGED_VALUE] and cycles
 * through the possible configurations on tap. The value is randomised on each update.
 */
class RangedValueProviderService : ComplicationProviderService() {
    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_RANGED_VALUE) {
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
        val caseValue = state % 4
        val minValue = MIN_VALUES[caseValue]
        val maxValue = MAX_VALUES[caseValue]
        val value = Math.random().toFloat() * (maxValue - minValue) + minValue
        when (caseValue) {
            0 -> data = ComplicationData.Builder(type)
                .setMinValue(minValue)
                .setMaxValue(maxValue)
                .setValue(value)
                .setShortText(
                    ComplicationText.plainText(
                        getString(R.string.short_text_only)))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            1 -> data = ComplicationData.Builder(type)
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
                .build()
            2 -> data = ComplicationData.Builder(type)
                .setMinValue(minValue)
                .setMaxValue(maxValue)
                .setValue(value)
                .setShortText(
                    ComplicationText.plainText(
                        getString(R.string.short_text_with_title)))
                .setShortTitle(
                    ComplicationText.plainText(getString(R.string.short_title)))
                .setTapAction(complicationTogglePendingIntent)
                .build()
            3 -> data = ComplicationData.Builder(type)
                .setMinValue(minValue)
                .setMaxValue(maxValue)
                .setValue(value)
                .setIcon(
                    Icon.createWithResource(
                        this, R.drawable.ic_event_vd_theme_24))
                .setTapAction(complicationTogglePendingIntent)
                .build()
        }
        manager.updateComplicationData(complicationId, data)
    }

    companion object {
        private val MIN_VALUES = floatArrayOf(0f, -20f, 57.5f, 10045f)
        private val MAX_VALUES = floatArrayOf(100f, 20f, 824.2f, 100000f)
    }
}