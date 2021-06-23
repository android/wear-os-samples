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
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complicationId = complicationId
        )
        val complicationTogglePendingIntent =
            ComplicationToggleReceiver.getComplicationToggleIntent(
                context = this,
                args = args
            )
        val state = args.getState(this)
        val data = ComplicationData.Builder(type)
            .setTapAction(complicationTogglePendingIntent)
            .apply {
                when (state % 4) {
                    0 -> {
                        setShortText(ComplicationText.plainText(getString(R.string.short_text_only)))
                    }
                    1 -> {
                        setShortText(ComplicationText.plainText(getString(R.string.short_text_with_icon)))
                        setIcon(
                            Icon.createWithResource(
                                this@ShortTextProviderService,
                                R.drawable.ic_face_vd_theme_24
                            )
                        )
                    }
                    2 -> {
                        setShortText(ComplicationText.plainText(getString(R.string.short_text_with_title)))
                        setShortTitle(ComplicationText.plainText(getString(R.string.short_title)))
                    }
                    3 -> {
                        // When short text includes both short title and icon, the watch face should only
                        // display one of those fields.
                        setShortText(ComplicationText.plainText(getString(R.string.short_text_with_both)))
                        setShortTitle(ComplicationText.plainText(getString(R.string.short_title)))
                        setIcon(
                            Icon.createWithResource(
                                this@ShortTextProviderService,
                                R.drawable.ic_face_vd_theme_24
                            )
                        )
                    }
                }
            }
            .build()
        manager.updateComplicationData(complicationId, data)
    }
}
