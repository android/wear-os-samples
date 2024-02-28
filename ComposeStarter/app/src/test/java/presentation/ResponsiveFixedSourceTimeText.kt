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
package presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.TimeSource
import androidx.wear.compose.material.TimeText

/**
 * Provides a [TimeText] composable that applies the correct percentage-based padding.
 * TODO: This composable should be removed when ResponsiveTimeText is available in Horologist 0.5.x
 */
@Composable
fun ResponsiveFixedSourceTimeText() {
    val height = LocalConfiguration.current.screenHeightDp
    val padding = height * 0.021
    TimeText(
        contentPadding = PaddingValues(padding.dp),
        timeSource = object : TimeSource {
            override val currentTime: String
                @Composable get() = "10:10"
        }
    )
}
