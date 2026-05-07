/*
 * Copyright 2022-2026 The Android Open Source Project
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
@file:Suppress("ktlint:standard:max-line-length")

package com.example.wear.tiles.golden

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ProtoLayoutScope
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.box
import androidx.wear.protolayout.layout.column
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_LARGE
import androidx.wear.protolayout.material3.Typography.NUMERAL_MEDIUM
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.buttonGroup
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.materialScopeWithResources
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileService
import androidx.wear.tiles.tile
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.isLargeScreen
import com.google.common.util.concurrent.Futures

object Weather {
    data class Forecast(
        @DrawableRes val weatherIconId: Int,
        val temperature: String,
        val time: String
    )

    data class Conditions(
        @DrawableRes val weatherIconId: Int,
        val currentTemperature: String,
        val lowTemperature: String,
        val highTemperature: String,
        val weatherSummary: String
    )

    data class WeatherData(
        val location: String,
        val conditions: Conditions,
        val forecast: List<Forecast>
    )

    fun layout(
        context: Context,
        scope: ProtoLayoutScope,
        deviceParameters: DeviceParameters,
        data: WeatherData
    ) = materialScopeWithResources(context, scope, deviceParameters) {
        primaryLayout(
            titleSlot = { text(data.location.layoutString) },
            mainSlot = {
                column(
                    conditions(data.conditions),
                    DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS,
                    forecast(data.forecast),
                    width = expand(),
                    height = expand()
                )
            }
        )
    }

    private fun MaterialScope.conditions(conditions: Conditions): LayoutElement =
        box(
            buttonGroup {
                buttonGroupItem {
                    icon(
                        imageResource(androidImageResource(conditions.weatherIconId)),
                        width = dp(32F),
                        height = dp(32F),
                        tintColor = colorScheme.tertiary
                    )
                }
                buttonGroupItem { DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS }
                buttonGroupItem {
                    text(
                        conditions.currentTemperature.layoutString,
                        typography = if (isLargeScreen()) NUMERAL_LARGE else NUMERAL_MEDIUM
                    )
                }
                buttonGroupItem { DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS }
                buttonGroupItem {
                    column(
                        text(
                            conditions.highTemperature.layoutString,
                            typography = TITLE_MEDIUM
                        ),
                        text(
                            conditions.lowTemperature.layoutString,
                            typography = TITLE_MEDIUM
                        )
                    )
                }
            },
            width = expand(),
            height = weight(0.40F),
            verticalAlignment = LayoutElementBuilders.VERTICAL_ALIGN_CENTER
        )

    private fun MaterialScope.forecast(forecast: List<Forecast>): LayoutElement =
        card(
            onClick = clickable(),
            height = weight(0.60F),
            width = expand(),
            modifier = LayoutModifier.background(filledVariantCardColors().backgroundColor),
            contentPadding = padding(top = 5f)
        ) {
            val maxForecasts = if (isLargeScreen()) 4 else 3
            val displayedForecasts = forecast.take(maxForecasts)
            buttonGroup {
                displayedForecasts.forEachIndexed { index, forecast ->
                    buttonGroupItem { hourForecast(forecast) }
                }
            }
        }

    private fun MaterialScope.hourForecast(forecast: Forecast): LayoutElement =
        column(
            column(
                *listOfNotNull(
                    icon(imageResource(androidImageResource(forecast.weatherIconId))),
                    DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS,
                    if (isLargeScreen()) text(forecast.temperature.layoutString, typography = TITLE_MEDIUM) else null,
                    if (isLargeScreen()) DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS else null,
                    text(forecast.time.layoutString, typography = BODY_SMALL)
                ).toTypedArray()
            ),
            width = expand(),
            height = expand()
        )
}

class WeatherTileService : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest) =
        Futures.immediateFuture(
            tile(
                Timeline.fromLayoutElement(
                    Weather.layout(
                        this,
                        requestParams.scope,
                        requestParams.deviceConfiguration,
                        weatherData()
                    )
                )
            )
        )
}

@MultiRoundDevicesWithFontScalePreviews
internal fun weatherPreview(context: Context) =
    TilePreviewData { request ->
        TilePreviewHelper
            .singleTimelineEntryTileBuilder(
                Weather.layout(
                    context,
                    request.scope,
                    request.deviceConfiguration,
                    weatherData()
                )
            ).build()
    }

private fun weatherData() =
    Weather.WeatherData(
        location = "San Francisco",
        conditions =
            Weather.Conditions(
                weatherIconId = R.drawable.scattered_showers,
                currentTemperature = "52°",
                lowTemperature = "48°",
                highTemperature = "64°",
                weatherSummary = "Showers"
            ),
        forecast =
            listOf(
                Weather.Forecast(R.drawable.baseline_cloud_24, "68°", "9AM"),
                Weather.Forecast(R.drawable.baseline_thunderstorm_24, "65°", "10AM"),
                Weather.Forecast(R.drawable.outline_partly_cloudy_day_24, "62°", "11AM"),
                Weather.Forecast(R.drawable.scattered_showers, "60°", "12PM")
            )
    )
