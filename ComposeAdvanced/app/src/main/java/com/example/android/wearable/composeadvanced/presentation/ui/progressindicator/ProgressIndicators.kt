/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.android.wearable.composeadvanced.presentation.ui.progressindicator

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListAnchorType
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.android.wearable.composeadvanced.presentation.MenuItem
import com.google.android.horologist.compose.navscaffold.scrollableColumn

@Composable
fun ProgressIndicatorsScreen(
    scalingLazyListState: ScalingLazyListState,
    focusRequester: FocusRequester,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier
            .scrollableColumn(focusRequester, scalingLazyListState)
            .fillMaxWidth(),
        state = scalingLazyListState,
        anchorType = ScalingLazyListAnchorType.ItemStart
    ) {
        for (menuItem in menuItems) {
            item {
                CompactChip(
                    onClick = menuItem.clickHander,
                    label = {
                        Text(
                            menuItem.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    }
}

@Composable
fun IndeterminateProgressIndicator(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun FullScreenProgressIndicator(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition()

    val currentRotation by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = LinearEasing,
                delayMillis = 1000
            )
        )
    )
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            startAngle = 315f,
            endAngle = 225f,
            progress = currentRotation,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 1.dp)
        )
    }
}
