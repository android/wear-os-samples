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
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.emptyClickable
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object News {

    fun layout(
        context: Context,
        deviceParameters: DeviceParameters,
        date: LocalDate,
        clock: Clock = Clock.systemDefaultZone(),
        headline: String,
        newsVendor: String,
        clickable: Clickable,
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .apply {
                if (deviceParameters.screenWidthDp > 225) {
                    setPrimaryLabelTextContent(
                        Text.Builder(
                                context,
                                date.formatLocalDateTime(today = LocalDate.now(clock)),
                            )
                            .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                            .build()
                    )
                }
            }
            .setContent(
                Text.Builder(context, headline)
                    .setMaxLines(3)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .setTypography(Typography.TYPOGRAPHY_BODY1)
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, newsVendor)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.RichBlue))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .build()
            )
            .setPrimaryChipContent(
                CompactChip.Builder(context, "News", clickable, deviceParameters).build()
            )
            .build()
}

internal fun LocalDate.formatLocalDateTime(today: LocalDate = LocalDate.now()): String {
    val yesterday = today.minusDays(1)

    return when {
        this == yesterday -> "yesterday ${format(DateTimeFormatter.ofPattern("MMM d"))}"
        this == today -> "today ${format(DateTimeFormatter.ofPattern("MMM d"))}"
        else -> format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }
}

@MultiRoundDevicesWithFontScalePreviews
internal fun newsPreview(context: Context) = TilePreviewData {
    val now = LocalDateTime.of(2024, 8, 1, 0, 0).toInstant(ZoneOffset.UTC)
    val clock = Clock.fixed(now, Clock.systemUTC().zone)

    TilePreviewHelper.singleTimelineEntryTileBuilder(
            News.layout(
                context,
                it.deviceConfiguration,
                headline = "Millions still without power as new storm moves across US",
                newsVendor = "The New York Times",
                date = LocalDate.now(clock).minusDays(1),
                clock = clock,
                clickable = emptyClickable,
            )
        )
        .build()
}
