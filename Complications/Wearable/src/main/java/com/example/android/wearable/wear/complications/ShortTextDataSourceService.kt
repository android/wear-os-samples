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
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * A complication provider that supports only [ComplicationType.SHORT_TEXT] and cycles
 * through the possible configurations on tap.
 */
class ShortTextDataSourceService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.SHORT_TEXT) {
            return NoDataComplicationData()
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complication = Complication.SHORT_TEXT,
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
            case = Case.TEXT_WITH_ICON_AND_TITLE,
        )

    private fun getComplicationData(
        tapAction: PendingIntent?,
        case: Case,
    ): ComplicationData =
        when (case) {
            Case.TEXT_ONLY -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = safeText(R.string.short_text_only),
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = safeText(R.string.short_text_only_content_description),
                ).build(),
            )

            Case.TEXT_WITH_ICON -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(
                    text = safeText(R.string.short_text_with_icon),
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = safeText(R.string.short_text_with_icon_content_description),
                ).build(),
            )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_face_vd_theme_24),
                    ).build(),
                )
            x`
                Case.TEXT_WITH_TITLE -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(
                text = safeText(R.string.short_text_with_title),
            ).build(),
            contentDescription = PlainComplicationText.Builder(
                text = safeText(R.string.short_text_with_title_content_description),
            ).build(),
        )
            .setTitle(
                PlainComplicationText.Builder(
                    text = safeText(R.string.short_title),
                ).build(),
            )

            Case.TEXT_WITH_ICON_AND_TITLE -> ShortTextComplicationData.Builder(
                // When short text includes both short title and icon, the watch face should only
                // display one of those fields.
                text = PlainComplicationText.Builder(
                    text = safeText(R.string.short_text_with_both),
                ).build(),
                contentDescription = PlainComplicationText.Builder(
                    text = safeText(R.string.short_text_with_both_content_description),
                ).build(),
            )
                .setTitle(
                    PlainComplicationText.Builder(
                        text = safeText(R.string.short_title),
                    ).build(),
                )
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = Icon.createWithResource(this, R.drawable.ic_face_vd_theme_24),
                    ).build(),
                )
        }
            .setTapAction(tapAction)
            .build()

    private enum class Case {
        TEXT_ONLY,
        TEXT_WITH_ICON,
        TEXT_WITH_TITLE,
        TEXT_WITH_ICON_AND_TITLE,
    }

    private fun safeText(@StringRes resId: Int): String = getText(resId).toString()
}
