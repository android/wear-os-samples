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
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

/**
 * b/238548541 (internal bug - the spacing doesn't match Figma)
 */
@Preview
fun HeartRateSimple(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        HeartRate.simpleLayout(
            context,
            it.deviceConfiguration,
            heartRateBpm = 86,
            clickable = emptyClickable
        )
    ).build()
}

//@Preview
fun HeartRateGraph(context: Context) {
    TODO()
}

@Preview
fun MeditationChips(context: Context) = TilePreviewData(resources {
    addIdToImageMapping(
        Meditation.CHIP_1_ICON_ID,
        drawableResToImageResource(R.drawable.ic_breathe_24)
    )
    addIdToImageMapping(
        Meditation.CHIP_2_ICON_ID,
        drawableResToImageResource(R.drawable.ic_mindfulness_24)
    )
}) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Meditation.chipsLayout(
            context,
            it.deviceConfiguration,
            session1 = Meditation.Session(
                label = "Breathe",
                iconId = Meditation.CHIP_1_ICON_ID,
                clickable = emptyClickable
            ),
            session2 = Meditation.Session(
                label = "Daily mindfulness",
                iconId = Meditation.CHIP_2_ICON_ID,
                clickable = emptyClickable
            ),
            browseClickable = emptyClickable
        )
    ).build()
}

@Preview
fun MeditationButtons(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Meditation.buttonsLayout(
            context,
            it.deviceConfiguration,
            timer1 = Meditation.Timer(minutes = 5, clickable = emptyClickable),
            timer2 = Meditation.Timer(minutes = 10, clickable = emptyClickable),
            timer3 = Meditation.Timer(minutes = 15, clickable = emptyClickable),
            clickable = emptyClickable
        )
    ).build()
}

@Preview
fun Timer(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Timer.layout(
            context,
            it.deviceConfiguration,
            timer1 = Timer.Timer(minutes = "05", clickable = emptyClickable),
            timer2 = Timer.Timer(minutes = "10", clickable = emptyClickable),
            timer3 = Timer.Timer(minutes = "15", clickable = emptyClickable),
            timer4 = Timer.Timer(minutes = "20", clickable = emptyClickable),
            timer5 = Timer.Timer(minutes = "30", clickable = emptyClickable),
            clickable = emptyClickable
        )
    ).build()
}

@Preview
fun Alarm(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
        Alarm.layout(
            context,
            it.deviceConfiguration,
            timeUntilAlarm = "Less than 1 min",
            alarmTime = "14:58",
            alarmDays = "Mon, Tue, Wed",
            clickable = emptyClickable
        )
    ).build()
}
