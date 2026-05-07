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

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.glance.wear.GlanceWearWidgetManager
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.launch

class WeatherActivity : ComponentActivity() {
    @android.annotation.SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val weatherState by weatherStateFlow.collectAsState(initial = WeatherState(72, "☀️"))
            MaterialTheme {
                WeatherControlPanel(
                    currentState = weatherState,
                    onUpdate = { temp, cond ->
                        scope.launch {
                            setWeatherState(WeatherState(temp, cond))
                            triggerUpdateOption1(this@WeatherActivity)
                            // triggerUpdateOption2(this@WeatherActivity)
                        }
                    },
                )
            }
        }
    }

    private fun triggerUpdateOption1(context: android.content.Context) {
        @SuppressLint("RestrictedApi")
        WeatherWidget()
            .triggerUpdate(context, ComponentName(context, WeatherWidgetService::class.java))
    }

    private suspend fun triggerUpdateOption2(context: android.content.Context) {
        val manager = GlanceWearWidgetManager(context)
        val activeWidgets = manager.fetchActiveWidgets(WeatherWidget::class)
        val widget = WeatherWidget()
        activeWidgets.forEach { handle -> widget.triggerUpdate(context, handle.instanceId) }
    }
}

@Composable
private fun WeatherControlPanel(currentState: WeatherState, onUpdate: (Int, String) -> Unit) {
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item {
                Text(
                    stringResource(R.string.weather_push_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Text(
                    stringResource(R.string.weather_push_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }

            item {
                WeatherButton(
                    stringResource(R.string.weather_condition_sunny),
                    72,
                    "☀️",
                    currentState.condition == "☀️",
                    onUpdate,
                )
            }
            item {
                WeatherButton(
                    stringResource(R.string.weather_condition_cloudy),
                    55,
                    "☁️",
                    currentState.condition == "☁️",
                    onUpdate,
                )
            }
            item {
                WeatherButton(
                    stringResource(R.string.weather_condition_rainy),
                    48,
                    "🌧️",
                    currentState.condition == "🌧️",
                    onUpdate,
                )
            }
            item {
                WeatherButton(
                    stringResource(R.string.weather_condition_snowy),
                    28,
                    "❄️",
                    currentState.condition == "❄️",
                    onUpdate,
                )
            }
        }
    }
}

@Composable
private fun WeatherButton(
    label: String,
    temp: Int,
    cond: String,
    isSelected: Boolean,
    onUpdate: (Int, String) -> Unit,
) {
    Button(
        onClick = { onUpdate(temp, cond) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                contentColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            ),
    ) {
        Text(label)
    }
}
