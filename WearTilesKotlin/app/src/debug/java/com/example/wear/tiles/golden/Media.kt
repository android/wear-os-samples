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
import androidx.wear.tiles.material.Chip
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.layouts.PrimaryLayout

object Media {
    const val CHIP_1_ICON_ID = "media_1"
    const val CHIP_2_ICON_ID = "media_2"

    fun layout(
        context: Context,
        deviceParameters: DeviceParameters,
        playlist1: Playlist,
        playlist2: Playlist,
        browseClickable: Clickable
    ) = PrimaryLayout.Builder(deviceParameters)
        .setContent(
            Column.Builder()
                // See the comment on `setWidth` below in `playlistChip()` too. The default width
                // for column is "wrap", so we need to explicitly set it to "expand" so that we give
                // the chips enough space to layout
                .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
                .addContent(playlistChip(context, deviceParameters, playlist1))
                .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                .addContent(playlistChip(context, deviceParameters, playlist2))
                .build()
        )
        .setPrimaryChipContent(
            CompactChip.Builder(context, "Browse", browseClickable, deviceParameters)
                .setChipColors(
                    ChipColors(
                        /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.Pink),
                        /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray)
                    )
                )
                .build()
        )
        .build()

    private fun playlistChip(
        context: Context,
        deviceParameters: DeviceParameters,
        playlist: Playlist
    ): Chip {
        return Chip.Builder(context, playlist.clickable, deviceParameters)
            // TitleChip/Chip's default width == device width minus some padding
            // Since PrimaryLayout's content slot already has margin, this leads to clipping
            // unless we override the width to use the available space
            .setWidth(DimensionBuilders.ExpandedDimensionProp.Builder().build())
            .setIconContent(playlist.iconId)
            .setPrimaryLabelContent(playlist.label)
            .setChipColors(
                ChipColors(
                    /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkPink),
                    /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White)
                )
            )
            .build()
    }

    data class Playlist(val label: String, val iconId: String, val clickable: Clickable)
}
