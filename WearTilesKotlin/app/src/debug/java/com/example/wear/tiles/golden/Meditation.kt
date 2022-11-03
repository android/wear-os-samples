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
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.Chip
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiButtonLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout

object Meditation {
    const val CHIP_1_ICON_ID = "meditation_1"
    const val CHIP_2_ICON_ID = "meditation_2"

    fun chipsLayout(
        context: Context,
        deviceParameters: DeviceParameters,
        session1: Session,
        session2: Session,
        browseClickable: Clickable
    ) = PrimaryLayout.Builder(deviceParameters)
        .setContent(
            Column.Builder()
                // See the comment on `setWidth` below in `sessionChip()` too. The default width for
                // column is "wrap", so we need to explicitly set it to "expand" so that we give the
                // chips enough space to layout
                .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
                .addContent(sessionChip(context, deviceParameters, session1))
                .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                .addContent(sessionChip(context, deviceParameters, session2))
                .build()
        )
        .setPrimaryChipContent(
            CompactChip.Builder(context, "Browse", browseClickable, deviceParameters)
                .setChipColors(
                    ChipColors(
                        /* backgroundColor = */ ColorBuilders.argb(GoldenTilesColors.LightPurple),
                        /* contentColor = */ ColorBuilders.argb(GoldenTilesColors.DarkerGray)
                    )
                )
                .build()
        )
        .build()

    private fun sessionChip(
        context: Context,
        deviceParameters: DeviceParameters,
        session: Session
    ): Chip {
        return Chip.Builder(context, session.clickable, deviceParameters)
            // TitleChip/Chip's default width == device width minus some padding
            // Since PrimaryLayout's content slot already has margin, this leads to clipping
            // unless we override the width to use the available space
            .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
            .setIconContent(session.iconId)
            .setPrimaryLabelContent(session.label)
            .setChipColors(
                ChipColors(
                    /* backgroundColor = */ ColorBuilders.argb(GoldenTilesColors.DarkPurple),
                    /* iconColor = */ ColorBuilders.argb(GoldenTilesColors.LightPurple),
                    /* contentColor = */ ColorBuilders.argb(GoldenTilesColors.White),
                    /* secondaryContentColor = */ ColorBuilders.argb(GoldenTilesColors.White)
                )
            )
            .build()
    }

    fun buttonsLayout(
        context: Context,
        deviceParameters: DeviceParameters,
        timer1: Timer,
        timer2: Timer,
        timer3: Timer,
        clickable: Clickable
    ) = PrimaryLayout.Builder(deviceParameters)
        .setPrimaryLabelTextContent(
            Text.Builder(context, "Minutes")
                .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                .build()
        )
        .setContent(
            MultiButtonLayout.Builder()
                .addButtonContent(timerButton(context, timer1))
                .addButtonContent(timerButton(context, timer2))
                .addButtonContent(timerButton(context, timer3))
                .build()
        )
        .setPrimaryChipContent(
            CompactChip.Builder(context, "New", clickable, deviceParameters)
                .setChipColors(
                    ChipColors(
                        /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkPurple),
                        /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White)
                    )
                )
                .build()
        )
        .build()

    private fun timerButton(context: Context, timer: Timer) =
        Button.Builder(context, timer.clickable)
            .setTextContent(timer.minutes.toString(), Typography.TYPOGRAPHY_TITLE3)
            .setButtonColors(
                ButtonColors(
                    /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.LightPurple),
                    /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray)
                )
            )
            .build()

    data class Session(val label: String, val iconId: String, val clickable: Clickable)
    data class Timer(val minutes: Int, val clickable: Clickable)
}
