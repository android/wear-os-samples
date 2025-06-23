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
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper.singleTimelineEntryTileBuilder
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews

object Goal {
    fun layout(context: Context, deviceParameters: DeviceParameters, steps: Int, goal: Int) =
        EdgeContentLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setEdgeContent(
                CircularProgressIndicator.Builder()
                    .setProgress(steps.toFloat() / goal)
                    .setCircularProgressIndicatorColors(blueOnTranslucentWhite())
                    .build()
            )
            .setPrimaryLabelTextContent(
                Text.Builder(context, "Steps")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.Blue))
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, "/ $goal")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .setContent(
                Text.Builder(context, "$steps")
                    .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .build()
}

private fun blueOnTranslucentWhite() =
    ProgressIndicatorColors(
        /* indicatorColor = */
        ColorBuilders.argb(GoldenTilesColors.Blue),
        /* trackColor = */
        ColorBuilders.argb(GoldenTilesColors.White10Pc)
    )

@MultiRoundDevicesWithFontScalePreviews
internal fun goalPreview(context: Context) = TilePreviewData {
    singleTimelineEntryTileBuilder(
        Goal.layout(context, it.deviceConfiguration, steps = 5168, goal = 8000)
    )
        .build()
}
