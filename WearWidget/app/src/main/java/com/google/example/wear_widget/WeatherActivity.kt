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
package com.google.example.wear_widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import kotlinx.coroutines.launch

class WeatherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val weatherState by
                remember { weatherStateFlow }
                    .collectAsState(initial = WeatherState(72, WeatherCondition.SUNNY))
            MaterialTheme {
                AppScaffold {
                    WeatherControlPanel(
                        currentState = weatherState,
                        onUpdate = { temp, cond ->
                            scope.launch {
                                setWeatherState(WeatherState(temp, cond))
                                WeatherWidget().triggerUpdateAll(this@WeatherActivity)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun WeatherControlPanel(
    currentState: WeatherState,
    onUpdate: (Int, WeatherCondition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = listState, modifier = modifier) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                ListHeader(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(stringResource(R.string.weather_push_title), textAlign = TextAlign.Center)
                }
            }
            item {
                Text(
                    stringResource(R.string.weather_push_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .transformedHeight(this, transformationSpec),
                )
            }

            item {
                WeatherButton(
                    label = stringResource(R.string.weather_condition_sunny),
                    temp = 72,
                    cond = WeatherCondition.SUNNY,
                    isSelected = currentState.condition == WeatherCondition.SUNNY,
                    onUpdate = onUpdate,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                WeatherButton(
                    label = stringResource(R.string.weather_condition_cloudy),
                    temp = 55,
                    cond = WeatherCondition.CLOUDY,
                    isSelected = currentState.condition == WeatherCondition.CLOUDY,
                    onUpdate = onUpdate,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                WeatherButton(
                    label = stringResource(R.string.weather_condition_rainy),
                    temp = 48,
                    cond = WeatherCondition.RAINY,
                    isSelected = currentState.condition == WeatherCondition.RAINY,
                    onUpdate = onUpdate,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                WeatherButton(
                    label = stringResource(R.string.weather_condition_snowy),
                    temp = 28,
                    cond = WeatherCondition.SNOWY,
                    isSelected = currentState.condition == WeatherCondition.SNOWY,
                    onUpdate = onUpdate,
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
        }
    }
}

@Composable
private fun WeatherButton(
    label: String,
    temp: Int,
    cond: WeatherCondition,
    isSelected: Boolean,
    onUpdate: (Int, WeatherCondition) -> Unit,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    RadioButton(
        selected = isSelected,
        onSelect = { onUpdate(temp, cond) },
        modifier = modifier,
        transformation = transformation,
    ) {
        Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun WeatherControlPanelPreview() {
    MaterialTheme {
        AppScaffold {
            WeatherControlPanel(
                currentState = WeatherState(72, WeatherCondition.SUNNY),
                onUpdate = { _, _ -> },
            )
        }
    }
}
