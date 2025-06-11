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
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews

object Ski {

    fun layout(
        context: Context,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        stat1: Stat,
        stat2: Stat,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                MultiSlotLayout.Builder()
                    .setHorizontalSpacerWidth(16f)
                    .addSlotContent(statColumn(context, stat1))
                    .addSlotContent(statColumn(context, stat2))
                    .build()
            )
            .build()

    private fun statColumn(context: Context, stat: Stat) =
        LayoutElementBuilders.Column.Builder()
            .addContent(
                Text.Builder(context, stat.label)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.LightBlue))
                    .build()
            )
            .addContent(
                Text.Builder(context, stat.value)
                    .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .addContent(
                Text.Builder(context, stat.unit)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .build()

    data class Stat(val label: String, val value: String, val unit: String)
}

@MultiRoundDevicesWithFontScalePreviews
internal fun skiPreview(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
            Ski.layout(
                context,
                it.deviceConfiguration,
                stat1 = Ski.Stat("Max Spd", "46.5", "mph"),
                stat2 = Ski.Stat("Distance", "21.8", "mile"),
            )
        )
        .build()
}
