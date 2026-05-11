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
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.glance.wear.core.ContainerInfo
import androidx.glance.wear.core.WearWidgetParams
import androidx.glance.wear.core.WidgetInstanceId

/**
 * A [PreviewParameterProvider] that provides a variety of [WearWidgetParams] for Wear previews.
 *
 * Note: This is taken from
 * https://android-review.googlesource.com/c/platform/frameworks/support/+/4045856 Once that change
 * lands in the library:
 * 1. Remove this file.
 * 2. Update imports and usages in the project to use
 *    `androidx.glance.wear.tooling.preview.WearWidgetParamsProvider`.
 */
// Suppressed RestrictedApi because WearWidgetParams is currently restricted to LIBRARY_GROUP.
class WearWidgetParamsProviderSnapshot : PreviewParameterProvider<WearWidgetParams> {
    override val values: Sequence<WearWidgetParams> =
        sequenceOf(
            // Large Widget Preview
            WearWidgetParams(
                instanceId = WidgetInstanceId("widgets", 1),
                containerType = ContainerInfo.CONTAINER_TYPE_LARGE,
                widthDp = 200f,
                heightDp = 112f,
                verticalPaddingDp = 8f,
                horizontalPaddingDp = 8f,
                cornerRadiusDp = 26f,
            ),
            // Small Widget Preview
            WearWidgetParams(
                instanceId = WidgetInstanceId("widgets", 2),
                containerType = ContainerInfo.CONTAINER_TYPE_SMALL,
                widthDp = 200f,
                heightDp = 120f,
                verticalPaddingDp = 8f,
                horizontalPaddingDp = 8f,
                cornerRadiusDp = 26f,
            ),
        )
}
