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
import androidx.wear.tiles.LayoutElementBuilders.Image
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiSlotLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout

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
    ) = PrimaryLayout.Builder(deviceParameters)
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
