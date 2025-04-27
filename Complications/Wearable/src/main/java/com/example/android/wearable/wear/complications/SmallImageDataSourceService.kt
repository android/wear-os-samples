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
package com.example.android.wearable.wear.complications

import android.app.PendingIntent
import android.content.ComponentName
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * A complication provider that supports only [ComplicationType.SMALL_IMAGE] and cycles
 * between the different image styles on tap.
 */
class SmallImageDataSourceService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SMALL_IMAGE) {
            return NoDataComplicationData()
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complication = Complication.SMALL_IMAGE,
            complicationInstanceId = request.complicationInstanceId,
        )
        val complicationTogglePendingIntent =
            ComplicationToggleReceiver.getComplicationToggleIntent(
                context = this,
                args = args,
            )
        // Suspending function to retrieve the complication's state
        val state = args.getState(this)
        val case = Case.entries[state.mod(Case.entries.size)]
        return getComplicationData(
            tapAction = complicationTogglePendingIntent,
            case = case,
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        getComplicationData(
            tapAction = null,
            case = Case.PHOTO,
        )

    private fun getComplicationData(
        tapAction: PendingIntent?,
        case: Case,
    ): ComplicationData =
        when (case) {
            Case.PHOTO -> SmallImageComplicationData.Builder(
                // An image using IMAGE_STYLE_PHOTO may be cropped to fill the space given to it.
                smallImage = SmallImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.aquarium),
                    type = SmallImageType.PHOTO,
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.small_image_photo_content_description),
                ).build(),
            )

            Case.ICON -> SmallImageComplicationData.Builder(
                // An image using IMAGE_STYLE_ICON must not be cropped, and should fit within the
                // space given to it.
                smallImage = SmallImage.Builder(
                    image = Icon.createWithResource(this, R.drawable.ic_launcher),
                    type = SmallImageType.ICON,
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.small_image_icon_content_description),
                ).build(),
            )
        }
            .setTapAction(tapAction)
            .build()

    /**
     * Different SmallImage styles supported for complications.
     */
    private enum class Case {
        PHOTO, // Cropped photo-style image
        ICON // Bounded icon-style image
    }
}
