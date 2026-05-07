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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val KEY_TEMP = intPreferencesKey("temp")
private val KEY_CONDITION = stringPreferencesKey("condition")

private val Context.weatherDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "weather_prefs")

val Context.weatherStateFlow: Flow<WeatherState>
    get() =
        weatherDataStore.data.map { preferences ->
            WeatherState(
                temp = preferences[KEY_TEMP] ?: 72,
                condition = WeatherCondition.fromEmoji(preferences[KEY_CONDITION] ?: "☀️"),
            )
        }

suspend fun Context.getWeatherState(): WeatherState = weatherStateFlow.first()

suspend fun Context.setWeatherState(state: WeatherState) {
    weatherDataStore.edit { preferences ->
        preferences[KEY_TEMP] = state.temp
        preferences[KEY_CONDITION] = state.condition.emoji
    }
}

enum class WeatherCondition(val emoji: String) {
    SUNNY("☀️"),
    CLOUDY("☁️"),
    RAINY("🌧️"),
    SNOWY("❄️");

    companion object {
        fun fromEmoji(emoji: String): WeatherCondition {
            return values().find { it.emoji == emoji } ?: SUNNY
        }
    }
}

data class WeatherState(val temp: Int, val condition: WeatherCondition)
