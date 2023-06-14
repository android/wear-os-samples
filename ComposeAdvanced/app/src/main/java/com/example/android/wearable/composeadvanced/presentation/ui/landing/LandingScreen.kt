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
package com.example.android.wearable.composeadvanced.presentation.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.curvedRow
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.radialGradientBackground
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.curvedText
import com.example.android.wearable.composeadvanced.R
import com.example.android.wearable.composeadvanced.presentation.menuNameAndCallback
import com.example.android.wearable.composeadvanced.presentation.navigation.Screen
import com.example.android.wearable.composeadvanced.presentation.ui.util.ReportFullyDrawn
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.ToggleChip
import com.google.android.horologist.compose.material.ToggleChipToggleControl

/**
 * Simple landing page with three actions, view a list of watches, toggle on/off text before the
 * time or view a demo of different user input components.
 *
 * A text label indicates the screen shape and places it at the bottom of the screen.
 * If it's a round device, it will curve the text along the bottom curve. Otherwise, for a square
 * device, it's a regular Text composable.
 */
@Composable
fun LandingScreen(
    columnState: ScalingLazyColumnState,
    onClickWatchList: () -> Unit,
    onNavigate: (String) -> Unit,
    proceedingTimeTextEnabled: Boolean,
    onClickProceedingTimeText: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        menuNameAndCallback(
            onNavigate = onNavigate,
            menuNameResource = R.string.user_input_components_label,
            screen = Screen.UserInputComponents
        ),
        menuNameAndCallback(
            onNavigate = onNavigate,
            menuNameResource = R.string.map_label,
            screen = Screen.Map
        ),
        menuNameAndCallback(
            onNavigate = onNavigate,
            menuNameResource = R.string.dialogs_label,
            screen = Screen.Dialogs
        ),
        menuNameAndCallback(
            onNavigate = onNavigate,
            menuNameResource = R.string.progress_indicators_label,
            screen = Screen.ProgressIndicators
        ),
        menuNameAndCallback(
            onNavigate = onNavigate,
            menuNameResource = R.string.theme_label,
            screen = Screen.Theme
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Places both Chips (button and toggle) in the middle of the screen.
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier.fillMaxSize()
        ) {
            item {
                // Signify we have drawn the content of the first screen
                ReportFullyDrawn()

                Chip(
                    onClick = onClickWatchList,
                    label = {
                        Text(
                            stringResource(R.string.list_of_watches_button_label),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            for (listItem in menuItems) {
                item {
                    Chip(
                        onClick = listItem.clickHander,
                        label = {
                            Text(
                                listItem.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = proceedingTimeTextEnabled,
                    onCheckedChanged = onClickProceedingTimeText,
                    label = stringResource(R.string.proceeding_text_toggle_chip_label),
                    toggleControl = ToggleChipToggleControl.Switch
                )
            }
        }

        // Places curved text at the bottom of round devices and straight text at the bottom of
        // non-round devices.
        if (LocalConfiguration.current.isScreenRound) {
            val watchShape = stringResource(R.string.watch_shape)
            val primaryColor = MaterialTheme.colors.primary
            CurvedLayout(
                anchor = 90F,
                anchorType = AnchorType.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                curvedRow {
                    curvedText(
                        text = watchShape,
                        angularDirection = CurvedDirection.Angular.CounterClockwise,
                        style = CurvedTextStyle(
                            fontSize = 18.sp,
                            color = primaryColor
                        ),
                        modifier = CurvedModifier
                            .radialGradientBackground(
                                0f to Color.Transparent,
                                0.2f to Color.DarkGray.copy(alpha = 0.2f),
                                0.6f to Color.DarkGray.copy(alpha = 0.2f),
                                0.7f to Color.DarkGray.copy(alpha = 0.05f),
                                1f to Color.Transparent
                            )
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.3f to Color.DarkGray.copy(alpha = 0.05f),
                                0.4f to Color.DarkGray.copy(alpha = 0.2f),
                                0.8f to Color.DarkGray.copy(alpha = 0.2f),
                                1f to Color.Transparent
                            )
                        ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = stringResource(R.string.watch_shape),
                    fontSize = 18.sp
                )
            }
        }
    }
}
