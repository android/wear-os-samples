/*
 * Copyright 2026 The Android Open Source Project
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
@file:SuppressLint("RestrictedApi")

package com.google.example.wear_widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.tooling.preview.WearWidgetPreview
import androidx.glance.wear.tooling.preview.SquircleSmallWidgetPreviewParams
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams

private val ColorSunny = Color(0xFF2196F3)
private val ColorCloudy = Color(0xFF9E9E9E)
private val ColorRainy = Color(0xFF673AB7)
private val ColorSnowy = Color(0xFFE3F2FD)

// Suppressed file-level RestrictedApi because Remote Compose APIs are currently restricted to
// LIBRARY_GROUP.
class WeatherWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = WeatherWidget()
}

class WeatherWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val state = context.getWeatherState()

        val location = context.getString(R.string.weather_location_london)

        val bgColor =
            when (state.condition) {
                WeatherCondition.SUNNY -> ColorSunny
                WeatherCondition.CLOUDY -> ColorCloudy
                WeatherCondition.RAINY -> ColorRainy
                WeatherCondition.SNOWY -> ColorSnowy
            }

        val textColor = if (state.condition == WeatherCondition.SNOWY) Color.Black else Color.White

        val brush = WearWidgetBrush.color(bgColor.rc)
        val weatherText =
            context.getString(R.string.weather_format, state.temp, state.condition.emoji)

        return WearWidgetDocument(background = brush) {
            WeatherContent(weatherText, location, textColor.rc)
        }
    }
}

@RemoteComposable
@Composable
fun WeatherContent(weatherText: String, location: String, textColor: RemoteColor) {
    RemoteBox(modifier = RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
        RemoteColumn(horizontalAlignment = RemoteAlignment.CenterHorizontally) {
            RemoteText(
                text = location.rs,
                color = textColor,
                fontSize = 14.rsp,
                modifier = RemoteModifier.padding(bottom = 4.rdp),
            )
            RemoteText(text = weatherText.rs, color = textColor, fontSize = 36.rsp)
        }
    }
}

class MockWeatherWidget(private val temp: Int, private val condition: WeatherCondition) :
    GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val location = context.getString(R.string.weather_location_london)
        val bgColor =
            when (condition) {
                WeatherCondition.SUNNY -> ColorSunny
                WeatherCondition.CLOUDY -> ColorCloudy
                WeatherCondition.RAINY -> ColorRainy
                WeatherCondition.SNOWY -> ColorSnowy
            }
        val textColor = if (condition == WeatherCondition.SNOWY) Color.Black else Color.White
        val brush = WearWidgetBrush.color(bgColor.rc)
        val weatherText = context.getString(R.string.weather_format, temp, condition.emoji)
        return WearWidgetDocument(background = brush) {
            WeatherContent(weatherText, location, textColor.rc)
        }
    }
}

@Preview
@Composable
fun WeatherWidgetPreview(
    @PreviewParameter(SquircleSmallWidgetPreviewParams::class) params: WearWidgetParams
) =
    WearWidgetPreview(
        MockWeatherWidget(75, WeatherCondition.SUNNY),
        params,
    )
