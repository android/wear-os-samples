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
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.google.android.horologist.tiles.images.drawableResToImageResource

object Weather {

    const val SCATTERED_SHOWERS_ICON_ID = "weather scattered showers icon"

    fun layout(
        context: Context,
        deviceParameters: DeviceParameters,
        location: String,
        weatherIconId: String,
        currentTemperature: String,
        lowTemperature: String,
        highTemperature: String,
        weatherSummary: String
    ) =
        PrimaryLayout.Builder(deviceParameters)
            .setResponsiveContentInsetEnabled(true)
            .setPrimaryLabelTextContent(
                Text.Builder(context, location)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.Blue))
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .build()
            )
            .setContent(
                MultiSlotLayout.Builder()
                    .addSlotContent(
                        Image.Builder()
                            .setWidth(dp(32f))
                            .setHeight(dp(32f))
                            .setResourceId(weatherIconId)
                            .build()
                    )
                    .addSlotContent(
                        Text.Builder(context, currentTemperature)
                            .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                            .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                            .build()
                    )
                    .addSlotContent(
                        Column.Builder()
                            .addContent(
                                Text.Builder(context, highTemperature)
                                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                                    .build()
                            )
                            .addContent(
                                Text.Builder(context, lowTemperature)
                                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                                    .setColor(ColorBuilders.argb(GoldenTilesColors.Gray))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .setSecondaryLabelTextContent(
                Text.Builder(context, weatherSummary)
                    .setColor(ColorBuilders.argb(GoldenTilesColors.White))
                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .build()
            )
            .build()
}

@MultiRoundDevicesWithFontScalePreviews
internal fun weatherPreview(context: Context) =
    TilePreviewData(
        resources {
            addIdToImageMapping(
                Weather.SCATTERED_SHOWERS_ICON_ID,
                drawableResToImageResource(R.drawable.scattered_showers)
            )
        }
    ) {
        TilePreviewHelper.singleTimelineEntryTileBuilder(
            Weather.layout(
                context,
                it.deviceConfiguration,
                location = "San Francisco",
                weatherIconId = Weather.SCATTERED_SHOWERS_ICON_ID,
                currentTemperature = "52°",
                lowTemperature = "48°",
                highTemperature = "64°",
                weatherSummary = "Showers"
            )
        )
            .build()
    }
