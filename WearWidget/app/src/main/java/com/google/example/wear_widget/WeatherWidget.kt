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
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteArrangement
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteColumn
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteImage
import androidx.compose.remote.creation.compose.layout.RemoteRow
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.fillMaxWidth
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.rb
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.glance.wear.AssociateWithGlanceWearWidget
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.ContainerInfo
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.image
import androidx.glance.wear.tooling.preview.RectangularAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.RoundAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.SquircleAllWidgetPreviewParams
import androidx.glance.wear.tooling.preview.WearWidgetPreview
import androidx.wear.compose.remote.material3.RemoteMaterialTheme

private val ColorWeatherFallbackBackground = Color(0xFF063443)
private val ColorWeatherOnBackground = Color.White
private val FontWeightEmphasis = FontWeight(550)
private val FontWeightBody = FontWeight.Medium

private const val COMPACT_WIDTH_THRESHOLD_DP = 200f

private const val ICON_SIZE_DP = 44

data class WeatherWidgetUiModel(
    val location: String,
    val currentTemp: String,
    val highTemp: String,
    val lowTemp: String,
    val feelsLike: String,
    val highLow: String,
    val condition: WeatherCondition,
    val conditionLabel: String,
)

private fun buildWeatherWidgetUiModel(
    context: Context,
    location: String,
    temp: Int,
    condition: WeatherCondition,
): WeatherWidgetUiModel {
    val high = temp + 7
    val low = temp - 7
    val feelsLike = temp + 3
    return WeatherWidgetUiModel(
        location = location,
        currentTemp = context.getString(R.string.weather_temperature_format, temp),
        highTemp = context.getString(R.string.weather_high_format, high),
        lowTemp = context.getString(R.string.weather_low_format, low),
        feelsLike = context.getString(R.string.weather_feels_like_format, feelsLike),
        highLow = context.getString(R.string.weather_high_low_format, high, low),
        condition = condition,
        conditionLabel = context.getString(condition.labelRes()),
    )
}

private fun WeatherCondition.labelRes(): Int =
    when (this) {
        WeatherCondition.SUNNY -> R.string.weather_condition_sunny
        WeatherCondition.CLOUDY -> R.string.weather_condition_cloudy
        WeatherCondition.RAINY -> R.string.weather_condition_rainy
        WeatherCondition.SNOWY -> R.string.weather_condition_snowy
    }

@DrawableRes
private fun WeatherCondition.iconRes(): Int? =
    when (this) {
        WeatherCondition.SUNNY,
        WeatherCondition.CLOUDY -> R.drawable.weather_cloudy
        WeatherCondition.RAINY,
        WeatherCondition.SNOWY -> null
    }

private fun Context.decodeImage(@DrawableRes resId: Int): ImageBitmap? =
    BitmapFactory.decodeResource(resources, resId)?.asImageBitmap()

private fun weatherDocument(
    context: Context,
    params: WearWidgetParams,
    model: WeatherWidgetUiModel,
): WearWidgetData {
    val backgroundImage = context.decodeImage(R.drawable.weather_bg_cloudy)
    val icon = model.condition.iconRes()?.let { context.decodeImage(it) }
    val colorBrush = WearWidgetBrush.color(ColorWeatherFallbackBackground.rc)
    val background =
        if (backgroundImage != null) colorBrush.image(backgroundImage.rb, ContentScale.Crop)
        else colorBrush

    return WearWidgetDocument(background = background) {
        RemoteMaterialTheme {
            WeatherWidgetContent(
                containerType = params.containerType,
                isCompact = params.widthDp < COMPACT_WIDTH_THRESHOLD_DP,
                model = model,
                icon = icon,
            )
        }
    }
}

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
        val model = buildWeatherWidgetUiModel(context, location, state.temp, state.condition)
        return weatherDocument(context, params, model)
    }
}

@RemoteComposable
@Composable
fun WeatherWidgetContent(
    @ContainerInfo.ContainerType containerType: Int,
    isCompact: Boolean,
    model: WeatherWidgetUiModel,
    icon: ImageBitmap?,
) {
    when (containerType) {
        ContainerInfo.CONTAINER_TYPE_SMALL ->
            WeatherSmallContent(model = model, icon = icon, isCompact = isCompact)
        else -> WeatherLargeContent(model = model, icon = icon, isCompact = isCompact)
    }
}

