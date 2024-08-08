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

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Goal(context: Context) = GoalPreview(context)

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun WorkoutButtons(context: Context) = WorkoutButtonsPreview(context)

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun WorkoutLargeChip(context: Context) = WorkoutLargeChipPreview(context)

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun Run(context: Context) = RunPreview(context)

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
