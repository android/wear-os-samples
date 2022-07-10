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
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.material.Chip
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
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
                .addContent(sessionChip(context, deviceParameters, session1))
                .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                .addContent(sessionChip(context, deviceParameters, session2))
                .build()
        )
        .setPrimaryChipContent(
            CompactChip.Builder(context, "Browse", browseClickable, deviceParameters)
                .setChipColors(
                    ChipColors(
                        /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.LightPurple),
                        /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray)
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
            .setIconContent(session.iconId)
            .setPrimaryLabelContent(session.label)
            .setChipColors(
                ChipColors(
                    /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkPurple),
                    /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White)
                )
            )
            .build()

    }

    data class Session(val label: String, val iconId: String, val clickable: Clickable)
}
