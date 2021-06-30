/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ComplicationTestSuite")

/**
 * Returns the current state for a given complication.
 */
suspend fun ComplicationToggleArgs.getState(context: Context): Long {
    val stateKey = getStatePreferenceKey()
    return context.dataStore.data
        .map { preferences ->
            preferences[stateKey] ?: 0
        }
        .first()
}

/**
 * Updates the current state for a given complication, incrementing it by 1.
 */
suspend fun ComplicationToggleArgs.updateState(context: Context) {
    val stateKey = getStatePreferenceKey()
    context.dataStore.edit { preferences ->
        val currentValue = preferences[stateKey] ?: 0
        preferences[stateKey] = currentValue + 1 // benign overflow possible, all samples take a modulo of this number
    }
}
