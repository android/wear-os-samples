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
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.glance.wear.AssociateWithGlanceWearWidget
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.tooling.preview.RectangularSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleSmallWidgetPreviewParams
import androidx.glance.wear.tooling.preview.WearWidgetPreview
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.remote.material3.RemoteText
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales

private val ColorSunny = Color(0xFF1976D2)
private val ColorCloudy = Color(0xFF616161)
private val ColorRainy = Color(0xFF455A64)
private val ColorSnowy = Color(0xFFE0F7FA)

private fun weatherColorPair(condition: WeatherCondition): Pair<RemoteColor, RemoteColor> =
    when (condition) {
        WeatherCondition.SUNNY -> ColorSunny.rc to Color.White.rc
        WeatherCondition.CLOUDY -> ColorCloudy.rc to Color.White.rc
        WeatherCondition.RAINY -> ColorRainy.rc to Color.White.rc
        WeatherCondition.SNOWY -> ColorSnowy.rc to Color.Black.rc
    }

// Suppressed file-level RestrictedApi because Remote Compose APIs are currently restricted to
// LIBRARY_GROUP.
@AssociateWithGlanceWearWidget(WeatherWidget::class)
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
        val (bgColor, textColor) = weatherColorPair(state.condition)
        val brush = WearWidgetBrush.color(bgColor)
        val weatherText =
            context.getString(R.string.weather_format, state.temp, state.condition.emoji)

        return WearWidgetDocument(background = brush) {
            WeatherContent(weatherText, location, textColor)
        }
    }
}

@RemoteComposable
@Composable
fun WeatherContent(weatherText: String, location: String, textColor: RemoteColor) {
    RemoteMaterialTheme {
        RemoteBox(
            modifier = RemoteModifier.fillMaxSize(),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteColumn(horizontalAlignment = RemoteAlignment.CenterHorizontally) {
                RemoteText(
                    text = location.rs,
                    color = textColor,
                    style = RemoteMaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = RemoteModifier.padding(bottom = 4.rdp),
                )
                RemoteText(
                    text = weatherText.rs,
                    color = textColor,
                    style = RemoteMaterialTheme.typography.displaySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
        val (bgColor, textColor) = weatherColorPair(condition)
        val brush = WearWidgetBrush.color(bgColor)
        val weatherText = context.getString(R.string.weather_format, temp, condition.emoji)
        return WearWidgetDocument(background = brush) {
            WeatherContent(weatherText, location, textColor)
        }
    }
}

@Preview(name = "Squircle Preview")
@Composable
fun WeatherWidgetSquirclePreview(
    @PreviewParameter(SquircleAllWidgetPreviewParams::class) params: WearWidgetParams
) {
    WearWidgetPreview(MockWeatherWidget(75, WeatherCondition.SUNNY), params)
}

@Preview(name = "Round Preview")
@Composable
fun WeatherWidgetRoundPreview(
    @PreviewParameter(RoundSmallWidgetPreviewParams::class) params: WearWidgetParams
) {
    WearWidgetPreview(MockWeatherWidget(75, WeatherCondition.SUNNY), params)
}

// Generates the uncropped rectangular preview images referenced by
// <container previewImage="@drawable/..." /> in res/xml/weather_widget_info.xml.
// Providing a full rectangular asset allows the system widget picker on each
// device (Pixel Watch, Galaxy Watch, etc.) to apply its own native shape mask.
@Preview(name = "Widget Picker Preview")
@Composable
fun WeatherWidgetRectangularPreview(
    @PreviewParameter(RectangularSmallWidgetPreviewParams::class) params: WearWidgetParams
) {
    WearWidgetPreview(MockWeatherWidget(75, WeatherCondition.SUNNY), params)
}

@WearPreviewFontScales
@Composable
fun WeatherWidgetFontScalePreview() {
    WearWidgetPreview(
        MockWeatherWidget(75, WeatherCondition.SUNNY),
        SquircleSmallWidgetPreviewParams().values.first(),
    )
}

@Preview(name = "Cloudy Preview")
@Composable
fun WeatherWidgetCloudyPreview() {
    WearWidgetPreview(
        MockWeatherWidget(55, WeatherCondition.CLOUDY),
        SquircleSmallWidgetPreviewParams().values.first(),
    )
}

@Preview(name = "Rainy Preview")
@Composable
fun WeatherWidgetRainyPreview() {
    WearWidgetPreview(
        MockWeatherWidget(48, WeatherCondition.RAINY),
        SquircleSmallWidgetPreviewParams().values.first(),
    )
}

@Preview(name = "Snowy Preview")
@Composable
fun WeatherWidgetSnowyPreview() {
    WearWidgetPreview(
        MockWeatherWidget(28, WeatherCondition.SNOWY),
        SquircleSmallWidgetPreviewParams().values.first(),
    )
}
