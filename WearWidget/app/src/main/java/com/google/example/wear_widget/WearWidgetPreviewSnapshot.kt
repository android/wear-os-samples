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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.remote.tooling.preview.RemoteDocPreview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.core.WearWidgetParams
import kotlinx.coroutines.runBlocking

/**
 * Previews a [GlanceWearWidget] within a widget container in the Android Studio Preview.
 *
 * Note: This is taken from
 * https://android-review.googlesource.com/c/platform/frameworks/support/+/4045856 Once that change
 * lands in the library:
 * 1. Remove this file.
 * 2. Update imports and usages in the project to use
 *    `androidx.glance.wear.tooling.preview.WearWidgetPreview`.
 */
@SuppressLint("RestrictedApi")
@Composable
fun WearWidgetPreviewSnapshot(
    widget: GlanceWearWidget,
    params: WearWidgetParams,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val document =
        remember(widget, params, context) {
            runBlocking {
                val widgetData = widget.provideWidgetData(context, params)
                widgetData.captureRawContent(context, params).rcDocument
            }
        }

    RemoteDocPreview(
        document,
        modifier =
            modifier
                .width((params.widthDp + 2f * params.horizontalPaddingDp).dp)
                .height((params.heightDp + 2f * params.verticalPaddingDp).dp),
    )
}