@RemoteComposable
@Composable
fun WeatherSmallContent(model: WeatherWidgetUiModel, icon: ImageBitmap?, isCompact: Boolean) {
    RemoteRow(
        modifier = RemoteModifier.fillMaxSize().padding(start = 10.rdp, end = 20.rdp),
        horizontalArrangement = RemoteArrangement.SpaceBetween,
        verticalAlignment = RemoteAlignment.CenterVertically,
    ) {
        RemoteRow(
            verticalAlignment = RemoteAlignment.CenterVertically,
            horizontalArrangement = RemoteArrangement.spacedBy(4.rdp),
        ) {
            ConditionIcon(model = model, icon = icon)
            CurrentTemperature(model = model, isCompact = isCompact)
        }
        RemoteColumn(horizontalAlignment = RemoteAlignment.End) {
            RemoteText(
                text = RemoteString(model.highTemp),
                color = ColorWeatherOnBackground.rc,
                fontSize = 12.rsp,
                fontWeight = FontWeightBody,
                textAlign = TextAlign.End,
            )
            RemoteText(
                text = RemoteString(model.lowTemp),
                color = ColorWeatherOnBackground.rc,
                fontSize = 12.rsp,
                fontWeight = FontWeightBody,
                textAlign = TextAlign.End,
            )
        }
    }
}

@RemoteComposable
@Composable
fun WeatherLargeContent(model: WeatherWidgetUiModel, icon: ImageBitmap?, isCompact: Boolean) {
    RemoteColumn(
        modifier =
            RemoteModifier.fillMaxSize()
                .padding(start = 16.rdp, top = 14.rdp, end = 16.rdp, bottom = 14.rdp),
        verticalArrangement = RemoteArrangement.SpaceBetween,
    ) {
        RemoteRow(
            modifier = RemoteModifier.fillMaxWidth(),
            horizontalArrangement = RemoteArrangement.SpaceBetween,
        ) {
            RemoteText(
                text = RemoteString(model.location),
                modifier = RemoteModifier.weight(1f).padding(top = 3.rdp),
                color = ColorWeatherOnBackground.rc,
                fontSize = 16.rsp,
                fontWeight = FontWeightEmphasis,
                maxLines = 2,
            )
            ConditionIcon(model = model, icon = icon)
        }
        RemoteRow(
            modifier = RemoteModifier.fillMaxWidth(),
            horizontalArrangement = RemoteArrangement.SpaceBetween,
            verticalAlignment = RemoteAlignment.Bottom,
        ) {
            RemoteColumn(modifier = RemoteModifier.weight(1f)) {
                RemoteText(
                    text = RemoteString(model.feelsLike),
                    color = ColorWeatherOnBackground.rc,
                    fontSize = 13.rsp,
                    fontWeight = FontWeightBody,
                    maxLines = 1,
                )
                RemoteText(
                    text = RemoteString(model.highLow),
                    color = ColorWeatherOnBackground.rc,
                    fontSize = 13.rsp,
                    fontWeight = FontWeightBody,
                    maxLines = 1,
                )
            }
            CurrentTemperature(model = model, isCompact = isCompact)
        }
    }
}

@RemoteComposable
@Composable
private fun ConditionIcon(model: WeatherWidgetUiModel, icon: ImageBitmap?) {
    if (icon != null) {
        RemoteImage(
            remoteBitmap  = icon.rb,
            contentDescription = model.conditionLabel.rs,
            modifier = RemoteModifier.size(ICON_SIZE_DP.rdp),
        )
    } else {
        RemoteBox(
            modifier = RemoteModifier.size(ICON_SIZE_DP.rdp),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText(text = RemoteString(model.condition.emoji), fontSize = 30.rsp)
        }
    }
}

@RemoteComposable
@Composable
private fun CurrentTemperature(model: WeatherWidgetUiModel, isCompact: Boolean) {
    RemoteText(
        text = RemoteString(model.currentTemp),
        color = ColorWeatherOnBackground.rc,
        fontSize = if (isCompact) 32.rsp else 40.rsp,
        fontWeight = FontWeightEmphasis,
        maxLines = 1,
    )
}

class MockWeatherWidget(private val temp: Int, private val condition: WeatherCondition) :
    GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        val location = context.getString(R.string.weather_location_san_francisco)
        val model = buildWeatherWidgetUiModel(context, location, temp, condition)
        return weatherDocument(context, params, model)
    }
}

@Preview(name = "Squircle Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun WeatherWidgetSquirclePreview(
    @PreviewParameter(SquircleAllWidgetPreviewParams::class) params: WearWidgetParams
) = WearWidgetPreview(MockWeatherWidget(65, WeatherCondition.SUNNY), params)

@Preview(name = "Round Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun WeatherWidgetRoundPreview(
    @PreviewParameter(RoundAllWidgetPreviewParams::class) params: WearWidgetParams
) = WearWidgetPreview(MockWeatherWidget(65, WeatherCondition.SUNNY), params)

// Generates the uncropped rectangular preview images referenced by
// <container previewImage="@drawable/..." /> in res/xml/weather_widget_info.xml.
// Providing a full rectangular asset allows the system widget picker on each
// device (Pixel Watch, Galaxy Watch, etc.) to apply its own native shape mask.
@Preview(name = "Widget Picker Preview", device = "spec:width=1000dp,height=1000dp,dpi=320")
@Composable
fun WeatherWidgetRectangularPreview(
    @PreviewParameter(RectangularAllWidgetPreviewParams::class) params: WearWidgetParams
) = WearWidgetPreview(MockWeatherWidget(65, WeatherCondition.CLOUDY), params)
