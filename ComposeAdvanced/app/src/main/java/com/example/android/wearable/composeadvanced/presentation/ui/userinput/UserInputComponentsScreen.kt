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
@file:OptIn(ExperimentalComposeLayoutApi::class)

package com.example.android.wearable.composeadvanced.presentation.ui.userinput

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import com.example.android.wearable.composeadvanced.R
import com.google.android.horologist.compose.navscaffold.ExperimentalComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollableColumn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Displays a value by using a Stepper or a Slider
 */
@Composable
fun UserInputComponentsScreen(
    scalingLazyListState: ScalingLazyListState,
    focusRequester: FocusRequester,
    value: Int,
    dateTime: LocalDateTime,
    onClickStepper: () -> Unit,
    onClickSlider: () -> Unit,
    onClickDemoDatePicker: () -> Unit,
    onClickDemo12hTimePicker: () -> Unit,
    onClickDemo24hTimePicker: () -> Unit,
) {
    ScalingLazyColumn(
        modifier = Modifier.scrollableColumn(focusRequester, scalingLazyListState),
        state = scalingLazyListState
    ) {
        item {
            Chip(
                onClick = onClickStepper,
                label = {
                    Text(
                        stringResource(id = R.string.stepper_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    Text(
                        text = value.toString(),
                    )
                }
            )
        }

        item {
            Chip(
                onClick = onClickSlider,
                label = {
                    Text(
                        stringResource(id = R.string.slider_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    Text(
                        text = value.toString(),
                    )
                }
            )
        }

        item {
            Chip(
                onClick = onClickDemoDatePicker,
                label = {
                    Text(
                        stringResource(R.string.date_picker_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    Text(
                        text = dateTime.toLocalDate().toString(),
                    )
                }
            )
        }

        item {
            Chip(
                onClick = onClickDemo12hTimePicker,
                label = {
                    Text(
                        stringResource(R.string.time_12h_picker_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    val formatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
                    Text(
                        text = dateTime.toLocalTime().format(formatter),
                    )
                }
            )
        }

        item {
            Chip(
                onClick = onClickDemo24hTimePicker,
                label = {
                    Text(
                        stringResource(R.string.time_24h_picker_label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                secondaryLabel = {
                    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
                    Text(
                        text = dateTime.toLocalTime().format(formatter),
                    )
                }
            )
        }
    }
}
