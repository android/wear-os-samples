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
package com.example.android.wearable.composestarter.presentation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureAction
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureClickIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureClickIndicatorState
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureDefaults
import androidx.wear.compose.material3.onehandedgesture.OneHandedGesturePriority
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureScrollIndicator
import androidx.wear.compose.material3.onehandedgesture.OneHandedGestureScrollIndicatorState
import androidx.wear.compose.material3.onehandedgesture.oneHandedGesture
import androidx.wear.compose.material3.onehandedgesture.rememberOneHandedGestureConfiguration

@Composable
fun GestureScreen(modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    val onClick = remember { { isPlaying = !isPlaying } }

    val scrollState = rememberTransformingLazyColumnState()
    val scrollInteractionSource = remember { MutableInteractionSource() }

    val scrollGestureConfig = rememberOneHandedGestureConfiguration(
        action = OneHandedGestureAction.Primary,
        priority = OneHandedGesturePriority.Scrollable
    )
    val scrollIndicatorState = remember { OneHandedGestureScrollIndicatorState() }

    val buttonGestureConfig = rememberOneHandedGestureConfiguration(
        action = OneHandedGestureAction.Primary,
        priority = OneHandedGesturePriority.Clickable
    )
    val buttonIndicatorState = remember { OneHandedGestureClickIndicatorState() }

    ScreenScaffold(
        scrollState = scrollState,
        scrollIndicator = {
            OneHandedGestureScrollIndicator(
                gestureConfiguration = scrollGestureConfig,
                indicatorState = scrollIndicatorState,
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    ) { contentPadding ->
        TransformingLazyColumn(
            state = scrollState,
            contentPadding = contentPadding,
            modifier =
                Modifier
                    .fillMaxSize()
                    .oneHandedGesture(
                        gestureConfiguration = scrollGestureConfig,
                        onGestureLabel = "Scroll to next item",
                        interactionSource = scrollInteractionSource,
                        onGesture = { OneHandedGestureDefaults.scrollDownToNextItem(scrollState) }
                    )
        ) {
            item {
                ListHeader {
                    Text("Gesture Demo")
                }
            }

            // Interactive Gesture Button (Play/Pause)
            item {
                var buttonVisible by remember { mutableStateOf(false) }
                val buttonInteractionSource = remember { MutableInteractionSource() }

                Button(
                    onClick = onClick,
                    interactionSource = buttonInteractionSource,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .onVisibilityChanged { buttonVisible = it } then
                            if (buttonVisible) {
                                Modifier.oneHandedGesture(
                                    gestureConfiguration = buttonGestureConfig,
                                    onGestureLabel = if (isPlaying) "Pause" else "Play",
                                    interactionSource = buttonInteractionSource,
                                    onGesture = onClick
                                )
                            } else {
                                Modifier
                            }
                ) {
                    OneHandedGestureClickIndicator(
                        gestureConfiguration = buttonGestureConfig,
                        state = buttonIndicatorState
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
                            Icon(icon, contentDescription = "Play/Pause")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPlaying) "Pause" else "Play")
                        }
                    }
                }
            }

            // Scrollable Items to demonstrate gesture scrolling
            items(10) { index ->
                Card(
                    onClick = {},
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 10.dp)
                ) {
                    Text("Scrollable Item ${index + 1}", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
