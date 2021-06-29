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

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import androidx.datastore.core.DataStore
import kotlin.random.Random

/**
 * A complication provider that supports only [ComplicationData.TYPE_RANGED_VALUE] and cycles
 * through the possible configurations on tap. The value is randomised on each update.
 *
 * Note: This subclasses [SuspendingComplicationProviderService] instead of [ComplicationProviderService] to support
 * coroutines, so data operations (specifically, calls to [DataStore]) can be supported directly in the
 * [onComplicationUpdate].
 * See [SuspendingComplicationProviderService] for the implementation details.
 *
 * If you don't perform any suspending operations to update your complications, you can subclass
 * [ComplicationProviderService] and override [onComplicationUpdate] directly.
 * (see [NoDataProviderService] for an example)
 */
class RangedValueProviderService : SuspendingComplicationProviderService() {
    override suspend fun onComplicationUpdateImpl(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_RANGED_VALUE) {
            manager.noUpdateRequired(complicationId)
            return
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complicationId = complicationId
        )
        val complicationTogglePendingIntent =
            ComplicationToggleReceiver.getComplicationToggleIntent(
                context = this,
                args = args
            )
        // Suspending function to retrieve the complication's state
        val state = args.getState(this)
        val caseValue = state.mod(4)
        val minValue = MIN_VALUES[caseValue]
        val maxValue = MAX_VALUES[caseValue]
        val value = Random.nextDouble(minValue.toDouble(), maxValue.toDouble()).toFloat()

        val data = ComplicationData.Builder(type)
            .setMinValue(minValue)
            .setMaxValue(maxValue)
            .setValue(value)
            .setTapAction(complicationTogglePendingIntent)
            .apply {
                when (caseValue) {
                    0 -> {
                        setShortText(ComplicationText.plainText(getString(R.string.short_text_only)))
                    }
                    1 -> {
                        setShortText(ComplicationText.plainText(getString(R.string.short_text_with_icon)))
                        setIcon(Icon.createWithResource(this@RangedValueProviderService, R.drawable.ic_battery))
                        setBurnInProtectionIcon(
                            Icon.createWithResource(
                                this@RangedValueProviderService,
                                R.drawable.ic_battery_burn_protect
                            )
                        )
                    }
                    2 -> {
                        setShortText(ComplicationText.plainText(getString(R.string.short_text_with_title)))
                        setShortTitle(ComplicationText.plainText(getString(R.string.short_title)))
                    }
                    3 -> {
                        setIcon(Icon.createWithResource(this@RangedValueProviderService, R.drawable.ic_event_vd_theme_24))
                    }
                }
            }
            .build()
        manager.updateComplicationData(complicationId, data)
    }

    companion object {
        private val MIN_VALUES = floatArrayOf(0f, -20f, 57.5f, 10045f)
        private val MAX_VALUES = floatArrayOf(100f, 20f, 824.2f, 100000f)
    }
}
