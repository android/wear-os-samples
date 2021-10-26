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
import androidx.wear.watchface.complications.data.PhotoImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService

/**
 * A complication provider that supports only [ComplicationType.PHOTO_IMAGE] and cycles
 * between a couple of images on tap.
 *
 * Note: This subclasses [SuspendingComplicationDataSourceService] instead of [ComplicationDataSourceService] to support
 * coroutines, so data operations (specifically, calls to [DataStore]) can be supported directly in the
 * [onComplicationRequest].
 *
 * If you don't perform any suspending operations to update your complications, you can subclass
 * [ComplicationDataSourceService] and override [onComplicationRequest] directly.
 * (see [NoDataDataSourceService] for an example)
 */
class LargeImageDataSourceService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        if (request.complicationType != ComplicationType.PHOTO_IMAGE) {
            return null
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complication = Complication.LARGE_IMAGE,
            complicationInstanceId = request.complicationInstanceId
        )

        // On many watch faces a large image complication might not respond to taps as the
        // complication is used to provide the background for the watch. Providers should not rely
        // on tap functionality for large image complications, but the tap action is still included
        // here in case it is supported.
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
            case = Case.AQUARIUM
        )

    private fun getComplicationData(
        tapAction: PendingIntent?,
        case: Case
    ): ComplicationData =
        when (case) {
            Case.AQUARIUM -> PhotoImageComplicationData.Builder(
                photoImage = Icon.createWithResource(this, R.drawable.aquarium),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.photo_image_aquarium_content_description)
                ).build()
            )
            Case.OUTDOORS -> PhotoImageComplicationData.Builder(
                photoImage = Icon.createWithResource(this, R.drawable.outdoors),
                contentDescription = PlainComplicationText.Builder(
                    text = getText(R.string.photo_image_outdoors_content_description)
                ).build()
            )
        }
            .setTapAction(tapAction)
            .build()

    private enum class Case {
        AQUARIUM, OUTDOORS
    }
}
