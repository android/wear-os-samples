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
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.TitleChip
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiButtonLayout
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

    private fun timerButton(context: Context, timer: Timer) =
        Button.Builder(context, timer.clickable)
            .setTextContent(timer.minutes, Typography.TYPOGRAPHY_TITLE3)
            .setButtonColors(
                ButtonColors(
                    /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.Yellow),
                    /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray)
                )
            )
            .build()

    data class Timer(val minutes: String, val clickable: Clickable)
}
