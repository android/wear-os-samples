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
@file:android.annotation.SuppressLint("RestrictedApi")

package com.google.example.wear_widget

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
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.remote.tooling.preview.RemotePreview
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.remote.material3.RemoteColorScheme
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

private val ColorSunny = Color(0xFF2196F3)
private val ColorCloudy = Color(0xFF9E9E9E)
private val ColorRainy = Color(0xFF673AB7)
private val ColorSnowy = Color(0xFFE3F2FD)

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

        val localColorScheme = ColorScheme()
        val remoteColorScheme = RemoteColorScheme(localColorScheme)

        val bgColor =
            when (state.condition) {
                "☀️" -> ColorSunny
                "☁️" -> ColorCloudy
                "🌧️" -> ColorRainy
                "❄️" -> ColorSnowy
                else -> null
            }

        val textColor = if (state.condition == "❄️") Color.Black else Color.White

        val brush = if (bgColor != null) WearWidgetBrush.color(bgColor.rc) else WearWidgetBrush.color(remoteColorScheme.primary)
        val resolvedTextColor = if (bgColor != null) textColor.rc else remoteColorScheme.onPrimary

        return WearWidgetDocument(background = brush) {
            WeatherContent(state, location, resolvedTextColor)
        }
    }
}

@RemoteComposable
@Composable
fun WeatherContent(state: WeatherState, location: String, textColor: RemoteColor) {
    RemoteBox(modifier = RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
        RemoteColumn(horizontalAlignment = RemoteAlignment.CenterHorizontally) {
            RemoteText(
                text = location.rs,
                color = textColor,
                fontSize = 14.rsp,
                modifier = RemoteModifier.padding(bottom = 4.rdp),
            )
            RemoteText(
                text = "${state.temp}° ${state.condition}".rs,
                color = textColor,
                fontSize = 36.rsp,
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun WeatherContentPreview() = RemotePreview {
    WeatherContent(
        state = WeatherState(72, "☀️"),
        location = "London",
        textColor = Color.White.rc,
    )
}
