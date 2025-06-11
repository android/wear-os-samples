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
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.emptyClickable

object Timer {

    fun layout(
        context: Context,
        deviceParameters: DeviceParameters,
        timerList: List<Timer>,
        clickable: Clickable,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                MultiButtonLayout.Builder()
                    .apply {
                        timerList
                            .take(if (deviceParameters.screenWidthDp > 225) 6 else 5)
                            .forEach { timer -> addButtonContent(timerButton(context, timer)) }
                    }
                    .build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(context, "New", clickable, deviceParameters)
                    .setChipColors(
                        ChipColors(
                            /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkYellow),
                            /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.White),
                        )
                    )
                    .build()
            )
            .build()

    private fun timerButton(context: Context, timer: Timer) =
        Button.Builder(context, timer.clickable)
            .setTextContent(timer.minutes, Typography.TYPOGRAPHY_TITLE3)
            .setButtonColors(
                ButtonColors(
                    /*backgroundColor=*/ ColorBuilders.argb(GoldenTilesColors.Yellow),
                    /*contentColor=*/ ColorBuilders.argb(GoldenTilesColors.DarkerGray),
                )
            )
            .build()

    data class Timer(val minutes: String, val clickable: Clickable)
}

@MultiRoundDevicesWithFontScalePreviews
internal fun timerPreview(context: Context) = TilePreviewData {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
            Timer.layout(
                context,
                it.deviceConfiguration,
                timerList =
                    listOf(
                        Timer.Timer(minutes = "05", clickable = emptyClickable),
                        Timer.Timer(minutes = "10", clickable = emptyClickable),
                        Timer.Timer(minutes = "15", clickable = emptyClickable),
                        Timer.Timer(minutes = "20", clickable = emptyClickable),
                        Timer.Timer(minutes = "30", clickable = emptyClickable),
                        Timer.Timer(minutes = "45", clickable = emptyClickable),
                    ),
                clickable = emptyClickable,
            )
        )
        .build()
}
