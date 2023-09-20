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
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.WeightedElementsComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlin.random.Random

/**
 * A complication provider that supports only [ComplicationType.WEIGHTED_ELEMENTS] and cycles
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
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class WeightedElementsDataSourceService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.WEIGHTED_ELEMENTS) {
            return NoDataComplicationData()
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complication = Complication.WEIGHTED_ELEMENTS,
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

    override fun getPreviewData(type: ComplicationType): ComplicationData =
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

        // For the purposes of this example, limit the number of weighted elements to 4
        val numElements = 4
        val weighedElements = mutableListOf<WeightedElementsComplicationData.Element>()
        repeat(numElements) {
            weighedElements.add(
                WeightedElementsComplicationData.Element(
                    // Have each element in the list of weighted elements in the range 1 - 3.
                    weight = 2 * Random.nextFloat() + 1f,
                    color = Color.WHITE
                )
            )
        }

        when (case) {
            Case.TEXT_ONLY -> {
                text = PlainComplicationText.Builder(
                    text = getText(R.string.short_text_only)
                ).build()
                monochromaticImage = null
                title = null
                caseContentDescription = getString(
                    R.string.weighted_elements_text_only_content_description
                )
            }
            Case.TEXT_WITH_ICON -> {
                text = PlainComplicationText.Builder(
                    text = getText(R.string.weighted_elements_text_with_icon)
                ).build()
                monochromaticImage = MonochromaticImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_donut_large_24)
                )
                    .setAmbientImage(
                        ambientImage = Icon.createWithResource(
                            this,
                            R.drawable.ic_donut_large_24
                        )
                    )
                    .build()
                title = null
                caseContentDescription = getString(
                    R.string.weighted_elements_text_with_icon_content_description
                )
            }
            Case.TEXT_WITH_TITLE -> {
                text = PlainComplicationText.Builder(
                    text = getText(R.string.weighted_elements_text_with_title)
                ).build()
                monochromaticImage = null
                title = PlainComplicationText.Builder(
                    text = getText(R.string.weighted_elements_text_with_title)
                ).build()

                caseContentDescription = getString(
                    R.string.weighted_elements_text_with_title_content_description
                )
            }
            Case.ICON_ONLY -> {
                text = null
                monochromaticImage = MonochromaticImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_donut_large_24)
                ).build()
                title = null
                caseContentDescription = getString(
                    R.string.weighted_elements_icon_only_content_description
                )
            }
        }

        // Create a content description that includes the value information
        val contentDescription = PlainComplicationText.Builder(
            text = getString(
                R.string.weighted_elements_content_description,
                caseContentDescription,
                numElements
            )
        )
            .build()

        return WeightedElementsComplicationData.Builder(
            elements = weighedElements,
            contentDescription = contentDescription
        )
            .setText(text)
            .setMonochromaticImage(monochromaticImage)
            .setTitle(title)
            .setTapAction(tapAction)
            .build()
    }

    private enum class Case {
        TEXT_ONLY,
        TEXT_WITH_ICON,
        TEXT_WITH_TITLE,
        ICON_ONLY;
    }
}
