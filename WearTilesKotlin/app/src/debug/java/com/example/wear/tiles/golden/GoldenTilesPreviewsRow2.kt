/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.wear.tiles.golden

import android.content.Context
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesPreviews
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

/** b/238548541 (internal bug - the spacing doesn't match Figma) */
@MultiRoundDevicesPreviews
private fun heartRateSimple(context: Context) = heartRateSimplePreview(context)

// @Preview
private fun heartRateGraph(context: Context) {
    TODO()
}

@MultiRoundDevicesPreviews
private fun meditationChips(context: Context) =
    TilePreviewData(
        resources {
            addIdToImageMapping(
                Meditation.CHIP_1_ICON_ID,
                drawableResToImageResource(R.drawable.ic_breathe_24),
            )
            addIdToImageMapping(
                Meditation.CHIP_2_ICON_ID,
                drawableResToImageResource(R.drawable.ic_mindfulness_24),
            )
        }
    ) {
        TilePreviewHelper.singleTimelineEntryTileBuilder(
                Meditation.chipsLayout(
                    context,
                    it.deviceConfiguration,
                    numOfLeftTasks = 2,
                    session1 =
                        Meditation.Session(
                            label = "Breathe",
                            iconId = Meditation.CHIP_1_ICON_ID,
                            clickable = emptyClickable,
                        ),
                    session2 =
                        Meditation.Session(
                            label = "Daily mindfulness",
                            iconId = Meditation.CHIP_2_ICON_ID,
                            clickable = emptyClickable,
                        ),
                    browseClickable = emptyClickable,
                )
            )
            .build()
    }

@MultiRoundDevicesPreviews
private fun meditationButtons(context: Context) = meditationButtonsPreview(context)

@MultiRoundDevicesPreviews private fun timer(context: Context) = timerPreview(context)

@MultiRoundDevicesPreviews private fun alarm(context: Context) = alarmPreview(context)
