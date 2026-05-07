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
import android.util.Log
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
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
import androidx.wear.compose.remote.material3.RemoteMaterialTheme
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

private val DefaultBlue = Color(0xFF2196F3)

// Suppressed file-level RestrictedApi because Remote Compose APIs are currently restricted to
// LIBRARY_GROUP.
class HelloWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = HelloWidget()
}

class HelloWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(
        context: Context,
        params: WearWidgetParams,
    ): WearWidgetData {
        Log.d("HelloWidget", "provideWidgetData")
        val localColorScheme = ColorScheme()
        val remoteColorScheme = RemoteColorScheme(localColorScheme)
        return WearWidgetDocument(background = WearWidgetBrush.color(remoteColorScheme.primary)) {
            HelloWidgetContent(remoteColorScheme, context.getString(R.string.hello_world))
        }
    }
}

@RemoteComposable
@Composable
fun HelloWidgetContent(colorScheme: RemoteColorScheme, text: String) {
    RemoteMaterialTheme(colorScheme = colorScheme) {
        RemoteBox(
            modifier = RemoteModifier.fillMaxSize(),
            contentAlignment = RemoteAlignment.Center,
        ) {
            RemoteText(text = text.rs, color = colorScheme.onPrimary, fontSize = 20.rsp)
        }
    }
}

@WearPreviewDevices
@Composable
fun HelloWidgetContentPreview() = RemotePreview {
    val localColorScheme = ColorScheme()
    val remoteColorScheme = RemoteColorScheme(localColorScheme)
    HelloWidgetContent(remoteColorScheme, "Hello, World!")
}
