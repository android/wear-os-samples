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
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.TitleChip
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper.singleTimelineEntryTileBuilder
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

object Workout {
    const val BUTTON_1_ICON_ID = "workout 1"
    const val BUTTON_2_ICON_ID = "workout 2"
    const val BUTTON_3_ICON_ID = "workout 3"

    fun buttonsLayout(
        context: Context,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        weekSummary: String,
        button1Clickable: ModifiersBuilders.Clickable,
        button2Clickable: ModifiersBuilders.Clickable,
        button3Clickable: ModifiersBuilders.Clickable,
        chipClickable: ModifiersBuilders.Clickable,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setPrimaryLabelTextContent(
                Text.Builder(context, weekSummary)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.Blue))
                    .build()
            )
            .setContent(
                MultiButtonLayout.Builder()
                    .addButtonContent(
                        Button.Builder(context, button1Clickable)
                            .setIconContent(BUTTON_1_ICON_ID)
                            .build()
                    )
                    .addButtonContent(
                        Button.Builder(context, button2Clickable)
                            .setIconContent(BUTTON_2_ICON_ID)
                            .build()
                    )
                    .addButtonContent(
                        Button.Builder(context, button3Clickable)
                            .setIconContent(BUTTON_3_ICON_ID)
                            .build()
                    )
                    .build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(context, "More", chipClickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.BlueGray),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White),
                        )
                    )
                    .build()
            )
            .build()

    fun largeChipLayout(
        context: Context,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        clickable: ModifiersBuilders.Clickable,
        lastWorkoutSummary: String,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setPrimaryLabelTextContent(
                Text.Builder(context, "Power Yoga")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.Yellow))
                    .build()
            )
            .setContent(
                TitleChip.Builder(context, "Start", clickable, deviceParameters)
                    // TitleChip/Chip's default width == device width minus some padding
                    // Since PrimaryLayout's content slot already has margin, this leads to clipping
                    // unless we override the width to use the available space
                    .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.Yellow),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.Black),
                        )
                    )
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, lastWorkoutSummary)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .build()
            )
            .build()
}

@MultiRoundDevicesWithFontScalePreviews
internal fun workoutButtonsPreview(context: Context) =
    TilePreviewData(
        onTileResourceRequest =
            resources {
                addIdToImageMapping(
                    Workout.BUTTON_1_ICON_ID,
                    drawableResToImageResource(R.drawable.ic_run_24),
                )
                addIdToImageMapping(
                    Workout.BUTTON_2_ICON_ID,
                    drawableResToImageResource(R.drawable.ic_yoga_24),
                )
                addIdToImageMapping(
                    Workout.BUTTON_3_ICON_ID,
                    drawableResToImageResource(R.drawable.ic_cycling_24),
                )
            }
    ) {
        singleTimelineEntryTileBuilder(
                Workout.buttonsLayout(
                    context,
                    it.deviceConfiguration,
                    weekSummary = "1 run this week",
                    button1Clickable = emptyClickable,
                    button2Clickable = emptyClickable,
                    button3Clickable = emptyClickable,
                    chipClickable = emptyClickable,
                )
            )
            .build()
    }

@MultiRoundDevicesWithFontScalePreviews
internal fun workoutLargeChipPreview(context: Context) = TilePreviewData {
    singleTimelineEntryTileBuilder(
            Workout.largeChipLayout(
                context,
                it.deviceConfiguration,
                clickable = emptyClickable,
                lastWorkoutSummary = "Last session 45m",
            )
        )
        .build()
}
