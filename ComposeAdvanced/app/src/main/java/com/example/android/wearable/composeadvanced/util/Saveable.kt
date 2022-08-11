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
package com.example.android.wearable.composeadvanced.util

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle

// Interop between SavedStateHandle and Compose Saver/SaveableStateRegistry
// Can be removed after https://issuetracker.google.com/195689777
fun <T : Any> SavedStateHandle.saveable(
    key: String,
    saver: Saver<T, out Any> = autoSaver(),
    init: () -> T
): T {
    @Suppress("UNCHECKED_CAST")
    saver as Saver<T, Any>
    // value is restored using the SavedStateHandle or created via [init] lambda
    val value = get<Bundle?>(key)?.get("value")?.let(saver::restore) ?: init()

    // Hook up saving the state to the SavedStateHandle
    setSavedStateProvider(key) {
        bundleOf("value" to with(saver) { SaverScope { true }.save(value) })
    }
    return value
}
