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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.remote.tooling.preview.RemoteDocPreview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.core.WearWidgetParams
import androidx.wear.compose.material3.Text
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
    title: String = widget.javaClass.simpleName.replace(Regex("(?<=.)(?=\\p{Lu})"), " "),
) {
    val context = LocalContext.current
    val document =
        remember(widget, params, context) {
            runBlocking {
                val widgetData = widget.provideWidgetData(context, params)
                widgetData.captureRawContent(context, params).rcDocument
            }
        }

    Box(
        modifier = Modifier.size(227.dp).clip(CircleShape).background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        RemoteDocPreview(
            document,
            modifier =
                modifier
                    .width((params.widthDp + 2f * params.horizontalPaddingDp).dp)
                    .height((params.heightDp + 2f * params.verticalPaddingDp).dp),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = 8.dp),
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Android Logo",
                    modifier = Modifier.size(20.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF424242)),
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
