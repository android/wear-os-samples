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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material.CurvedText
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults

/**
 * Custom version of TimeText (Curved Text) that enables leading text (if wanted) and hides while
 * scrolling so user can just focus on the list's items.
 */
@Composable
fun CustomTimeText(
    visible: Boolean,
    showLeadingText: Boolean,
    leadingText: String
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        TimeText(
            leadingCurvedContent = if (showLeadingText) {
                {
                    CurvedText(
                        text = leadingText,
                        style = CurvedTextStyle(TimeTextDefaults.timeTextStyle())
                    )
                }
            } else null,
            leadingLinearContent = if (showLeadingText) {
                {
                    Text(
                        text = leadingText,
                        style = TimeTextDefaults.timeTextStyle()
                    )
                }
            } else null,
        )
    }
}

@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = Devices.WEAR_OS_LARGE_ROUND
)
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = Devices.WEAR_OS_SQUARE
)
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    showSystemUi = true,
    device = Devices.WEAR_OS_SMALL_ROUND
)
// This will only be rendered properly in AS Chipmunk and beyond
@Composable
fun PreviewCustomTimeText() {
    CustomTimeText(
        visible = true,
        showLeadingText = true,
        leadingText = "Testing Leading Text..."
    )
}
