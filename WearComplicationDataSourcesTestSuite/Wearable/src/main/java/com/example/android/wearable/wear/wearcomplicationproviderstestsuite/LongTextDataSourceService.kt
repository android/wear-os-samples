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
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * A complication provider that supports only [ComplicationType.LONG_TEXT] and cycles
 * through the possible configurations on tap.
 *
 * Note: This subclasses [SuspendingComplicationDataSourceService] instead of [ComplicationDataSourceService] to support
 * coroutines, so data operations (specifically, calls to [DataStore]) can be supported directly in the
 * [onComplicationRequest].
 *
 * If you don't perform any suspending operations to update your complications, you can subclass
 * [ComplicationDataSourceService] and override [onComplicationRequest] directly.
 * (see [NoDataDataSourceService] for an example)
 */
class LongTextDataSourceService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.LONG_TEXT) {
            return null
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complication = Complication.LONG_TEXT,
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
            case = Case.TEXT_WITH_ICON_AND_TITLE
        )

    private fun getComplicationData(
        tapAction: PendingIntent?,
        case: Case
    ): ComplicationData =
        when (case) {
            Case.TEXT_ONLY -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_only)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_only_content_description)
                ).build()
            )
            Case.TEXT_WITH_ICON -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_icon)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_icon_content_description)
                ).build()
            )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_face_vd_theme_24)
                    ).build()
                )
            // Unlike for short text complications, if the long title field is supplied then it
            // should always be displayed by the watch face. This means that when a long text
            // provider supplies both title and icon, it is expected that both are displayed.
            Case.TEXT_WITH_ICON_AND_TITLE -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_icon_and_title)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_icon_and_title_content_description)
                ).build()
            )
                .setTitle(
                    PlainComplicationText.Builder(
                        text = getText(R.string.long_title)
                    ).build()
                )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_battery)
                    )
                        .setAmbientImage(
                            ambientImage = Icon.createWithResource(this, R.drawable.ic_battery_burn_protect)
                        )
                        .build()
                )
            Case.TEXT_WITH_TITLE -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_title)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_title_content_description)
                ).build()
            )
                .setTitle(
                    PlainComplicationText.Builder(
                        text = getText(R.string.long_title)
                    ).build()
                )
            Case.TEXT_WITH_IMAGE -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_image)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_image_content_description)
                ).build()
            )
                .setSmallImage(
                    SmallImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.outdoors),
                        type = SmallImageType.PHOTO
                    ).build()
                )
            Case.TEXT_WITH_IMAGE_AND_TITLE -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_image_and_title)
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.long_text_with_image_and_title_content_description)
                ).build()
            )
                .setTitle(
                    PlainComplicationText.Builder(
                        text = getText(R.string.long_title)
                    ).build()
                )
                .setSmallImage(
                    SmallImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.aquarium),
                        type = SmallImageType.PHOTO
                    ).build()
                )
        }
            .setTapAction(tapAction)
            .build()

    private enum class Case {
        TEXT_ONLY,
        TEXT_WITH_ICON,
        TEXT_WITH_ICON_AND_TITLE,
        TEXT_WITH_TITLE,
        TEXT_WITH_IMAGE,
        TEXT_WITH_IMAGE_AND_TITLE
    }
}
