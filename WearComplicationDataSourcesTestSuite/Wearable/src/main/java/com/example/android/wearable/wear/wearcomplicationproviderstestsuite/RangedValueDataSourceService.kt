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
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlin.random.Random

/**
 * A complication provider that supports only [ComplicationType.RANGED_VALUE] and cycles
 * through the possible configurations on tap. The value is randomised on each update.
 *
 * Note: This subclasses [SuspendingComplicationDataSourceService] instead of [ComplicationDataSourceService] to support
 * coroutines, so data operations (specifically, calls to [DataStore]) can be supported directly in the
 * [onComplicationRequest].
 *
 * If you don't perform any suspending operations to update your complications, you can subclass
 * [ComplicationDataSourceService] and override [onComplicationRequest] directly.
 * (see [NoDataDataSourceService] for an example)
 */
class RangedValueDataSourceService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.RANGED_VALUE) {
            return null
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complication = Complication.RANGED_VALUE,
            complicationInstanceId = request.complicationInstanceId
        )
        val complicationTogglePendingIntent =
            ComplicationToggleReceiver.getComplicationToggleIntent(
                context = this,
                args = args
            )
        // Suspending function to retrieve the complication's state
        val state = args.getState(this)
        val case = Case.values()[state.mod(Case.values().size)]

        return getComplicationData(
            tapAction = complicationTogglePendingIntent,
            case = case
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        getComplicationData(
            tapAction = null,
            case = Case.TEXT_WITH_ICON
        )

    private fun getComplicationData(
        tapAction: PendingIntent?,
        case: Case
    ): ComplicationData {
        val text: ComplicationText?
        val monochromaticImage: MonochromaticImage?
        val title: ComplicationText?
        val caseContentDescription: String

        val minValue = case.minValue
        val maxValue = case.maxValue
        val value = Random.nextDouble(minValue.toDouble(), maxValue.toDouble()).toFloat()
        val percentage = (value - minValue) / (maxValue - minValue)

        when (case) {
            Case.TEXT_ONLY -> {
                text = PlainComplicationText.Builder(
                    text = getText(R.string.short_text_only)
                ).build()
                monochromaticImage = null
                title = null
                caseContentDescription = getString(R.string.ranged_value_text_only_content_description)
            }
            Case.TEXT_WITH_ICON -> {
                text = PlainComplicationText.Builder(
                    text = getText(R.string.short_text_with_icon)
                ).build()
                monochromaticImage = MonochromaticImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_battery)
                )
                    .setAmbientImage(
                        ambientImage = Icon.createWithResource(this, R.drawable.ic_battery_burn_protect)
                    )
                    .build()
                title = null
                caseContentDescription = getString(R.string.ranged_value_text_with_icon_content_description)
            }
            Case.TEXT_WITH_TITLE -> {
                text = PlainComplicationText.Builder(
                    text = getText(R.string.short_text_with_title)
                ).build()
                monochromaticImage = null
                title = PlainComplicationText.Builder(
                    text = getText(R.string.short_title)
                ).build()

                caseContentDescription = getString(R.string.ranged_value_text_with_title_content_description)
            }
            Case.ICON_ONLY -> {
                text = null
                monochromaticImage = MonochromaticImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_event_vd_theme_24)
                ).build()
                title = null
                caseContentDescription = getString(R.string.ranged_value_icon_only_content_description)
            }
        }

        // Create a content description that includes the value information
        val contentDescription = PlainComplicationText.Builder(
            text = getString(
                R.string.ranged_value_content_description,
                caseContentDescription,
                value,
                percentage,
                minValue,
                maxValue
            )
        )
            .build()

        return RangedValueComplicationData.Builder(
            value = value,
            min = minValue,
            max = maxValue,
            contentDescription = contentDescription
        )
            .setText(text)
            .setMonochromaticImage(monochromaticImage)
            .setTitle(title)
            .setTapAction(tapAction)
            .build()
    }

    private enum class Case(
        val minValue: Float,
        val maxValue: Float
    ) {
        TEXT_ONLY(0f, 100f),
        TEXT_WITH_ICON(-20f, 20f),
        TEXT_WITH_TITLE(57.5f, 824.2f),
        ICON_ONLY(10_045f, 100_000f);

        init {
            require(minValue < maxValue) { "Minimum value was greater than maximum value!" }
        }
    }
}
