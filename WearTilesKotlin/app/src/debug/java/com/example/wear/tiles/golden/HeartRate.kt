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
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.emptyClickable

object HeartRate {

    fun simpleLayout(
        context: Context,
        deviceParameters: DeviceParameters,
        highestHeartRateBpm: Int,
        lowestHeartRateBpm: Int,
        clickable: Clickable,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setPrimaryLabelTextContent(
                Text.Builder(context, "Now")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .setContent(
                MultiSlotLayout.Builder()
                    .setHorizontalSpacerWidth(16f)
                    .addSlotContent(
                        Column.Builder()
                            .apply {
                                if (deviceParameters.screenWidthDp > 225) {
                                    addContent(
                                        Text.Builder(context, "Highest")
                                            .setTypography(Typography.TYPOGRAPHY_BUTTON)
                                            .setColor(
                                                ColorBuilders.argb(GoldenTilesColors.LightRed)
                                            )
                                            .build()
                                    )
                                }
                            }
                            .addContent(
                                Text.Builder(context, highestHeartRateBpm.toString())
                                    .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                                    .build()
                            )
                            .build()
                    )
                    .apply {
                        if (deviceParameters.screenWidthDp > 225) {
                            addSlotContent(
                                Column.Builder()
                                    .addContent(
                                        Text.Builder(context, "Lowest")
                                            .setTypography(Typography.TYPOGRAPHY_BUTTON)
                                            .setColor(
                                                ColorBuilders.argb(GoldenTilesColors.LightRed)
                                            )
                                            .build()
                                    )
                                    .addContent(
                                        Text.Builder(context, lowestHeartRateBpm.toString())
                                            .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                                            .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                                            .build()
                                    )
                                    .build()
                            )
                        }
                    }
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, "bpm")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.LightGray))
                    .build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(context, "Measure", clickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.LightRed),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.Black),
                        )
                    )
                    .build()
            )
            .build()
}

@MultiRoundDevicesWithFontScalePreviews
internal fun heartRateSimplePreview(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
            HeartRate.simpleLayout(
                context,
                it.deviceConfiguration,
                highestHeartRateBpm = 86,
                lowestHeartRateBpm = 54,
                clickable = emptyClickable,
            )
        )
        .build()
}
