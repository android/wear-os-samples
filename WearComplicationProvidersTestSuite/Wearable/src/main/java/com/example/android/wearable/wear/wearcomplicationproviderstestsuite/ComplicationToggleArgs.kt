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

import android.content.ComponentName
import android.os.Parcelable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.parcelize.Parcelize

/**
 * The arguments for toggling a complication.
 */
@Parcelize
data class ComplicationToggleArgs(

    /**
     * The component of the complication being toggled.
     */
    val providerComponent: ComponentName,

    /**
     * An app-defined key for different provided complications.
     */
    val complication: Complication,

    /**
     * The system-defined key for the instance of a provided complication.
     * (it's entirely possible for the same complication to be used multiple times)
     */
    val complicationInstanceId: Int
) : Parcelable

/**
 * Returns the key for the preference used to hold the current state of a given complication.
 */
fun ComplicationToggleArgs.getStatePreferenceKey(): Preferences.Key<Long> =
    longPreferencesKey("${complication.key}_$complicationInstanceId")
