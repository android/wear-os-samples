/*
 * Copyright 2024 The Android Open Source Project
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
package com.example.android.wearable.composestarter.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.MaterialTheme

// TODO delete and get from Horologist
object Temp {
    @Composable
    fun ResponsiveListHeader(
        modifier: Modifier = Modifier,
        backgroundColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colors.onSurfaceVariant,
        contentPadding: PaddingValues = itemPadding(),
        content: @Composable RowScope.() -> Unit,
    ) {
        Row(
            modifier = modifier
                .height(IntrinsicSize.Min)
                .wrapContentSize()
                .background(backgroundColor)
                .padding(contentPadding)
                .semantics(mergeDescendants = true) { heading() },
        ) {
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalTextStyle provides MaterialTheme.typography.button,
            ) {
                content()
            }
        }
    }

    @Composable
    internal fun screenWidthDp() = LocalConfiguration.current.screenWidthDp

    /**
     * Padding for the first item in the list. It is recommended to omit
     * top padding for this item so that it is positioned directly below the
     * list's own top padding.
     */
    @Composable
    fun firstItemPadding(): PaddingValues = PaddingValues(
        start = screenWidthDp().dp * HorizontalPaddingPercent,
        end = screenWidthDp().dp * HorizontalPaddingPercent,
        bottom = BottomPadding,
    )

    /**
     * Padding for list items other than the top item in the list.
     */
    @Composable
    fun itemPadding(): PaddingValues = PaddingValues(
        start = screenWidthDp().dp * HorizontalPaddingPercent,
        end = screenWidthDp().dp * HorizontalPaddingPercent,
        top = TopPadding,
        bottom = BottomPadding,
    )

    private const val HorizontalPaddingPercent = 0.073f
    private val TopPadding = 12.dp
    private val BottomPadding = 8.dp
}
