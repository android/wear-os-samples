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
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.profile.RcPlatformProfiles.WEAR_WIDGETS
import androidx.compose.remote.tooling.preview.RemotePreview
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.remote.material3.RemoteColorScheme

/** Common wrapper that handles tooling configuration and suppresses internal lints. */
// TODO: Remove RestrictedApi suppression once WearWidgetParams is made public in alpha09+
@SuppressLint("RestrictedApi")
@Composable
fun WidgetPreview(content: @RemoteComposable @Composable () -> Unit) {
    val localColorScheme = ColorScheme()
    val remoteColorScheme = RemoteColorScheme(localColorScheme)
    RemotePreview(profile = WEAR_WIDGETS) {
        RemoteBox(
            modifier = RemoteModifier.fillMaxSize().background(remoteColorScheme.primary),
            contentAlignment = RemoteAlignment.Center,
        ) {
            content()
        }
    }
}

/** Custom preview annotation for Wear OS Large Round display. */
@Preview(name = "Wear Large Round", device = "id:wearos_large_round", showSystemUi = true)
annotation class PreviewWearLarge
