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
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

object Media {
    const val CHIP_1_ICON_ID = "media_1"
    const val CHIP_2_ICON_ID = "media_2"

    fun layout(
        context: Context,
        deviceParameters: DeviceParameters,
        playlist1: Playlist,
        playlist2: Playlist,
        browseClickable: Clickable,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .apply {
                if (deviceParameters.screenWidthDp > 225) {
                    setPrimaryLabelTextContent(
                        Text.Builder(context, "Last Played")
                            .setTypography(Typography.TYPOGRAPHY_BODY2)
                            .setColor(argb(GoldenTilesColors.Pink))
                            .build()
                    )
                }
            }
            .setContent(
                Column.Builder()
                    // See the comment on `setWidth` below in `playlistChip()` too. The default
                    // width
                    // for column is "wrap", so we need to explicitly set it to "expand" so that we
                    // give
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
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray),
                        )
                    )
                    .build()
            )
            .build()

    private fun playlistChip(
        context: Context,
        deviceParameters: DeviceParameters,
        playlist: Playlist,
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
                    /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White),
                )
            )
            .build()
    }

    data class Playlist(val label: String, val iconId: String, val clickable: Clickable)
}

@MultiRoundDevicesWithFontScalePreviews
internal fun mediaPreview(context: Context) =
    TilePreviewData(
        resources {
            addIdToImageMapping(
                Media.CHIP_1_ICON_ID,
                drawableResToImageResource(R.drawable.ic_music_queue_24),
            )
            addIdToImageMapping(
                Media.CHIP_2_ICON_ID,
                drawableResToImageResource(R.drawable.ic_podcasts_24),
            )
        }
    ) {
        TilePreviewHelper.singleTimelineEntryTileBuilder(
                Media.layout(
                    context,
                    it.deviceConfiguration,
                    playlist1 =
                        Media.Playlist(
                            label = "Liked songs",
                            iconId = Media.CHIP_1_ICON_ID,
                            clickable = emptyClickable,
                        ),
                    playlist2 =
                        Media.Playlist(
                            label = "Podcasts",
                            iconId = Media.CHIP_2_ICON_ID,
                            clickable = emptyClickable,
                        ),
                    browseClickable = emptyClickable,
                )
            )
            .build()
    }
