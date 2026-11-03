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

package com.google.example.wear_widget.template

import android.annotation.SuppressLint
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteDp
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.wear.core.WearWidgetParams

/**
 * Responsive layout metrics derived from [WearWidgetParams], adapting padding and sizes between
 * narrow round watch displays (usable width < 120dp) and wide surfaces.
 */
data class WidgetTemplateConfig(
    val isNarrow: Boolean = false,
    val horizontalPadding: RemoteDp = if (isNarrow) 12.rdp else 16.rdp,
    val verticalPadding: RemoteDp = if (isNarrow) 8.rdp else 10.rdp,
    val contentGap: RemoteDp = if (isNarrow) 8.rdp else 12.rdp,
    val badgeSize: RemoteDp = if (isNarrow) 28.rdp else 32.rdp,
    val badgeIconSize: RemoteDp = 16.rdp,
    val cardRadius: RemoteDp = if (isNarrow) 16.rdp else 24.rdp,
    val actionButtonSize: RemoteDp = if (isNarrow) 46.rdp else 52.rdp,
    val actionButtonEndPadding: RemoteDp = 4.rdp,
    val actionButtonVerticalPadding: RemoteDp = 0.rdp,
) {
    companion object {
        /** Threshold below which a host surface is considered narrow. */
        const val NARROW_WIDTH_THRESHOLD_DP = 120f

        /** Derives responsive layout configuration from [WearWidgetParams]. */
        fun from(params: WearWidgetParams): WidgetTemplateConfig {
            val usableWidth = params.widthDp - 2f * params.horizontalPaddingDp
            val isNarrow = usableWidth < NARROW_WIDTH_THRESHOLD_DP || params.heightDp <= 56f
            val totalContainerHeight = params.heightDp + 2f * params.verticalPaddingDp
            val isSmall = params.heightDp <= 64f
            val actionButtonSize =
                if (isSmall) {
                    (totalContainerHeight - 8f).rdp
                } else {
                    ((totalContainerHeight - 12f) / 2f).rdp
                }
            return WidgetTemplateConfig(
                isNarrow = isNarrow,
                actionButtonSize = actionButtonSize,
                actionButtonEndPadding = (4f - params.horizontalPaddingDp).rdp,
                actionButtonVerticalPadding = (4f - params.verticalPaddingDp).rdp,
            )
        }
    }
}

val LocalWidgetTemplateConfig = compositionLocalOf { WidgetTemplateConfig() }

/** Styling color tokens for widget templates with default redline palette. */
data class WidgetTemplateColors(
    val containerColor: RemoteColor = Color(0xFF74332C).rc,
    val secondaryContainerColor: RemoteColor = Color(0xFF442926).rc,
    val contentColor: RemoteColor = Color.White.rc,
    val secondaryContentColor: RemoteColor = Color(0xFFD7C1BF).rc,
    val badgeBackgroundColor: RemoteColor = Color(0xFFFF8578).rc,
    val badgeIconTint: RemoteColor = Color(0xFF450002).rc,
    val actionButtonColor: RemoteColor = Color(0xFF442926).rc,
    val actionIconTint: RemoteColor = Color(0xFFFFD7D2).rc,
)
