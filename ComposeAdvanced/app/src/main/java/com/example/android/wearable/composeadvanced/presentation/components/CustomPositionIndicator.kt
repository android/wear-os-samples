/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.android.wearable.composeadvanced.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.ScalingLazyListState

/**
 * Simple PositionIndicator (also known as scroll indicator) that only appears when the user is
 * scrolling the list.
 */
@Composable
fun CustomPositionIndicator(
    visible: Boolean,
    scalingLazyListState: ScalingLazyListState
) {
    if (visible) {
        PositionIndicator(
            scalingLazyListState = scalingLazyListState
        )
    }
}

@Preview(
    widthDp = 300,
    heightDp = 300,
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    backgroundColor = 0x000000,
    showBackground = true
)
@Composable
fun PreviewPositionIndicator() {
    CustomPositionIndicator(visible = true, scalingLazyListState = ScalingLazyListState())
}
