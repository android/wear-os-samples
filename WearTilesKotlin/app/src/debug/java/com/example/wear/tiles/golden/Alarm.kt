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
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.TitleChip
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.PrimaryLayout

object Alarm {

    fun layout(
        context: Context,
        deviceParameters: DeviceParameters,
        timeUntilAlarm: String,
        alarmTime: String,
        alarmDays: String,
        clickable: Clickable
    ) = PrimaryLayout.Builder(deviceParameters)
        .setPrimaryLabelTextContent(
            Text.Builder(context, timeUntilAlarm)
                .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .build()
        )
        .setContent(
            TitleChip.Builder(context, alarmTime, clickable, deviceParameters)
                // TitleChip/Chip's default width == device width minus some padding
                // Since PrimaryLayout's content slot already has margin, this leads to clipping
                // unless we override the width to use the available space
                .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
                .setChipColors(
                    ChipColors(
                        /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.Yellow),
                        /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray)
                    )
                )
                .build()
        )
        .setSecondaryLabelTextContent(
            Text.Builder(context, alarmDays)
                .setColor(ColorBuilders.argb(GoldenTilesColors.Yellow))
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .build()
        )
        .build()
}
