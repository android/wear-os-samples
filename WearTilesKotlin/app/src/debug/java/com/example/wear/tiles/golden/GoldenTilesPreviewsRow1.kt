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
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper.singleTimelineEntryTileBuilder
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Goal(context: Context) = TilePreviewData {
    singleTimelineEntryTileBuilder(
        Goal.layout(
            context, it.deviceConfiguration, steps = 5168, goal = 8000
        )
    ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun WorkoutButtons(context: Context) = TilePreviewData(onTileResourceRequest = resources {
    addIdToImageMapping(
        Workout.BUTTON_1_ICON_ID, drawableResToImageResource(R.drawable.ic_run_24)
    )
    addIdToImageMapping(
        Workout.BUTTON_2_ICON_ID, drawableResToImageResource(R.drawable.ic_yoga_24)
    )
    addIdToImageMapping(
        Workout.BUTTON_3_ICON_ID, drawableResToImageResource(R.drawable.ic_cycling_24)
    )
}) {
    singleTimelineEntryTileBuilder(
        Workout.buttonsLayout(
            context,
            it.deviceConfiguration,
            weekSummary = "1 run this week",
            button1Clickable = emptyClickable,
            button2Clickable = emptyClickable,
            button3Clickable = emptyClickable,
            chipClickable = emptyClickable
        )
    ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun WorkoutLargeChip(context: Context) = TilePreviewData {
    singleTimelineEntryTileBuilder(
        Workout.largeChipLayout(
            context,
            it.deviceConfiguration,
            clickable = emptyClickable,
            lastWorkoutSummary = "Last session 45m"
        )
    ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Run(context: Context) = TilePreviewData {
    singleTimelineEntryTileBuilder(
        Run.layout(
            context,
            it.deviceConfiguration,
            lastRunText = "2 days ago",
            chanceOfRain = 20,
            startRunClickable = emptyClickable,
            moreChipClickable = emptyClickable
        )
    ).build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Ski(context: Context) = TilePreviewData {
    singleTimelineEntryTileBuilder(
        Ski.layout(
            context,
            stat1 = Ski.Stat("Max Spd", "46.5", "mph"),
            stat2 = Ski.Stat("Distance", "21.8", "mile")
        )
    ).build()
}

//@Preview
fun SleepTracker(context: Context) {
    // TODO: This tile doesn't use standard components; we can achieve it by drawing on a Canvas (Compose's DrawScope) then converting it to a bitmap using Horologist
}

internal fun resources(fn: ResourceBuilders.Resources.Builder.() -> Unit): (RequestBuilders.ResourcesRequest) -> ResourceBuilders.Resources =
    {
        ResourceBuilders.Resources.Builder().setVersion(it.version).apply(fn).build()
    }
