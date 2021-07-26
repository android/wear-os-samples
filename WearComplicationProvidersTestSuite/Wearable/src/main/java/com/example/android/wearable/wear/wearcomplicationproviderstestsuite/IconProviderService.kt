/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import androidx.datastore.core.DataStore
import androidx.wear.complications.ComplicationProviderService
import androidx.wear.complications.ComplicationRequest
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.MonochromaticImageComplicationData
import androidx.wear.complications.data.PlainComplicationText

/**
 * A complication provider that supports only [ComplicationType.MONOCHROMATIC_IMAGE] and cycles through
 * a few different icons on each tap.
 *
 * Note: This subclasses [SuspendingComplicationProviderService] instead of [ComplicationProviderService] to support
 * coroutines, so data operations (specifically, calls to [DataStore]) can be supported directly in the
 * [onComplicationRequest].
 * See [SuspendingComplicationProviderService] for the implementation details.
 *
 * If you don't perform any suspending operations to update your complications, you can subclass
 * [ComplicationProviderService] and override [onComplicationRequest] directly.
 * (see [NoDataProviderService] for an example)
 */
class IconProviderService : SuspendingComplicationProviderService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.MONOCHROMATIC_IMAGE) {
            return null
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complication = Complication.ICON,
            complicationInstanceId = request.complicationInstanceId
        )
        val complicationTogglePendingIntent =
            ComplicationToggleReceiver.getComplicationToggleIntent(
                context = this,
                args = args
            )
        // Suspending function to retrieve the complication's state
        val state = args.getState(this@IconProviderService)
        val case = Case.values()[state.mod(Case.values().size)]
        return getComplicationData(
            tapAction = complicationTogglePendingIntent,
            case = case
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        getComplicationData(
            tapAction = null,
            case = Case.FACE,
        )

    private fun getComplicationData(
        tapAction: PendingIntent?,
        case: Case,
    ): ComplicationData =
        when (case) {
            Case.FACE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = MonochromaticImage.Builder(
                    Icon.createWithResource(this, R.drawable.ic_face_vd_theme_24)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.icon_face_content_description)
                ).build()
            )
            Case.BATTERY -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = MonochromaticImage.Builder(
                    Icon.createWithResource(this, R.drawable.ic_battery)
                )
                    .setAmbientImage(Icon.createWithResource(this, R.drawable.ic_battery_burn_protect))
                    .build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.icon_battery_content_description)
                ).build()
            )
            Case.EVENT -> MonochromaticImageComplicationData.Builder(
                monochromaticImage = MonochromaticImage.Builder(
                    Icon.createWithResource(this, R.drawable.ic_event_vd_theme_24)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.icon_event_content_description)
                ).build()
            )
        }
            .setTapAction(tapAction)
            .build()

    private enum class Case {
        FACE, BATTERY, EVENT
    }
}
