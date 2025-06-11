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
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.tiles.images.drawableResToImageResource

object Meditation {
    const val CHIP_1_ICON_ID = "meditation_1"
    const val CHIP_2_ICON_ID = "meditation_2"

    fun chipsLayout(
        context: Context,
        deviceParameters: DeviceParameters,
        numOfLeftTasks: Int,
        session1: Session,
        session2: Session,
        browseClickable: Clickable,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .apply {
                if (deviceParameters.screenWidthDp > 225) {
                    setPrimaryLabelTextContent(
                        Text.Builder(context, "$numOfLeftTasks mindful tasks left")
                            .setTypography(Typography.TYPOGRAPHY_BODY2)
                            .setColor(ColorBuilders.argb(GoldenTilesColors.Pink))
                            .build()
                    )
                }
            }
            .setContent(
                Column.Builder()
                    // See the comment on `setWidth` below in `sessionChip()` too. The default width
                    // for
                    // column is "wrap", so we need to explicitly set it to "expand" so that we give
                    // the
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
                            /* backgroundColor = */ ColorBuilders.argb(
                                GoldenTilesColors.LightPurple
                            ),
                            /* contentColor = */ ColorBuilders.argb(GoldenTilesColors.DarkerGray),
                        )
                    )
                    .build()
            )
            .build()

    private fun sessionChip(
        context: Context,
        deviceParameters: DeviceParameters,
        session: Session,
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
                    /* secondaryContentColor = */ ColorBuilders.argb(GoldenTilesColors.White),
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
        timer4: Timer,
        timer5: Timer,
        clickable: Clickable,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
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
                    .apply {
                        if (deviceParameters.screenWidthDp > 225) {
                            addButtonContent(timerButton(context, timer4))
                            addButtonContent(timerButton(context, timer5))
                        }
                    }
                    .build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(context, "New", clickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkPurple),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White),
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
                    /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray),
                )
            )
            .build()

    data class Session(val label: String, val iconId: String, val clickable: Clickable)

    data class Timer(val minutes: Int, val clickable: Clickable)
}

@MultiRoundDevicesWithFontScalePreviews
internal fun meditationChipsPreview(context: Context) =
    TilePreviewData(
        resources {
            addIdToImageMapping(
                Meditation.CHIP_1_ICON_ID,
                drawableResToImageResource(R.drawable.ic_breathe_24),
            )
            addIdToImageMapping(
                Meditation.CHIP_2_ICON_ID,
                drawableResToImageResource(R.drawable.ic_mindfulness_24),
            )
        }
    ) {
        TilePreviewHelper.singleTimelineEntryTileBuilder(
                Meditation.chipsLayout(
                    context,
                    it.deviceConfiguration,
                    numOfLeftTasks = 2,
                    session1 =
                        Meditation.Session(
                            label = "Breathe",
                            iconId = Meditation.CHIP_1_ICON_ID,
                            clickable = emptyClickable,
                        ),
                    session2 =
                        Meditation.Session(
                            label = "Daily mindfulness",
                            iconId = Meditation.CHIP_2_ICON_ID,
                            clickable = emptyClickable,
                        ),
                    browseClickable = emptyClickable,
                )
            )
            .build()
    }

@MultiRoundDevicesWithFontScalePreviews
internal fun meditationButtonsPreview(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
            Meditation.buttonsLayout(
                context,
                it.deviceConfiguration,
                timer1 = Meditation.Timer(minutes = 5, clickable = emptyClickable),
                timer2 = Meditation.Timer(minutes = 10, clickable = emptyClickable),
                timer3 = Meditation.Timer(minutes = 15, clickable = emptyClickable),
                timer4 = Meditation.Timer(minutes = 20, clickable = emptyClickable),
                timer5 = Meditation.Timer(minutes = 25, clickable = emptyClickable),
                clickable = emptyClickable,
            )
        )
        .build()
}
