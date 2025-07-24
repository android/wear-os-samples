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
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.material3.ButtonGroupDefaults.DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS
import androidx.wear.protolayout.material3.CardDefaults.filledVariantCardColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.BODY_SMALL
import androidx.wear.protolayout.material3.Typography.NUMERAL_LARGE
import androidx.wear.protolayout.material3.Typography.NUMERAL_MEDIUM
import androidx.wear.protolayout.material3.Typography.TITLE_MEDIUM
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.padding
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tiles.tooling.preview.TilePreviewHelper
import com.example.wear.tiles.R
import com.example.wear.tiles.tools.MultiRoundDevicesWithFontScalePreviews
import com.example.wear.tiles.tools.addIdToImageMapping
import com.example.wear.tiles.tools.box
import com.example.wear.tiles.tools.column
import com.example.wear.tiles.tools.isLargeScreen
import com.example.wear.tiles.tools.resources
import com.example.wear.tiles.tools.row

private fun getRandomWeatherIcon(context: Context): String {
  val weatherIcons =
    listOf(
      R.drawable.scattered_showers,
      R.drawable.baseline_cloud_24,
      R.drawable.baseline_thunderstorm_24,
      R.drawable.outline_partly_cloudy_day_24
    )
  return context.resources.getResourceName(weatherIcons.random())
}

object Weather {
  data class Forecast(
    val weatherIconId: String,
    val temperature: String,
    val time: String
  )

  data class Conditions(
    val weatherIconId: String,
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
    deviceParameters: DeviceParameters,
    data: WeatherData
  ) =
    materialScope(context, deviceParameters) {
      primaryLayout(
        titleSlot = { text(data.location.layoutString) },
        mainSlot = {
          column {
            setHeight(expand())
            setWidth(expand())
            addContent(conditions(data.conditions))
            addContent(
              Spacer.Builder().setWidth(expand()).setHeight(dp(12F)).build()
            )
            addContent(forecast(data.forecast))
          }
        }
      )
    }

  private fun MaterialScope.conditions(
    conditions: Weather.Conditions
  ): LayoutElement = box {
    setHeight(weight(0.40F))
    setWidth(expand())
    addContent(
      row {
        setHeight(expand())
        setWidth(expand())
        setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
        addContent(
          icon(
            conditions.weatherIconId,
            width = dp(32F),
            height = dp(32F),
            tintColor = colorScheme.tertiary
          )
        )
        addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
        addContent(
          text(
            conditions.currentTemperature.layoutString,
            typography = if (isLargeScreen()) NUMERAL_LARGE else NUMERAL_MEDIUM
          )
        )
        addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
        addContent(
          column {
            addContent(
              text(
                conditions.highTemperature.layoutString,
                typography = TITLE_MEDIUM
              )
            )
            addContent(
              text(
                conditions.lowTemperature.layoutString,
                typography = TITLE_MEDIUM
              )
            )
          }
        )
      }
    )
  }

  private fun MaterialScope.forecast(forecast: List<Forecast>): LayoutElement =
    card( // helpme: change the corner radius (shapes) to "large"
      onClick = clickable(),
      height = weight(0.60F),
      width = expand(),
      modifier =
      LayoutModifier.background(filledVariantCardColors().backgroundColor),
      contentPadding = padding(top = 10f)
    ) {
      row {
        setWidth(expand())
        setHeight(expand())
        val maxForecasts = if (isLargeScreen()) 4 else 3
        val displayedForecasts = forecast.take(maxForecasts)
        displayedForecasts.forEachIndexed { index, forecast ->
          addContent(hourForecast(forecast))
          if (index < displayedForecasts.size - 1) {
            addContent(
              Spacer.Builder().setWidth(dp(0f)).setHeight(expand()).build()
            )
          }
        }
      }
    }

  private fun MaterialScope.hourForecast(forecast: Forecast): LayoutElement {
    return column {
      setWidth(expand())
      setHeight(expand())
      addContent(
        column {
          addContent(icon(forecast.weatherIconId))
          addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
          if (isLargeScreen()) {
            addContent(
              text(forecast.temperature.layoutString, typography = TITLE_MEDIUM)
            )
            addContent(DEFAULT_SPACER_BETWEEN_BUTTON_GROUPS)
          }
          addContent(text(forecast.time.layoutString, typography = BODY_SMALL))
        }
      )
    }
  }

  fun resources(context: Context) = resources {
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.scattered_showers),
      R.drawable.scattered_showers
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.baseline_cloud_24),
      R.drawable.baseline_cloud_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(R.drawable.baseline_thunderstorm_24),
      R.drawable.baseline_thunderstorm_24
    )
    addIdToImageMapping(
      context.resources.getResourceName(
        R.drawable.outline_partly_cloudy_day_24
      ),
      R.drawable.outline_partly_cloudy_day_24
    )
  }
}

class WeatherTileService : BaseTileService() {
  override fun layout(
    context: Context,
    deviceParameters: DeviceParameters
  ): LayoutElement {
    return Weather.layout(
      context,
      deviceParameters,
      Weather.WeatherData(
        location = "San Francisco",
        conditions =
        Weather.Conditions(
          weatherIconId = getRandomWeatherIcon(context),
          currentTemperature = "52°",
          lowTemperature = "48°",
          highTemperature = "64°",
          weatherSummary = "Showers"
        ),
        forecast =
        listOf(
          Weather.Forecast(getRandomWeatherIcon(context), "68°", "9AM"),
          Weather.Forecast(getRandomWeatherIcon(context), "65°", "10AM"),
          Weather.Forecast(getRandomWeatherIcon(context), "62°", "11AM"),
          Weather.Forecast(getRandomWeatherIcon(context), "60°", "12PM")
        )
      )
    )
  }

  override fun resources(context: Context) = Weather.resources(context)
}

@MultiRoundDevicesWithFontScalePreviews
internal fun weatherPreview(context: Context) =
  TilePreviewData(Weather.resources(context)) {
    TilePreviewHelper.singleTimelineEntryTileBuilder(
      Weather.layout(
        context,
        it.deviceConfiguration,
        Weather.WeatherData(
          location = "San Francisco",
          conditions =
          Weather.Conditions(
            weatherIconId = getRandomWeatherIcon(context),
            currentTemperature = "52°",
            lowTemperature = "48°",
            highTemperature = "64°",
            weatherSummary = "Showers"
          ),
          forecast =
          listOf(
            Weather.Forecast(getRandomWeatherIcon(context), "68°", "9AM"),
            Weather.Forecast(getRandomWeatherIcon(context), "65°", "10AM"),
            Weather.Forecast(getRandomWeatherIcon(context), "62°", "11AM"),
            Weather.Forecast(getRandomWeatherIcon(context), "60°", "12PM")
          )
        )
      )
    )
      .build()
  }
